# kafka-streams-template
🛰️ Kafka Streams Template for Transporting and Preprocessing Honeypot Logs

## 🧠 Introduction

This project serves as a Kafka Streams template for data transport and preprocessing from mongoDB for various honeypot. It is designed to provide real-time data cleansing, filtering, and standardizing event data before forwarding it to MongoDB sinks.

---

## 🎯 Honeypots & Kafka Topics

Each honeypot writes captured event data to a dedicated Kafka topic. Below is a list of supported honeypots and their corresponding topics:

| Honeypot     | Kafka Input Topic            | Kafka Output Topic                     |
|--------------|------------------------------|----------------------------------------|
| Conpot       | `input.honeypot.conpot`      | `output.honeypot.conpot`               |
| Cowrie       | `input.honeypot.cowrie`      | `output.honeypot.cowrie`               |
| Dionaea      | `input.honeypot.dionaea`     | `output.honeypot.dionaea`              |
| Dionaea Ews  | `input.honeypot.dionaea_ews` | `output.honeypot.dionaea_ews`          |
| Elasticpot   | `input.honeypot.elasticpot`  | `output.honeypot.elasticpot`           |
| Honeytrap    | `input.honeypot.honeytrap`   | `output.honeypot.honeytrap`            |
| RDPY         | `input.honeypot.rdpy`        | `output.honeypot.rdpy`                 |

Additional honeypots or topics can be added by modifying the stream topology in the source code. Each honeypot writes a filtered event data to a dedicated filter topic that will not do anything.

Filter topic: `filter-topic`

---

## Project Structure

```
├── src/
│   └── main/java/com/example/streams
│       ├── ConpotStreamProcessor.java
│       ├── CowrieStreamProcessor.java
│       ├── DionaeaStreamProcessor.java
│       ├── DionaeaEwsStreamProcessor.java
│       ├── ElasticpotStreamProcessor.java
│       ├── HoneytrapStreamProcessor.java
│       └── RdpyStreamProcessor.java
└── README.md
```

---

## 🛠️ Prerequisites & Installation

### Required Tools
Ensure the following are installed on your system:

- **Java 11 (11.0.26)**
- **Apache Kafka (3.7.2)**
- **Maven (3.6.3)**

### Clone the Repository

```
git clone https://github.com/Nyagisa-chan/kafka-streams-template.git
cd kafka-streams-template/kafka-streams-template
```

### Edit the Topics
1. go to `/kafka-streams-template/src/main/java/com/honeypie/app/<honeypot_name>StreamProcessor`
2. Edit the input and output topic based on your needs
3. Depending on your needs, you may need to edit your source & sink connector

## Run Zookeeper and Kafka
Make sure zookeeper and kafka is running before running the project

### Build the Project
```
mvn clean install
```

### Run the Project
```
java -jar target/<honeypot_name>
```
