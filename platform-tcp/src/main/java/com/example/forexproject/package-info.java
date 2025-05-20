/**
 * platform-tcp modülü:
 * - TCP üzerinden gerçek zamanlı döviz akışını sağlayan sunucu uygulaması.
 *
 * Sınıflar:
 * - com.example.forexproject.TcpServerApplication: Uygulamayı başlatan Spring Boot ana sınıfı.
 * - com.example.forexproject.TcpServer: Gelen TCP bağlantılarını dinleyen ve ham döviz mesajlarını sağlayan bileşen.
 * - com.example.forexproject.config.TcpStreamingProperties: TCP akış parametreleri (host, port vb.) için yapılandırma sınıfı.
 */
package com.example.forexproject;
