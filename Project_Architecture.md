# Proje Mimarisi

Bu doküman, **Forex Projesi**'nin tüm bileşenlerini, birbirleriyle nasıl etkileştiğini ve konfigürasyon parametrelerinin sistem üzerinde nasıl işlediğini detaylı şekilde açıklar.

## Modüler Yapı ve Sorumluluklar

1. **common**
   - Ortak model sınıfları: `Rate`, `RateFields`, `RateStatus`, `RawRateEntity`, `CalculatedRateEntity`.
   - DTO ve Mapper: `RateDto`, `RateMapper`.
   - Repository: `RawRateRepository`, `CalculatedRateRepository` (JPA).

2. **platform-tcp (PF1)**
   - **TcpServerApplication** ile aya kalkar.
   - **TcpStreamingProperties**: `host`, `port`, `initialBid/Ask`, `driftPercentage`, `messageInterval`, `messageCount`.
   - Mesaj formatı: `RATE_NAME|<bid>:number:<value>|<ask>:number:<value>|<timestamp>`, örn: `PF1_USDTRY|22:number:34.4013|25:number:35.4013|5:timestamp:2024-12-15T11:31:34.509`.
   - **subscribe|<RATE>**, **unsubscribe|<RATE>** komutlarıyla client tarafından kontrol edilir.

3. **platform-rest (PF2)**
   - Spring Boot uygulaması.
   - **RateController**: GET `/api/rates/{rateName}`, SSE stream `/api/rates/stream/{rateName}`.
   - **RateSimulationProperties** (`application.properties` ya da `application.yml`):
     - `rates`: simüle edilecek sembol listesi.
     - `initial-bid/initial-ask`: başlangıç değerleri.
     - `drift-percentage`: her sorguda varyasyon yüzdesi.
     - `poll-interval`: SSE yayını aralığı (ms).
   - Yanıt modeli: `RateResponse(rateName, bid, ask, timestamp)` JSON.

4. **coordinator (Orkestrasyon Servisi)**
   - Spring Boot.
   - **DataProvider** arayüzü: `PF1TcpProvider`, `PF2RestProvider` ve dinamik yüklenecek sınıflar.
   - **ProviderProperties** (`coordinator.providers.classes`): runtime'da `Class.forName(...)` ile ek sağlayıcılar yüklenir.
   - **PF2RestProperties** (`pf2.rest.base-url`, `poll-interval`, `enabled`, `manual-mode`):
     - `enabled` (boolean): provider'ın **tamamen** aktif/pasif olması.
     - `manual-mode` (boolean): otomatik polling'in devre dışı bırakılması.
     - Autopoll ve manuel polling ayrımı:
       1. **enabled=false, manual-mode=false** → hiç polling yok, sadece manuel endpoint ile veri çekilebilir.
       2. **enabled=true, manual-mode=false** → uygulama aya kalkınca otomatik arka plan thread'i başlar ve her `poll-interval` ms'de `pollOnce()` ile sorgular.
       3. **enabled=true, manual-mode=true** → otomatik thread başlatılmaz, ancak `/api/manual/pf2/{symbol}` endpoint'inden `poll(symbol)` çağrısı ile tek seferlik sorgulama yapılabilir.
   - **Start Flow** (`CoordinatorService.initDataProviders()`):
     1. Tüm `DataProvider`'lar `setCallback(this)` ile callback'e bağlanır.
     2. `startProvider()` çağrılır. Enabled/manual kontrolü burada yapılır.
     3. **PF2RestProvider** ise ardından `props.getRates().forEach(...) subscribe(...)` ile sembol abonelikleri kaydeder.
   - Callback metotları (`onRateAvailable`, `onRateUpdate`, `onRateStatus`):
     - **Redis List**: `raw:<rate>` anahtarına `rightPush(message)` ile ham veri.
     - **Kafka Topic**: `kafkaTemplate.send(kafkaProps.getRawTopic(), message)` ile ham veri.
     - **Dynamic Calculation**: `FormulaService` Groovy script'lerini (`resources/formulas/*.groovy`) JSR223 engine ile çalıştırır.
     - **AlarmService**: PF2 hata durumlarını (`RateStatus.ERROR`) `thresholdSeconds` üzeri sürerse sembol aboneliğini iptal eder ve e-posta yollar.

