package com.example.forexproject.model;

public class Rate {
    private String rateName;
    private double bid;
    private double ask;
    private String timestamp;
    // Getter/Setter
    public String getRateName() { return rateName; }
    public void setRateName(String rateName) { this.rateName = rateName; }
    public double getBid() { return bid; }
    public void setBid(double bid) { this.bid = bid; }
    public double getAsk() { return ask; }
    public void setAsk(double ask) { this.ask = ask; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
