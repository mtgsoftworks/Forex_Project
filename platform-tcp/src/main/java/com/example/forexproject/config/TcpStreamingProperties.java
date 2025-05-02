package com.example.forexproject.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Configuration properties for the TCP Streaming Simulator (PF1).
 */
@Component
@ConfigurationProperties(prefix = "tcp.streaming")
public class TcpStreamingProperties {
    private int port = 8081;
    private long messageInterval = 1000;
    private int messageCount = 10;
    private double driftPercentage = 1.0;
    private List<String> rates = List.of("PF1_USDTRY", "PF1_EURUSD", "PF1_GBPUSD");
    private Map<String, Double> initialBid = Map.of(
        "PF1_USDTRY", 34.0,
        "PF1_EURUSD", 1.05,
        "PF1_GBPUSD", 0.80
    );
    private Map<String, Double> initialAsk = Map.of(
        "PF1_USDTRY", 35.0,
        "PF1_EURUSD", 1.07,
        "PF1_GBPUSD", 0.82
    );
    private String welcomeMessage = "Welcome to the Forex TCP Server. Available commands: subscribe|RATE, unsubscribe|RATE";

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public long getMessageInterval() { return messageInterval; }
    public void setMessageInterval(long messageInterval) { this.messageInterval = messageInterval; }

    public int getMessageCount() { return messageCount; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }

    public double getDriftPercentage() { return driftPercentage; }
    public void setDriftPercentage(double driftPercentage) { this.driftPercentage = driftPercentage; }

    public List<String> getRates() { return rates; }
    public void setRates(List<String> rates) { this.rates = rates; }

    public Map<String, Double> getInitialBid() { return initialBid; }
    public void setInitialBid(Map<String, Double> initialBid) { this.initialBid = initialBid; }

    public Map<String, Double> getInitialAsk() { return initialAsk; }
    public void setInitialAsk(Map<String, Double> initialAsk) { this.initialAsk = initialAsk; }

    public String getWelcomeMessage() { return welcomeMessage; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }
}
