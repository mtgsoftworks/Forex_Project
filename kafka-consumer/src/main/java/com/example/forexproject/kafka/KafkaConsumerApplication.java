package com.example.forexproject.kafka;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.forexproject")
@ConfigurationPropertiesScan
@EntityScan("com.example.forexproject.model")  // Entity'lerinizin bulunduğu paket
@EnableJpaRepositories(basePackages = "com.example.forexproject.repository")
@EnableScheduling


public class KafkaConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaConsumerApplication.class, args);
    }
}
