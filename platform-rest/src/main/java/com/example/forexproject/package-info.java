/**
 * platform-rest modülü:
 * - REST API uç noktalarını sunar.
 *
 * Sınıflar:
 * - RateRestApplication.java: Uygulamayı başlatan Spring Boot ana sınıfı.
 * - RateController.java: Döviz oranı ile ilgili HTTP isteklerini işleyen kontrolör.
 * - RateResponse.java: Client’a dönen döviz oranı yanıt modelini tanımlar.
 * - config/RateSimulationProperties.java: Örnek veri veya gecikme simülasyonu için yapılandırma sınıfı.
 * - exception/GlobalExceptionHandler.java: Uygulama genelindeki istisnaları yakalayıp HTTP yanıtları üretir.
 */
package com.example.forexproject;
