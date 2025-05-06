package com.example.forexproject.coordinator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for coordinator Kafka topics.
 */
@Component
@ConfigurationProperties(prefix = "coordinator.kafka-topic")
public class CoordinatorKafkaProperties {

    /** Default topic for forex messages */
    private String forex = "forex_topic";

    public String getForex() {
        return forex;
    }

    public void setForex(String forex) {
        this.forex = forex;
    }
}
