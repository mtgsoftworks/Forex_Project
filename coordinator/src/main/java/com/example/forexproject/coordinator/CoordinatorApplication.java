package com.example.forexproject.coordinator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.example.forexproject.coordinator.config.ProviderProperties;

@SpringBootApplication
@EnableConfigurationProperties(ProviderProperties.class)
@EnableScheduling
@ConfigurationPropertiesScan
public class CoordinatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoordinatorApplication.class, args);
    }
}
