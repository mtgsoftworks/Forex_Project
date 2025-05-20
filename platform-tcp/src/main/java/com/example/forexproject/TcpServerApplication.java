package com.example.forexproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Spring Boot TCP sunucusu uygulamasını başlatan ana sınıf.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ConfigurationPropertiesScan
public class TcpServerApplication {
    private static final Logger logger = LogManager.getLogger(TcpServerApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TcpServerApplication.class, args);
        TcpServer server = context.getBean(TcpServer.class);
        try {
            new Thread(server).start();
        } catch (Exception e) {
            logger.error("Failed to start TcpServer: {}", e.getMessage(), e);
        }
    }
}
