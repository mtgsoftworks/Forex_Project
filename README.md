# Forex Projesi - Teknik Dokümantasyon

## Genel Bakış
Bu proje, farklı veri sağlayıcı platformlarından (PF1, PF2) döviz piyasası verilerini toplayıp entegre eden, bu veriler üzerinden dinamik ve statik hesaplamalar yaparak sonuçları Redis ve Kafka'ya yayınlayan modüler bir mimari sunar.

## Mimari Bileşenler

1. **PF1 (TCP Streaming Simulator)**
   - Teknoloji: Standalone Java uygulaması
   - Port: 8081 (TCP Streaming)
   - Yapılandırma: `application.properties`
     - `tcp.streaming.port`: Dinleme portu
     - `tcp.streaming.rates`: Abone olunacak rate listesi
     - `tcp.streaming.initialBid/Ask`: Başlangıç değerleri
     - `tcp.streaming.messageInterval`: Mesaj intervali (ms)
     - `tcp.streaming.messageCount`: Gönderilecek mesaj sayısı
     - `tcp.streaming.driftPercentage`: Fiyat sapması yüzdesi
   - Komutlar: `subscribe|RATE`, `unsubscribe|RATE`
   - Mesaj Formatı: `RATE_NAME|<bid>|<ask>|<timestamp>`

2. **PF2 (REST API Simulator)**
   - Teknoloji: Spring Boot uygulaması
   - Port: 8080 (HTTP)
   - Yapılandırma: `application.yml`
     ```yaml
     pf2:
       rest:
         base-url: http://localhost:8080/api/rates/
         poll-interval: 1000
     ```
   - Servis Sınıfları:
     - `Pf2RestProperties`: REST ayarları
     - `PF2RestProvider`: `DataProvider` implementasyonu, `RestTemplate` ile periyodik polling

3. **Coordinator (Orkestrasyon Servisi)**
   - Teknoloji: Spring Boot
   - Görev: Tüm `DataProvider`'ları yönetir, callback ile gelen verileri işler.
   - Ana Sınıflar:
     - `CoordinatorService`: `CoordinatorCallback` implementasyonu, Redis ve Kafka entegrasyonu, hesaplama akışı
     - `CalculationService`: Hem statik hem dinamik (Groovy script) hesaplama desteği
   - Yapılandırma: `application.yml` (Kafka topic, Redis bağlantı bilgileri)
   - Logging: Log4j2

4. **Kafka Consumer**
   - Teknoloji: Spring Kafka Consumer
   - Görev: Kafka'dan gelen mesajları alıp DB yazarı ve log indexer görevlerini gerçekleştirir.

## Hesaplama Mekanizması

- **Statik Metodlar:** `prepareUsdTryMessage`, `prepareEurTryMessage`, `prepareGbpTryMessage`
- **Dinamik Metod:** `computeDynamicCalculation` (Groovy JSR223 script engine)
- **Formüller:** `FormulaService` üzerinden yüklenir; kolayca güncellenebilir ve yeni formüller eklenebilir.

## Yapılandırma Yönetimi

- `application.yml` dosyası modüllere göre ayrılmıştır:
  - **platform-tcp**: TCP streaming ayarları
  - **platform-rest**: REST polling ayarları
  - **coordinator**: Kafka, Redis, hesaplama ayarları
- `@ConfigurationProperties` ile değerler Spring konteynerine enjekte edilir.

## Çalıştırma Talimatları

1. **Önkoşullar:**
   - Java 23
   - Maven 3.8+
   - Kafka & Zookeeper
   - Redis
2. **Derleme ve Test:**
   ```bash
   mvn clean install
   ```
3. **Modül Başlatma Sırası:**
   1. PF1 (TCP Streaming)
   2. PF2 (REST API)
   3. Coordinator (Orkestrasyon)
   4. Kafka Consumer
4. **Docker Compose (isteğe bağlı):**
   `docker-compose.yml` ile Kafka ve Redis servisleri ayağa kaldırılabilir.

## Testler

- **Birim Testleri:**
  - `PF2RestProviderTest`
  - `CalculationServiceTest`
- **Entegrasyon Testleri:**
  - `CoordinatorServicePF2IntegrationTest`

## Katkıda Bulunanlar

- MTG Softworks

---

Commit ve push işlemi için aşağıdaki komutları kullanabilirsiniz:
```bash
git add README.md
git commit -m "README.md güncellendi: Türkçe teknik dokümantasyon eklendi"
git push origin main
