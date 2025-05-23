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

public class HoneytrapStreamProcessor {
    static String INPUT_TOPIC = "input.honeypot.honeytrap";
    static String OUTPUT_TOPIC = "output.honeypot.honeytrap";
    static String FILTER_TOPIC = "filter-topic";

    private static boolean validLogs(String raw) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(raw);
            String protocol = root.get("protocol").asText();
            return protocol != null && !protocol.isEmpty() && !protocol.equals("heartbeat");
        } catch (Exception e) {
            return false;
        }
    }


    public static void main(String[] args) {
        // 1. Configure Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "honeytrap-app");
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
        }).to((key, value, recordContext) -> validLogs(value) ? OUTPUT_TOPIC : FILTER_TOPIC);


        // 3. Start Streams
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
