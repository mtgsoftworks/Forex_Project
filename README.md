# Forex Projesi

## Genel Bakış
Bu proje, farklı veri sağlayıcı platformlarından (PF1: TCP Streaming ve PF2: REST API) alınan döviz kuru verilerini modüler bir şekilde işleyen, dinamik hesaplama ve izleme özelliklerine sahip bir mimaridir.

- **Ham Veri**: Redis List (`raw:<rate>`)
- **Hesaplanmış Veri**: Kafka Topic (`computed:<symbol>`), örn: `computed:USDTRY`, `computed:EURUSD`, `computed:GBPUSD`
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

## Gereksinimler

Projeyi çalıştırmak için aşağıdaki yazılımları bilgisayarınıza kurmanız gerekmektedir:

- **Java 23**: [Oracle JDK 23](https://www.oracle.com/java/technologies/javase/jdk23-archive-downloads.html) veya [OpenJDK 23](https://openjdk.java.net/projects/jdk/23/)
- **Maven 3.8+**: [Maven İndirme Sayfası](https://maven.apache.org/download.cgi)
- **Docker**: [Windows](https://docs.docker.com/desktop/install/windows-install/), [Mac](https://docs.docker.com/desktop/install/mac-install/), [Linux](https://docs.docker.com/engine/install/)
- **Docker Compose**: Docker Desktop ile birlikte gelir veya [manuel kurulum](https://docs.docker.com/compose/install/)
- **Git**: [Git İndirme Sayfası](https://git-scm.com/downloads)

## Kurulum

### 1. Projeyi Klonlama

```bash
# GitHub'dan projeyi klonlayın
git clone https://github.com/mtgsoftworks/Forex_Project.git
cd Forex_Project
```

### 2. Maven ile Derleme

```bash
# Test'leri atlamak için -DskipTests parametresi kullanılır
mvn clean install -DskipTests
```

### 3. Docker Compose ile Çalıştırma

```bash
# Tüm servisleri başlatır
docker-compose up -d

# Logları takip etmek için
docker-compose logs -f
```

### 4. Manuel Olarak Modülleri Çalıştırma

Her bir modülü ayrı terminalde çalıştırmak için:

```bash
# Sırasıyla servisleri başlatın
cd platform-tcp
mvn spring-boot:run

# Yeni bir terminal açıp
cd platform-rest
mvn spring-boot:run

# Yeni bir terminal açıp
cd coordinator
mvn spring-boot:run

# Yeni bir terminal açıp
cd kafka-consumer
mvn spring-boot:run
```

## Konfigürasyon Detayları

### PF2 (REST) Konfigürasyon
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

### PF1 (TCP) Konfigürasyon
```yaml
pf1:
  tcp:
    host: localhost
    port: 8081
    enabled: false      # otomatik TCP bağlantısı
```  
- **enabled=false** → `PF1TcpProvider.startProvider()` connect() çağırmaz.
- **enabled=true**  → Uygulama açılınca TCP socket açılır ve semboller subscribe edilir.

### Konfigürasyon Dosyalarını Düzenleme

Her modülün konfigürasyon dosyasına erişmek için:

```
coordinator/src/main/resources/application.yml
platform-tcp/src/main/resources/application.yml
platform-rest/src/main/resources/application.yml
kafka-consumer/src/main/resources/application.yml
```

### Otomatik Polling Nedir?
Arka planda belirlenen `poll-interval` aralığında REST endpoint'e otomatik HTTP istek atmak ve güncel kurları almak demektir. Kodda:
```java
while(running) {
  pollOnce();
  Thread.sleep(pollInterval);
}
```
yapısı ile devam eder.

## Kullanım Kılavuzu

### 1. Sistemin Çalıştığını Doğrulama

Tüm servisler başlatıldıktan sonra aşağıdaki kontroller yapılabilir:

```bash
# Docker servisleri kontrolü
docker-compose ps

# Kafka'nın çalıştığını kontrol etme
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Redis'in çalıştığını kontrol etme
docker exec -it redis redis-cli ping
```

### 2. Manuel REST Sorgusu Yapma

```bash
# Coordinator servisine sorgu yapma
curl http://localhost:8080/api/manual/pf2/PF2_USDTRY
```

### 3. TCP Bağlantısı İçin Test Client Örneği

TCP test için basit bir Java client örneği:

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
            
            // TCP protokolüne göre abone olma komutu
            out.println("subscribe|PF1_USDTRY");
            
            // Gelen mesajları okuma
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

### 4. Veri Kontrolü

```bash
# Redis'ten raw veri okuma
docker exec -it redis redis-cli LRANGE raw:PF2_USDTRY 0 -1

# Kafka'dan computed veri okuma
docker exec -it kafka kafka-console-consumer --topic computed:USDTRY --from-beginning --bootstrap-server localhost:9092
docker exec -it kafka kafka-console-consumer --topic computed:EURUSD --from-beginning --bootstrap-server localhost:9092
docker exec -it kafka kafka-console-consumer --topic computed:GBPUSD --from-beginning --bootstrap-server localhost:9092
```

### 5. Dashboards ve İzleme

- OpenSearch Dashboards: `http://localhost:5601`
  - Kullanıcı: admin
  - Şifre: admin

## Sorun Giderme

### Genel Sorunlar ve Çözümleri

1. **Port Çakışması Sorunu**
   ```
   Error: Port 8080 is already in use
   ```
   Çözüm: İlgili portu kullanan uygulamayı bulup durdurun veya konfigürasyon dosyasından portu değiştirin.

2. **Docker Compose Bağlantı Hatası**
   ```
   Connection refused to kafka:9092
   ```
   Çözüm: Docker Compose'un tüm servisleri başlattığından emin olun ve ağ konfigürasyonunu kontrol edin.

3. **Redis Bağlantı Sorunu**
   ```
   JedisConnectionException: Could not connect to redis:16379
   ```
   Çözüm: Redis konteynerinin çalıştığını ve doğru port'un açık olduğunu kontrol edin.

4. **Java Sürüm Uyumsuzluğu**
   ```
   UnsupportedClassVersionError: ... requires Java 23
   ```
   Çözüm: JDK 23 kurulu olduğundan ve `JAVA_HOME` değişkeninin doğru ayarlandığından emin olun.

## API Referansı

### platform-rest (PF2)
- `GET /api/rates/{rateName}` → Tek seferlik döviz kuru getirir.
- `GET /api/rates/stream/{rateName}` → SSE üzerinden sürekli güncelleme.

### coordinator
- `GET /api/manual/pf2/{rateName}` → Manuel polling.
- `GET /api/status` → Servis durum bilgisi.

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

## Ek Kaynaklar

- [Kafka Dokümantasyonu](https://kafka.apache.org/documentation/)
- [Redis Komutları](https://redis.io/commands)
- [Spring Boot Referansı](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [OpenSearch Kılavuzu](https://opensearch.org/docs/latest/)

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

## Lisans

Bu proje [MIT Lisansı](LICENSE) ile lisanslanmıştır.