5. **kafka-consumer**
   - `ConsumerService`: ham ve hesaplanmış verileri PostgreSQL tablolarına (`raw_data`, `calculated_data`) yazar.
   - `ElasticConsumerService`: Elasticsearch/OpenSearch'e indeksler.

6. **logstash**
   - `logstash/conf/logstash.conf`: log toplama ve Elasticsearch yönlendirme.

7. **Docker Compose**
   - `docker-compose.yml`: Kafka, Zookeeper, Redis, PostgreSQL, OpenSearch, Logstash servislerini ayağa kaldırır.

## Veri Akış Diyagramı

```mermaid
flowchart TD
  subgraph "1. Providers"
    direction TB
    PF1[TCP Streaming Simulator<br>(PF1TcpProvider)]
    PF2[REST API Simulator<br>(PF2RestProvider)]
  end

  subgraph "2. Coordinator Service"
    direction TB
    subgraph "Data Providers"
      PF1Prov[PF1TcpProvider]
      PF2Prov[PF2RestProvider]
      DynProv[Dynamic Providers via ProviderProperties]
    end
    CalcServ[FormulaService & CalculationService<br>(Static & Groovy)]
    ASvc[AlarmService<br>@Scheduled(check-interval ms)]
    CPSvc[CoordinatorService]
  end

  PF1 -->|connect/subscribe| PF1Prov
  PF2 -->|auto/manual poll| PF2Prov
  PF1Prov --> CPSvc
  PF2Prov --> CPSvc
  DynProv --> CPSvc
  CPSvc -->|updateLastResponse| ASvc
  CPSvc -->|localCache→ Map| LocalCache[(In-memory Cache)]
  CPSvc -->|write raw| RedisRaw[(Redis List raw:<rate> & Stream raw_stream)]
  CPSvc -->|publish raw| KafkaTopic[(Kafka Topic forex_topic)]
  CPSvc -->|trigger calc| CalcServ
  CalcServ -->|write computed| RedisComp[(Redis List computed:<rate> & Stream computed_stream)]
  CalcServ -->|publish computed| KafkaTopic
  ASvc -->|threshold check & send| Mail[JavaMailSender → recipient-email]

  subgraph "3. Consumers & Storage"
    RawCons[ConsumerService<br>(group forex_group)]
    RawCons --> DBRaw[(PostgreSQL raw_data)]
    RawCons --> DBCalc[(PostgreSQL calculated_data)]
    ElasticCons[ElasticConsumerService<br>(group forex_elastic_group)]
    ElasticCons --> ES[(OpenSearch index forex_rates)]
  end
  KafkaTopic --> RawCons
  KafkaTopic --> ElasticCons

  subgraph "4. Infrastructure"
    ZK[Zookeeper]
    KB[Kafka Broker]
    RS[Redis]
    PG[PostgreSQL]
    OS[OpenSearch]
    LS[Logstash]
    DC[(Docker Compose)]
    DC --> ZK & KB & RS & PG & OS & LS
    LS --> Elasticsearch[(Elasticsearch Logs Index)]
  end
```

### Akış Adımları
1. **Başlatma**: `ProviderProperties` okunur, PF1 ve PF2 (ve dinamik sınıflar) `CoordinatorService` içinde `startProvider()` ile ayağa kalkar.
2. **Data Receipt**: `onRateAvailable` ile gelen her raw veri:
   - `updateLastResponse(platform)` ile alarm zamanlaması sıfırlanır.
   - local cache güncellenir.
   - Mesaj → Redis List & Stream → `raw:<rate>` & `raw_stream`.
   - Mesaj → Kafka Topic `forex_topic`.
3. **Hesaplama**: `processComputedRates()`:
   - `FormulaService` (statik) veya Groovy script (`calculation.formulas`) kullanılır.
   - Hesaplanan → Redis List & Stream → `computed:<rate>` & `computed_stream`.
   - Hesaplanan → Kafka Topic `forex_topic`.
4. **AlarmService**:
   - `@Scheduled(fixedRateString=check-interval)` ile her milisaniyede kontrol.
   - `threshold-seconds` aşılırsa: console log, `logger.warn`, e-posta gönderimi.
5. **Kafka Consumers**:
   - `ConsumerService` (group `forex_group`): `raw` ve `computed` mesajları alır → PostgreSQL koltuklarında saklar.
   - `ElasticConsumerService` (group `forex_elastic_group`): mesajları indeksler → OpenSearch `forex_rates`.
