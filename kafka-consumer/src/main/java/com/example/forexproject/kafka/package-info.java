/**
 * kafka-consumer modülü:
 * - Kafka topiclerinden döviz mesajlarını tüketir.
 * - Tüketilen verileri OpenSearch/Elasticsearch'e indeksler.
 *
 * Sınıflar:
 * - KafkaConsumerApplication.java: Spring Boot uygulamasını başlatan ana sınıf.
 * - ConsumerService.java: Kafka mesajlarını işleyip uygun formatta DTO'ya dönüştüren servis.
 * - ElasticConsumerService.java: İşlenen mesajları Elasticsearch/OpenSearch'e gönderip indeksleyen servis.
 *
 * config paketindeki sınıflar:
 * - KafkaConsumerConfig.java: Kafka consumer ayarlarını (topic, groupId vb.) yapılandırır.
 * - ElasticsearchConfig.java: OpenSearch/Elasticsearch istemci bağlantı ayarlarını tanımlar.
 */
package com.example.forexproject.kafka;
