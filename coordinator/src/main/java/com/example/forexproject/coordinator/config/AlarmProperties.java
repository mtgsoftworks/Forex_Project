package com.example.forexproject.coordinator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for AlarmService.
 */
@Component
@ConfigurationProperties(prefix = "coordinator.alarm")
public class AlarmProperties {
    /** Enable or disable the alarm checking */
    private boolean enabled = true;

    /** Interval in milliseconds between each alarm check */
    private long checkInterval = 10000;

    /** Threshold in seconds after last response to trigger alarm */
    private long thresholdSeconds = 30;

    /** Recipient email address for alarm notifications */
    private String recipientEmail;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public long getThresholdSeconds() {
        return thresholdSeconds;
    }

    public void setThresholdSeconds(long thresholdSeconds) {
        this.thresholdSeconds = thresholdSeconds;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
}