6. **Logstash**: uygulama log'larını toplar → Elasticsearch.
7. **Orkestrasyon**: `docker-compose.yml` ile Zookeeper, Kafka, Redis, PostgreSQL, OpenSearch, Logstash ayağa kalkar.

Not: Tüm bağlantı ve ayarlar `application.yml` içinden (`spring.kafka`, `spring.data.redis`, `pf1.tcp`, `pf2.rest`, `coordinator.alarm.recipient-email`) kontrol edilir.

## 8. Konfigürasyon Detayları

Bu projede tüm uygulama modüllerinin konfigürasyonu `application.yml` dosyaları üzerinden sağlanır. Ortam değişkenleri ve Docker Compose iç ağı üzerinden servis adları kullanılır. Öne çıkan parametreler:

```yaml
spring:
  kafka:
    # Kafka broker listesi, Compose ağındaki tcp bazlı servis
    bootstrap-servers: kafka:9092
  data:
    redis:
      # Redis host ve port
      host: redis
      port: 16379
  datasource:
    # PostgreSQL veritabanı bağlantısı
    url: jdbc:postgresql://postgres:5432/forexdb
    username: postgres
    password: 8465

pf2:
  rest:
    base-url: http://platform-rest:8082/api/rates/
    poll-interval: 1000  # ms cinsinden otomatik polling aralığı
    enabled: true        # REST provider aktif/pasif durumu
    manual-mode: false   # manuel polling kontrolü

pf1:
  tcp:
    host: platform-tcp  # TCP tabanlı simulator'un host adı
    port: 8081         # TCP portu
    enabled: true      # TCP provider aktif/pasif durumu
```

### Ortam Değişkenleri
- `SERVER_PORT` (modül çalışma portu)
- `SPRING_PROFILES_ACTIVE` (aktif profil seçimi)
- `JAVA_OPTS` (JVM ayarları)

## 9. Docker Compose Yapısı

**Not:** Docker Compose, her servis için ilgili klasördeki `Dockerfile` (uzantısız) dosyasını kullanır. `Dockerfile.dev` dosyaları yalnızca geliştirme ortamında, manuel build veya override senaryoları için kullanılmaktadır.

`docker-compose.yml` dosyasında aşağıdaki altyapı servisleri ve `app` servisi tanımlıdır:

- redis (16379)
- zookeeper (32181)
- kafka (9092, 29092)
- postgres (5432)
- opensearch-node1 (9200)
- opensearch-dashboards (5601)
- logstash (5000)
- app (platform-modüllerinin bir araya getirildiği tek konteyner)

Her servisin kendi `depends_on` tanımı ve `healthcheck` koşulları vardır. Örneğin:

```yaml
services:
  app:
    build: .
    depends_on:
      - kafka
      - redis
      - postgres
    ports:
      - "8082:8082"
      - "8080:8080"
      - "8081:8081"
      - "8090:8090"
      - "8100:8100"
    networks:
      - mynetwork
```

## 10. İzleme ve Loglama

- **Log4j2**: Her modül kendi `log4j2.xml` yapılandırmasını kullanır. Log seviyesi ve format `src/main/resources/log4j2.xml` içinde ayarlanır.
- **Logstash**: Uygulama log'ları `logstash/pipeline/*.conf` ile toplanıp OpenSearch'e iletilir.
- **OpenSearch Dashboards**: Port 5601 üzerinden erişim. Örnek gösterge panelleri hazır.
- **Healthcheck**: Docker Compose içinde servis sağlık kontrolleri (`curl` ile _cluster/health_) tanımlıdır.

## 11. Test, QA ve Sürekli Entegrasyon

- **Unit Test**: Her modülde `spring-boot-starter-test` kullanılarak JUnit 5 tabanlı testler yazılmıştır.
- **Integration Test**: Docker Compose Test Profile altında gerçek Redis ve Kafka ile entegrasyon testleri yapılabilir.
- **CI/CD** örneği (GitHub Actions):

```yaml
name: CI
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 23
        uses: actions/setup-java@v3
        with:
          java-version: '23'
      - name: Build with Maven
        run: mvn clean package -DskipTests
      - name: Docker Compose up
        run: docker-compose up -d --build
      - name: Run Integration Tests
        run: mvn verify -Pintegration-tests
```

---
