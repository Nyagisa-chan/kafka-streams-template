package com.honeypie.app;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DionaeaEwsStreamProcessor {
    static String INPUT_TOPIC = "input.honeypot.dionaea_ews";
    static String OUTPUT_TOPIC = "output.honeypot.dionaea_ews";
    static String FILTER_TOPIC = "filter-topic";
   
    private static boolean hasValidSrcIp(String raw) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(raw);
            String srcIp = root.get("src_ip").asText();
            return srcIp != null && !srcIp.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }


    public static void main(String[] args) {
        // 1. Configure Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "dionaea-ews-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());


        // 2. Build Topology
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> stream = builder.stream(INPUT_TOPIC);
        ObjectMapper mapper = new ObjectMapper();


        stream.mapValues(raw -> {
            try {
                // — Convert Extended JSON _id → plain hex string
                JsonNode root = mapper.readTree(raw);
                JsonNode idNode = root.get("_id");
                if (idNode != null && idNode.has("$oid")) {
                    String hex = idNode.get("$oid").asText();
                    // replace the object with a simple string
                    ((ObjectNode) root).put("_id", hex);
                }
                return mapper.writeValueAsString(root);
            } catch (Exception e) {
                return raw;  // leave it as-is on parse error
            }
        }).mapValues(raw -> {
            try {
                // deserialize the JSON string to a JsonNode object
                // to support "{\"\"key\":\"value\"}" format
                String unescaped = mapper.readValue(raw, String.class);
                JsonNode root = mapper.readTree(unescaped); // Parse JSON  
                String dstIp = root.get("dst_ip").asText(); // Extract fields
                if (dstIp.startsWith("::ffff:")) {
                    String ipv4 = dstIp.replaceFirst("^::ffff:", "");
                    ((ObjectNode) root).put("dst_ip", ipv4);//Update JSON tree
                }
                return mapper.writeValueAsString(root); // Serialize back to string
            } catch (Exception e) {
                return raw;
            }
        }).mapValues(raw -> {
            try {
                // String unescaped = mapper.readValue(raw, String.class);
                JsonNode root = mapper.readTree(raw); // Parse JSON  
                String srcIp = root.get("src_ip").asText(); // Extract fields
                if (srcIp.startsWith("::ffff:")) {
                    String ipv4 = srcIp.replaceFirst("^::ffff:", "");
                    ((ObjectNode) root).put("src_ip", ipv4);//Update JSON tree
                }
                return mapper.writeValueAsString(root); // Serialize back to string
            } catch (Exception e) {
                return raw;
            }
        }).mapValues(raw -> {
            try {
                // String unescaped = mapper.readValue(raw, String.class);
                JsonNode root = mapper.readTree(raw);
                String timestampStr = root.get("timestamp").asText();
                if (!timestampStr.endsWith("Z")) {
                    timestampStr += 'Z';
                    // timestampStr = timestampStr.substring(0, timestampStr.length() - 1);
                }
                ((ObjectNode) root).put("timestamp", timestampStr);
                return mapper.writeValueAsString(root);
            } catch (Exception e) {
                return raw;
            }
        }).mapValues(raw -> {
            try {
                // add protocol
                String unescaped = mapper.readValue(raw, String.class);
                JsonNode root = mapper.readTree(unescaped); // Parse JSON  

                // Extract fields
                String dst_port = root.get("dst_port").asText();
                String protocol = "";

                if (dst_port.equals("21")) {
                    protocol = "FTP";
                } else if (dst_port.equals("42")) {
                    protocol = "Host Name Server";
                } else if (dst_port.equals("69")) {
                    protocol = "TFTP";
                } else if (dst_port.equals("80")) {
                    protocol = "HTTP";
                } else if (dst_port.equals("135")) {
                    protocol = "DCE/RPC Endpoint Mapper";
                } else if (dst_port.equals("443")) {
                    protocol = "HTTPS";
                } else if (dst_port.equals("445")) {
                    protocol = "SMB";
                } else if (dst_port.equals("1433")) {
                    protocol = "MSSQL";
                } else if (dst_port.equals("1723")) {
                    protocol = "PPTP";
                } else if (dst_port.equals("1883")) {
                    protocol = "MQTT";
                } else if (dst_port.equals("3306")) {
                    protocol = "MySQL";
                } else if (dst_port.equals("5060")) {
                    protocol = "SIP";
                } else if (dst_port.equals("5061")) {
                    protocol = "SIPS";
                } else if (dst_port.equals("11211")) {
                    protocol = "MEMCACHED";
                } else {
                    protocol = "UNKNOWN";
                }

                // Add fields
                ((ObjectNode) root).put("protocol", protocol);

                return mapper.writeValueAsString(root); // Serialize back to string
            } catch (Exception e) {
                return raw;
            }
        }).to((key, value, recordContext) -> hasValidSrcIp(value) ? OUTPUT_TOPIC : FILTER_TOPIC);


        // 3. Start Streams
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
