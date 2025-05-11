# kafka-streams-template
🛰️ Kafka Streams Template for Honeypot Integration

## 🧠 Introduction

This project serves as a Kafka Streams template for data preprocessing  from various honeypots. It is designed to provide real-time data cleansing, filtering, and standardizing event data before forwarding it to MongoDB sinks.

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

Additional honeypots or topics can be added by modifying the stream topology in the source code.

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
│       ├── RdpyStreamProcessor.java
│       └── StreamTopology.java
├── resources/
│   └── application.properties
├── docker-compose.yml
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
TODO: place to edit the topics

### Build the Project
```
mvn clean install
```

### Run the Project
java -jar target/kafka-honeypot-streams-1.0.jar

