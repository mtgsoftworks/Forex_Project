# Forex Project - Real-time Currency Exchange Rate Processing System

[![Java](https://img.shields.io/badge/Java-23-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk23-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Kafka-7.x-blue.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7.x-red.svg)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 🌟 Overview

This project is a comprehensive **real-time forex exchange rate processing system** that simulates and processes currency exchange data from multiple data provider platforms. The system uses a modular architecture with **TCP streaming** and **REST API** data sources, featuring dynamic calculations, real-time monitoring, and enterprise-grade data processing capabilities.

### Key Features

- **Multi-Platform Data Ingestion**: TCP streaming (PF1) and REST API (PF2) data providers
- **Real-time Data Processing**: Live forex rate calculations and transformations
- **Enterprise Messaging**: Kafka-based event streaming architecture
- **High-Performance Caching**: Redis for raw data storage and fast retrieval
- **Data Persistence**: PostgreSQL for reliable data storage
- **Search & Analytics**: OpenSearch/Elasticsearch integration
- **Monitoring & Alerting**: Email notifications and comprehensive logging
- **Scalable Architecture**: Microservices-based modular design

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           Data Storage Layer                             │
│  ┌─────────────────┐              ┌─────────────────┐                   │
│  │   Raw Rates     │              │ Calculated Rates│                   │
│  │ (Hazelcast/Redis)│              │ (Hazelcast/Redis)│                   │
│  └─────────┬───────┘              └─────────┬───────┘                   │
└────────────┼────────────────────────────────┼───────────────────────────┘
             │                                │
             ▼                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         Coordinator Service                             │
│                                                                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐ │
│  │ Subscriber 1│  │ Subscriber 2│  │ Subscriber n│  │  Kafka Producer │ │
│  │    (TCP)    │  │   (REST)    │  │    (FIX)    │  │                 │ │
│  └─────┬───────┘  └─────┬───────┘  └─────┬───────┘  └─────┬───────────┘ │
│        │                │                │                │             │
└────────┼────────────────┼────────────────┼────────────────┼─────────────┘
         ▲                ▲                ▲                │
         │                │                │                ▼
┌────────┼───┐    ┌───────┼────┐   ┌───────┼────┐    ┌─────────────┐
│Platform│   │    │Platform│    │   │Platform│    │    │   Logger    │
│App 1   │TCP│    │App 2   │REST│   │App n   │ FIX│    │             │
│        │   │    │        │ API│   │        │    │    └─────┬───────┘
└────────────┘    └────────────┘   └────────────┘          │
                                                           ▼
                                                    ┌─────────────┐
                                          ┌─────────┤    Log      │
                                          │         │             │
                                          │         └─────────────┘
                                          │
                                          ▼         ┌─────────────┐
                                    ┌─────────────┐ │  Filebeat   │
                                    │   Kafka     │ │             │
                                    │             │ └─────┬───────┘
                                    └─────┬───────┘       │
                                          │               ▼
                            ┌─────────────┼─────────────────────┐
                            │             ▼                     │
                    ┌───────────────┐              ┌───────────────┐
                    │Kafka Consumer │              │ OpenSearch    │
                    │ (Hibernate)   │              │               │
                    └───────┬───────┘              │               │
                            │                      │               │
                            ▼                      └───────────────┘
                    ┌───────────────┐              ┌───────────────┐
                    │  PostgreSQL   │              │ OpenSearch    │
                    │               │              │ (Logs)        │
                    └───────────────┘              └───────────────┘
```

### Data Flow Architecture

1. **Multi-Protocol Data Ingestion**: 
   - **Platform App 1**: TCP-based real-time streaming
   - **Platform App 2**: REST API with polling mechanism  
   - **Platform App n**: FIX protocol support for financial messaging

2. **Centralized Coordination**:
   - **Coordinator Service** acts as the central hub managing all data providers
   - **Subscriber Components** handle different protocol types (TCP, REST, FIX)
   - **Kafka Producer** streams processed data to message queues

3. **Dual-Layer Caching**:
   - **Raw Rates Storage**: Hazelcast/Redis for unprocessed forex data
   - **Calculated Rates Storage**: Hazelcast/Redis for computed results

4. **Event-Driven Processing**:
   - **Kafka Message Bus** enables asynchronous data processing
   - **Kafka Consumer (Hibernate)** persists data with ORM mapping
   - **Parallel Processing** for high-throughput data streams

5. **Comprehensive Logging & Monitoring**:
   - **Logger Service** captures application events and metrics
   - **Filebeat** ships logs to OpenSearch for analysis
   - **Dual OpenSearch Instances** for data analytics and log aggregation

6. **Enterprise Data Persistence**:
   - **PostgreSQL** for ACID-compliant transactional data storage
   - **OpenSearch** for full-text search and real-time analytics

## 🏛️ Module Structure

### Core Modules

| Module | Description | Port | Technology Stack |
|--------|-------------|------|------------------|
| **common** | Shared models, DTOs, mappers, and JPA repositories | - | Spring Data JPA |
| **platform-tcp** | TCP streaming data provider with socket connections | 8081 | Java NIO, TCP Sockets |
| **platform-rest** | REST API + SSE streaming provider | 8082 | Spring WebFlux, SSE |
| **platform-fix** | FIX protocol financial messaging provider | 8083 | QuickFIX/J |
| **coordinator** | Central orchestration with multi-protocol subscribers | 8080 | Spring Boot, Groovy |
| **kafka-consumer** | Data persistence service with Hibernate ORM | - | Kafka Streams, Hibernate |
| **logger** | Centralized logging and monitoring service | - | Logback, SLF4J |

### Infrastructure Services

| Service | Description | Port | Purpose |
|---------|-------------|------|---------|
| **Kafka** | Message streaming platform | 9092 | Event-driven architecture |
| **Hazelcast/Redis** | Distributed caching layer | 6379/5701 | Raw & calculated rate caching |
| **PostgreSQL** | Relational database | 5432 | Transactional data persistence |
| **OpenSearch (Data)** | Search and analytics engine | 9200 | Financial data analytics |
| **OpenSearch (Logs)** | Log search and analysis | 9201 | Application log analytics |
| **Filebeat** | Log shipper | - | Log collection and forwarding |

## 🚀 Quick Start

### Prerequisites

Ensure you have the following software installed:

- **Java 23**: [Oracle JDK 23](https://www.oracle.com/java/technologies/javase/jdk23-archive-downloads.html) or [OpenJDK 23](https://openjdk.java.net/projects/jdk/23/)
- **Maven 3.8+**: [Maven Download](https://maven.apache.org/download.cgi)
- **Docker**: [Windows](https://docs.docker.com/desktop/install/windows-install/) | [Mac](https://docs.docker.com/desktop/install/mac-install/) | [Linux](https://docs.docker.com/engine/install/)
- **Docker Compose**: [Installation Guide](https://docs.docker.com/compose/install/)
- **Git**: [Git Download](https://git-scm.com/downloads)

### Installation & Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/mtgsoftworks/Forex_Project.git
   cd Forex_Project
   ```

2. **Build the Project**
   ```bash
   # Build all modules (skip tests for quick setup)
   mvn clean install -DskipTests
   ```

3. **Start Infrastructure Services**
   ```bash
   # Start all Docker services
   docker-compose up -d
   
   # Follow logs (optional)
   docker-compose logs -f
   ```

4. **Start Application Modules**
   
   **Option A: Individual Terminal Windows**
   ```bash
   # Terminal 1: TCP Platform
   cd platform-tcp
   mvn spring-boot:run
   
   # Terminal 2: REST Platform
   cd platform-rest
   mvn spring-boot:run
   
   # Terminal 3: Coordinator
   cd coordinator
   mvn spring-boot:run
   
   # Terminal 4: Kafka Consumer
   cd kafka-consumer
   mvn spring-boot:run
   ```

### Verification

Check that all services are running:

```bash
# Verify Docker services
docker-compose ps

# Test Kafka
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Test Redis
docker exec -it redis redis-cli ping

# Test Coordinator API
curl http://localhost:8080/api/manual/pf2/PF2_USDTRY
```

## ⚙️ Configuration

### Provider Configuration

Each module can be configured via its respective `application.yml` file:

#### PF2 (REST Provider) Configuration
```yaml
pf2:
  rest:
    base-url: http://localhost:8082/api/rates/
    poll-interval: 1000  # milliseconds
    enabled: false       # auto-polling enabled/disabled
    manual-mode: false   # manual-only mode
```

**Configuration Modes:**
- `enabled=false, manual-mode=false`: Auto-polling disabled, manual endpoint available
- `enabled=true, manual-mode=false`: Auto-polling every `poll-interval` ms
- `enabled=true, manual-mode=true`: Manual-only mode, no auto-polling

#### PF1 (TCP Provider) Configuration
```yaml
pf1:
  tcp:
    host: localhost
    port: 8081
    enabled: false  # auto-connect on startup
```

**TCP Modes:**
- `enabled=false`: Manual connection via API
- `enabled=true`: Auto-connect on application startup

## 🔌 API Documentation

### Coordinator Service (Port 8080)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/manual/pf2/{symbol}` | GET | Manual PF2 data fetch |
| `/api/status` | GET | Service health status |

### REST Platform Service (Port 8082)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/rates/{rateName}` | GET | Single forex rate |
| `/api/rates/stream/{rateName}` | GET | SSE stream |

### TCP Platform Service (Port 8081)

**TCP Protocol Commands:**
```
subscribe|PF1_USDTRY    # Subscribe to USD/TRY rates
unsubscribe|PF1_USDTRY  # Unsubscribe from rates
```

## 🧪 Testing & Development

### Manual Testing

1. **TCP Client Test**
   ```java
   // TCPClient.java
   import java.io.*;
   import java.net.*;
   
   public class TCPClient {
       public static void main(String[] args) {
           try {
               Socket socket = new Socket("localhost", 8081);
               PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
               BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
               
               // Subscribe to USD/TRY rates
               out.println("subscribe|PF1_USDTRY");
               
               // Read incoming messages
               String line;
               while ((line = in.readLine()) != null) {
                   System.out.println("Received: " + line);
               }
               socket.close();
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
   }
   ```

2. **Data Verification Commands**
   ```bash
   # Check raw data in Redis
   docker exec -it redis redis-cli LRANGE raw:PF2_USDTRY 0 -1
   
   # Check computed data in Kafka
   docker exec -it kafka kafka-console-consumer \
     --topic computed:USDTRY \
     --from-beginning \
     --bootstrap-server localhost:9092
   ```

### Running Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn verify -Pintegration-tests

# Docker Compose test profile
docker-compose -f docker-compose.yml -f docker-compose.test.yml up
```

## 📊 Monitoring & Observability

### OpenSearch Dashboards
- **URL**: http://localhost:5601
- **Username**: admin
- **Password**: admin

### Log Management
- Application logs: `logs/` directory
- Logstash pipelines: `logstash/pipeline/*.conf`
- OpenSearch endpoint: http://localhost:9200

### Data Monitoring

**Supported Currency Pairs:**
- USD/TRY (Turkish Lira)
- EUR/USD (Euro/US Dollar)
- GBP/USD (British Pound/US Dollar)

**Kafka Topics:**
- `forex_topic`: Raw data stream
- `computed:USDTRY`: Computed USD/TRY rates
- `computed:EURUSD`: Computed EUR/USD rates
- `computed:GBPUSD`: Computed GBP/USD rates

## 🛠️ Technology Stack

### Backend Technologies
- **Java 23**: Core programming language
- **Spring Boot 3.4.2**: Application framework
- **Maven 3.9.3**: Dependency management
- **Groovy 3.0.9**: Dynamic calculations and scripting

### Data & Messaging
- **Apache Kafka (Confluent CP 7.x)**: Event streaming platform
- **Redis 7.x**: In-memory data store
- **PostgreSQL 15-alpine**: Relational database
- **OpenSearch 2.x**: Search and analytics engine

### DevOps & Monitoring
- **Docker & Docker Compose 2.4**: Containerization
- **Logstash 7.x**: Log processing pipeline
- **OpenSearch Dashboards**: Data visualization

## 🔧 Troubleshooting

### Common Issues

1. **Port Conflict Error**
   ```
   Error: Port 8080 is already in use
   ```
   **Solution**: Kill the process using the port or change port in configuration

2. **Kafka Connection Error**
   ```
   Connection refused to kafka:9092
   ```
   **Solution**: Ensure all Docker services are running with `docker-compose ps`

3. **Redis Connection Error**
   ```
   JedisConnectionException: Could not connect to redis:16379
   ```
   **Solution**: Verify Redis container is running and port configuration

4. **Java Version Error**
   ```
   UnsupportedClassVersionError: ... requires Java 23
   ```
   **Solution**: Ensure JDK 23 is installed and `JAVA_HOME` is set correctly

### Service Health Checks

```bash
# Check all Docker services
docker-compose ps

# Check specific service logs
docker-compose logs [service-name]

# Restart specific service
docker-compose restart [service-name]
```

## 🎯 Use Cases & Applications

This system is designed for:

- **Financial Institutions**: Real-time forex rate processing
- **Trading Platforms**: High-frequency trading data feeds
- **Risk Management**: Currency exposure monitoring
- **Academic Research**: Forex market analysis and simulation
- **Fintech Applications**: Currency conversion services

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java coding standards
- Write comprehensive unit tests
- Update documentation for new features
- Ensure Docker Compose compatibility

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Mesut Taha Güven** ([@mtggamer](https://github.com/mtgsoftworks))

## 🙏 Acknowledgments

- Spring Boot community for excellent framework support
- Apache Kafka for robust messaging capabilities
- Redis community for high-performance caching solutions
- OpenSearch project for powerful search and analytics
