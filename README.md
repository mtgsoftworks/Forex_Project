# Forex Projesi

## Genel Bakış
Bu proje, farklı veri sağlayıcı platformlarından (PF1: TCP Streaming ve PF2: REST API) alınan döviz kuru verilerini modüler bir şekilde işleyen, dinamik hesaplama ve izleme özelliklerine sahip bir mimaridir.

- **Ham Veri**: Redis List (`raw:<rate>`)
- **Hesaplanmış Veri**: Kafka Topic (`computed:<symbol>`)
- **Veri Depolama**: PostgreSQL (`raw_data`, `calculated_data` tablosu)
- **Arama & Analiz**: OpenSearch/Elasticsearch
- **Alarm & İzleme**: E-posta uyarıları (`AlarmService`), Logstash ile log toplama

## Modüller ve Veri Akış

1. **common**: Ortak model, DTO, mapper ve JPA repository.
2. **platform-tcp (PF1)**: TCP üzerinden gerçek zamanlı stream.
3. **platform-rest (PF2)**: REST API + SSE stream ile veri simülasyonu.
4. **coordinator**: Tüm sağlayıcıları yönetir, veri akışını Redis/Kafka'ya yazar, hesaplama ve alarm.
   - Raw veriyi Redis list (`raw:<rate>`) ve Kafka topic (`forex_topic`) gönderir.
   - Hesaplamaları tetikler, sonuçları Kafka'ya publish eder.
   - AlarmService ile gecikme izler ve e-posta uyarısı yollar.
5. **kafka-consumer**: Kafka'dan veriyi çekip PostgreSQL ve Elasticsearch'e yazar.
6. **logstash**: Log toplama ve Elasticsearch yönlendirme.
7. **docker-compose**: Tüm altyapı servislerini ayağa kaldırır.

Daha ayrıntılı mimari ve akış için [Project_Architecture.md](Project_Architecture.md) dosyasına bakın.

## PF2 (REST) Konfigürasyon
```yaml
pf2:
  rest:
    base-url: http://localhost:8082/api/rates/
    poll-interval: 1000    # ms, auto-poll aralığı
    enabled: false         # provider aktif/pasif
    manual-mode: false     # otomatik polling kapalı
```
- **enabled=false, manual-mode=false**: Otomatik polling kapalı, ancak `/api/manual/pf2/{symbol}` endpoint'inden manuel poll çalışır.
- **enabled=true, manual-mode=false**: Uygulama açılır açılız her `poll-interval` ms'de REST sorgusu atılır.
- **enabled=true, manual-mode=true**: Otomatik thread devre dışı, sadece manual endpoint ile veri alınır.

## PF1 (TCP) Konfigürasyon
```yaml
pf1:
  tcp:
    host: localhost
    port: 8081
    enabled: false      # otomatik TCP bağlantısı
```  
- **enabled=false** → `PF1TcpProvider.startProvider()` connect() çağırmaz.
- **enabled=true**  → Uygulama açılınca TCP socket açılır ve semboller subscribe edilir.

## Otomatik Polling Nedir?
Arka planda belirlenen `poll-interval` aralığında REST endpoint'e otomatik HTTP istek atmak ve güncel kurları almak demektir. Kodda:
```java
while(running) {
  pollOnce();
  Thread.sleep(pollInterval);
}
```
yapısı ile devam eder.

## Manuel Polling Endpoint
```http
GET /api/manual/pf2/{symbol}
```
Bu çağrı `PF2RestProvider.poll(symbol)` metodunu tetikler ve tek seferlik veri döner.

## Çalıştırma Adımları

1. **Önkoşullar**: Java 23, Maven 3.8+, Docker & Docker Compose
2. **Derleme**: `mvn clean install`
3. **Konfigürasyon**: `coordinator/src/main/resources/application.yml` içindeki `pf2.rest` ve `pf1.tcp` değerlerini güncelle.
4. **Başlatma**:
   - **Modül modunda**: PF1 → PF2 → Coordinator → Kafka Consumer (her biri kendi IDE'den veya script'le)
   - **Tek komutla**: `docker-compose up` (Kafka, Zookeeper, Redis, PostgreSQL, OpenSearch, Logstash)

   **Not:** Bu komut, her modül için ilgili klasördeki `Dockerfile` dosyasını kullanır. Geliştirme aşamasında `Dockerfile.dev` dosyalarını kullanmak isterseniz, `docker build -f <modül>/Dockerfile.dev .` komutuyla veya `docker-compose.override.yml` dosyası aracılığıyla manuel olarak belirtebilirsiniz.

