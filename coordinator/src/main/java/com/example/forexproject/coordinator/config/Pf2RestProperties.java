package com.example.forexproject.coordinator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for PF2 REST provider polling and base URL.
 */
@Component
@ConfigurationProperties(prefix = "pf2.rest")
public class Pf2RestProperties {

    /** Base URL of the PF2 REST simulation service */
    private String baseUrl = "http://localhost:8080/api/rates/";

    /** Poll interval in milliseconds */
    private long pollInterval = 1000;

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
}
