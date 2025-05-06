package com.example.forexproject.coordinator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Configuration properties for the PF1 TCP streaming simulator.
 */
@Component
@ConfigurationProperties(prefix = "pf1.tcp")
public class PF1TcpProperties {
    /**
     * Hostname or IP of the PF1 TCP streaming simulator.
     */
    private String host = "localhost";

    /**
     * Port of the PF1 TCP streaming simulator.
     */
    private int port = 8081;

    /**
     * Symbols to subscribe for TCP provider
     */
    private List<String> rates = List.of("PF1_USDTRY", "PF1_EURUSD", "PF1_GBPUSD");

    /**
     * Whether to auto-start PF1 TCP provider
     */
    private boolean enabled = false;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<String> getRates() {
        return rates;
    }

    public void setRates(List<String> rates) {
        this.rates = rates;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
