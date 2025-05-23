package com.honeypie.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class ConpotStreamProcessor {
    static String INPUT_TOPIC = "input.honeypot.conpot";
    static String OUTPUT_TOPIC = "output.honeypot.conpot";
    static String FILTER_TOPIC = "filter-topic";
   
    // # to apply filter on src_ip
    // private static boolean hasValidSrcIp(String raw) {
    //     try {
    //         ObjectMapper mapper = new ObjectMapper();
    //         JsonNode root = mapper.readTree(raw);
    //         String srcIp = root.get("src_ip").asText();
    //         return srcIp != null && !srcIp.isEmpty();
    //     } catch (Exception e) {
    //         return false;
    //     }
    // }


    public static void main(String[] args) {
        // 1. Configure Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "conpot-app");
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
                JsonNode root = mapper.readTree(raw); // Parse JSON  

                // Extract fields
                String dst_port = root.get("dst_port").asText();
                String protocol = "";

                if (dst_port.equals("2121")) {
                    protocol = "ftp";
                } else if (dst_port.equals("5020")) {
                    protocol = "modbus";
                } else if (dst_port.equals("6230")) {
                    protocol = "ipmi";
                } else if (dst_port.equals("6969")) {
                    protocol = "tftp";
                } else if (dst_port.equals("8800")) {
                    protocol = "http";
                } else if (dst_port.equals("10201")) {
                    protocol = "s7comm";
                } else if (dst_port.equals("16100")) {
                    protocol = "snmp";
                } else if (dst_port.equals("47808")) {
                    protocol = "bacnet";
                } else if (dst_port.equals("44818")) {
                    protocol = "enip";
                } else {
                    protocol = "unknown";
                }

                // Add fields
                ((ObjectNode) root).put("protocol", protocol);

                return mapper.writeValueAsString(root); // Serialize back to string
            } catch (Exception e) {
                return raw;
            }
        }).to(OUTPUT_TOPIC); // Write to output topic

        // # to apply filter on src_ip
        // .to((key, value, recordContext) -> hasValidSrcIp(value) ? OUTPUT_TOPIC : FILTER_TOPIC);


        // 3. Start Streams
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
