# Forex Projesi

## Genel Bakış
Bu proje, farklı veri sağlayıcı platformlarından (PF1: TCP Streaming ve PF2: REST API) alınan döviz kuru verilerini entegre eden, dinamik ve statik hesaplamalar yapan modüler bir mimaridir. Hesaplama sonuçları Redis ve Kafka üzerinden yayınlanır; Kafka Consumer bileşeni ise bu verileri alarak veritabanına yazar ve log indeksleme yapar.

## Mimari Bileşenler

1. **PF1 (TCP Streaming Simulator)**
   - Teknoloji: Standalone Java
   - Port: 8081 (TCP)
   - Yapılandırma: `platform-tcp/src/main/resources/application.properties`
     - `tcp.streaming.port`
     - `tcp.streaming.rates`
     - `tcp.streaming.initialBid`, `tcp.streaming.initialAsk`
     - `tcp.streaming.messageInterval`
     - `tcp.streaming.messageCount`
     - `tcp.streaming.driftPercentage`
   - Komutlar: `subscribe|<RATE>`, `unsubscribe|<RATE>`
   - Mesaj Formatı: `RATE_NAME|<bid>|<ask>|<timestamp>`

2. **PF2 (REST API Simulator)**
   - Teknoloji: Spring Boot
   - Port: 8080 (HTTP)
   - Yapılandırma: `platform-rest/src/main/resources/application.yml`
     ```yaml
     pf2:
       rest:
         base-url: http://localhost:8080/api/rates/
         poll-interval: 1000
     ```
   - `Pf2RestProperties`, `PF2RestProvider` (RestTemplate ile periyodik polling)

3. **Coordinator (Orkestrasyon Servisi)**
   - Teknoloji: Spring Boot
   - Görev: Tüm `DataProvider` bileşenlerini yönetir, gelen verileri işler ve hesaplama akışını koordine eder.
   - Ana Sınıflar:
     - `CoordinatorService` (Redis, Kafka entegrasyonu)
     - `CalculationService` (statik ve dinamik (Groovy) hesaplamalar)
   - Yapılandırma: `coordinator/src/main/resources/application.yml`
   - Logging: Log4j2

4. **Kafka Consumer**
   - Teknoloji: Spring Kafka Consumer
   - Görev: Kafka’dan gelen mesajları alır, veritabanına yazar ve log indeksleme yapar (Logstash/Elasticsearch).

## Hesaplama Mekanizması
- **Statik Metodlar**: `FormulaService` üzerinden `prepareUsdTryMessage`, `prepareEurTryMessage`, vb.
- **Dinamik Metod**: `computeDynamicCalculation` (Groovy JSR223)
- Formüller: `resources/formulas.properties` dosyasına tanımlı, kolayca güncellenebilir.

## Yapılandırma Yönetimi
Modüllere göre ayrılmış `application.yml` ve `application.properties` dosyaları vardır. `@ConfigurationProperties` ile Spring Context’e enjekte edilir.

## Çalıştırma Adımları
1. **Önkoşullar**
   - Java 23 ve Maven 3.8+
   - Kafka & Zookeeper
   - Redis
2. **Derleme ve Test**
   ```bash
   mvn clean install
   ```
3. **Çalıştırma Sırası**
   1. PF1 (TCP Streaming)  
   2. PF2 (REST API)  
   3. Coordinator (Orkestrasyon)  
   4. Kafka Consumer  
4. **Docker Compose**
   ```bash
   docker-compose up
   ```

## Testler
- Birim testleri: `PF2RestProviderTest`, `CalculationServiceTest`
- Entegrasyon testleri: `CoordinatorServicePF2IntegrationTest`

### Katkıda Bulunanlar
- MTG Softworks (Okul Not Ortalaması: 2.01)
- 


