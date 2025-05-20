package com.example.forexproject.coordinator;

import com.example.forexproject.model.Rate;
import com.example.forexproject.model.RateFields;
import com.example.forexproject.model.RateStatus;

/**
 * Coordinator servisinin callback arayüzü; platform bağlantı, veri ve durum olaylarını tanımlar.
 */
public interface CoordinatorCallback {

    /**
     * Platform bağlantı durum değişikliğini bildirir.
     *
     * @param platformName Platform adı.
     * @param status       Bağlantı durumu (true=bağlandı, false=koptu).
     */
    void onConnect(String platformName, boolean status);

    /**
     * Platform bağlantı kopma durumunu bildirir.
     *
     * @param platformName Platform adı.
     * @param status       Bağlantı durumu (true=bağlandı, false=koptu).
     */
    void onDisconnect(String platformName, boolean status);

    /**
     * Yeni rate verisi geldiğinde çağrılır.
     *
     * @param platformName Platform adı.
     * @param rateName     Döviz sembolü.
     * @param rate         Rate model objesi.
     */
    void onRateAvailable(String platformName, String rateName, Rate rate);

    /**
     * Rate güncellemesi alındığında çağrılır.
     *
     * @param platformName Platform adı.
     * @param rateName     Döviz sembolü.
     * @param rateFields   Güncellenen alanları içeren RateFields objesi.
     */
    void onRateUpdate(String platformName, String rateName, RateFields rateFields);

    /**
     * Rate abonelik durum değişikliklerini bildirir.
     *
     * @param platformName Platform adı.
     * @param rateName     Döviz sembolü.
     * @param rateStatus   Rate status enum.
     */
    void onRateStatus(String platformName, String rateName, RateStatus rateStatus);
}
