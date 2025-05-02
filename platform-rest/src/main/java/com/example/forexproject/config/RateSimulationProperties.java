package com.example.forexproject.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for REST rate simulation (PF2).
 */
@Component
@ConfigurationProperties(prefix = "rate.simulation")
public class RateSimulationProperties {
    private List<String> rates = List.of("PF2_USDTRY", "PF2_EURUSD", "PF2_GBPUSD");
    private Map<String, Double> initialBid = Map.of(
        "PF2_USDTRY", 34.0,
        "PF2_EURUSD", 1.05,
        "PF2_GBPUSD", 0.80
    );
    private Map<String, Double> initialAsk = Map.of(
        "PF2_USDTRY", 35.0,
        "PF2_EURUSD", 1.07,
        "PF2_GBPUSD", 0.82
    );
    private double driftPercentage = 1.0;

    public List<String> getRates() { return rates; }
    public void setRates(List<String> rates) { this.rates = rates; }
    public Map<String, Double> getInitialBid() { return initialBid; }
    public void setInitialBid(Map<String, Double> initialBid) { this.initialBid = initialBid; }
    public Map<String, Double> getInitialAsk() { return initialAsk; }
    public void setInitialAsk(Map<String, Double> initialAsk) { this.initialAsk = initialAsk; }
    public double getDriftPercentage() { return driftPercentage; }
    public void setDriftPercentage(double driftPercentage) { this.driftPercentage = driftPercentage; }
}
