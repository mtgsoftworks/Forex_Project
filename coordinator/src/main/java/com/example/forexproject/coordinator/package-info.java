/**
 * coordinator modülü:
 * - Orkestrasyon servisi; tüm DataProvider’leri yönetir ve callback mekanizması ile veri akışını kontrol eder.
 * - Ham veriyi lokal cache, Redis list (raw:<rate>) ve Kafka topic’e (forex_topic) yazar.
 * - Statik hesaplamalar (FormulaService) ve dinamik Groovy script hesaplamalarını (CalculationService) tetikler.
 * - AlarmService ile platform gecikmelerini izler, eşiği aşan durumlarda aboneliği iptal edip e-posta uyarısı yollar.
 *
 * config:
 * - ProviderProperties: dinamik DataProvider sınıflarını konfigürasyondan okur.
 * - PF1TcpProperties / Pf2RestProperties: PF1 ve PF2 için host/port/pollInterval/enabled/manual-mode ayarları.
 * - CoordinatorKafkaProperties: Kafka topic ve broker ayarları.
 * - RedisConfig, KafkaProducerConfig, RestTemplateConfig: ilgili bean konfigürasyonları.
 *
 * controller paket:
 * - RatePushController: PF2 push endpoint’i.
 * - ManualController: manuel polling endpoint’i (/api/manual/pf2/{symbol}).
 *
 * provider paket:
 * - DataProvider: connect, disconnect, subscribe/unSubscribe, run metodlarını tanımlayan arayüz.
 * - PF1TcpProvider, PF2RestProvider: PF1 TCP ve PF2 REST sağlayıcı implementasyonları.
 *
 * service paket:
 * - CoordinatorService: init, callback (onRateAvailable/Update/Status) işlemleri.
 * - AlarmService: e-posta alarm mekanizması.
 * - CalculationService: formül ve script hesaplamaları.
 * - FormulaService: Groovy script’leri yönetme ve yükleme.
 */
package com.example.forexproject.coordinator;
