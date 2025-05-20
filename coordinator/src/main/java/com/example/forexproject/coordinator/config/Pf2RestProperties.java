package com.example.forexproject.coordinator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Configuration properties for PF2 REST provider polling and base URL.
 */
@Component
@ConfigurationProperties(prefix = "pf2.rest")
public class Pf2RestProperties {

    /** Base URL of the PF2 REST simulation service */
    private String baseUrl = "http://localhost:8082/api/rates/";

    /** Poll interval in milliseconds */
    private long pollInterval = 1000;

    /** Whether to auto-start polling */
    private boolean enabled = false;

    /** Enable manual polling instead of auto-poller */
    private boolean manualMode = false;

    /** Symbols to subscribe for REST provider */
    private List<String> rates = List.of("PF2_USDTRY", "PF2_EURUSD", "PF2_GBPUSD");

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isManualMode() {
        return manualMode;
    }

    public void setManualMode(boolean manualMode) {
        this.manualMode = manualMode;
    }

    public List<String> getRates() { 
        return rates; 
    }

    public void setRates(List<String> rates) { 
        this.rates = rates; 
    }
}
