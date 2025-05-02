package com.example.forexproject.model;

public class ForexData {
    private String fromCurrency;
    private String toCurrency;
    private double exchangeRate;
    private String lastRefreshed;
    // Getter/Setter
    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
    public double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }
    public String getLastRefreshed() { return lastRefreshed; }
    public void setLastRefreshed(String lastRefreshed) { this.lastRefreshed = lastRefreshed; }
}
