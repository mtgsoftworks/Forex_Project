package com.example.forexproject.coordinator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tcp.streaming")
public class TcpStreamingProperties {
    private int port = 8081;
    // Add more fields as needed for future config

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
}
