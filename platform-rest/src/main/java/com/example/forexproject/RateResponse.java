package com.example.forexproject;

/**
 * Döviz oranı bilgilerini temsil eden DTO sınıfı.
 */
public class RateResponse {
    private String rateName;
    private double bid;
    private double ask;
    private String timestamp;

    /**
     * Tam özellikli constructor.
     *
     * @param rateName Döviz sembolü adı.
     * @param bid      Teklif fiyatı.
     * @param ask      Alış fiyatı.
     * @param timestamp Oluşturulma zaman damgası.
     */
    public RateResponse(String rateName, double bid, double ask, String timestamp) {
        this.rateName = rateName;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }
    // Getter ve Setter metotları
    public String getRateName() { return rateName; }
    public void setRateName(String rateName) { this.rateName = rateName; }
    public double getBid() { return bid; }
    public void setBid(double bid) { this.bid = bid; }
    public double getAsk() { return ask; }
    public void setAsk(double ask) { this.ask = ask; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
