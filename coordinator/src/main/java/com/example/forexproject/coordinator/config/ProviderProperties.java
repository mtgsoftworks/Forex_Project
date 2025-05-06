package com.example.forexproject.coordinator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for dynamic extra DataProvider classes.
 */
@Component
@ConfigurationProperties(prefix = "coordinator.providers")
public class ProviderProperties {
    /** Fully qualified class names of additional DataProvider implementations to load */
    private List<String> classes;

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }
}