5. **Test**:
   - Otomatik polling: `enabled=true` yapın, loglarda her saniye yeni sorgu göreceksiniz.
   - Manuel polling: `curl http://localhost:8080/api/manual/pf2/PF2_USDTRY`
6. **Veri Kontrolü**:
   - Redis: `redis-cli LRANGE raw:PF2_USDTRY 0 -1`
   - Kafka: `kafka-console-consumer --topic computed:PF2_USDTRY --from-beginning`

## Teknoloji Yığını

- Java 23, Spring Boot 3.4.2
- Maven 3.9.3
- Groovy 3.0.9 (dinamik hesaplamalar)
- Kafka (Confluent CP 7.x)
- Redis 7.x
- PostgreSQL 15-alpine
- OpenSearch 2.x / Dashboards
- Logstash 7.x
- Docker & Docker Compose 2.4

## Hızlı Başlangıç

1. **Önkoşullar**:
   - Java 23 yüklü
   - Maven 3.9+
   - Docker & Docker Compose kurulumu
2. **Projeyi Derle**:
   ```bash
   mvn clean install -DskipTests
   ```
3. **Docker Compose ile Başlat**:
   ```bash
   docker-compose up --build -d
   ```

   **Not:** Bu komut, her modül için ilgili klasördeki `Dockerfile` dosyasını kullanır. Geliştirme aşamasında `Dockerfile.dev` dosyalarını kullanmak isterseniz, `docker build -f <modül>/Dockerfile.dev .` komutuyla veya `docker-compose.override.yml` dosyası aracılığıyla manuel olarak belirtebilirsiniz.

4. **Portlara Erişim**:
   - PF2 REST API: `http://localhost:8082/api/rates/{rateName}`
   - PF2 SSE Stream: `http://localhost:8082/api/rates/stream/{rateName}`
   - PF1 TCP Simulator: (client uygulamanız üzerinden `subscribe|RATE` komutu)
   - Coordinator API: `http://localhost:8090/api/...`
   - OpenSearch Dashboards: `http://localhost:5601`

## Konfigürasyon

Tüm servisler `src/main/resources/application.yml` dosyaları ile yapılandırılır. Ortam değişkenleri kullanarak değerleri geçersiniz:

```bash
# Örnek
export SERVER_PORT=8090
export SPRING_PROFILES_ACTIVE=default
```

### Öne Çıkan Parametreler

| Modül           | Parametre                  | Açıklama                                             |
|-----------------|----------------------------|------------------------------------------------------|
| platform-rest   | rate.simulation.rates      | Simülenecek sembol listesi                           |
| platform-tcp    | tcp.streaming.port         | TCP mesaj portu                                       |
| coordinator     | pf2.rest.enabled           | REST provider aktif/pasif durumu                      |
| kafka-consumer  | spring.kafka.bootstrap-servers | Kafka broker adresi                             |

## API Endpointler

### platform-rest (PF2)
- `GET /api/rates/{rateName}` → Tek seferlik döviz kuru getirir.
- `GET /api/rates/stream/{rateName}` → SSE üzerinden sürekli güncelleme.

### coordinator
- `POST /api/manual/pf2/{rateName}` → Manuel polling.
- `GET /api/status` → Servis durum bilgisi.

## İzleme ve Loglama

- Loglar `logs/` klasörüne dökülür.
- Logstash pipeline: `logstash/pipeline/*.conf`.
- Elasticsearch/OpenSearch: `http://localhost:9200`
- Dashboards: `http://localhost:5601`

## Test ve QA

- Unit test: `mvn test`
- Integration test: `mvn verify -Pintegration-tests`
- Docker Compose test profile: `docker-compose -f docker-compose.yml -f docker-compose.test.yml up`

## Katkıda Bulunanlar

- Mesut Taha Güven (@mtggamer)
- Zertifikat Deutsch / telc Deutsch B1 (Almanca)
- B2 Seviye İngilizce

## Lisans

Bu proje [MIT Lisansı](LICENSE) ile lisanslanmıştır.
