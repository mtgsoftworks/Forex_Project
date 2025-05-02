package com.example.forexproject.coordinator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlarmService {
    private static final Logger logger = LogManager.getLogger(AlarmService.class);

    // Her platform için son yanıt zamanlarını tutan thread-safe map
    private Map<String, LocalDateTime> lastResponseMap = new ConcurrentHashMap<>();

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Platformdan yeni veri geldiğinde çağrılır.
     *
     * @param platformName Platformın adı
     */
    public void updateLastResponse(String platformName) {
        lastResponseMap.put(platformName, LocalDateTime.now());
    }

    /**
     * Her 10 saniyede bir, her platform için son yanıt süresini kontrol eder.
     * 30 saniyeden uzun süre yanıt alınamadıysa alarm maili gönderir.
     */
    @Scheduled(fixedRate = 10000)
    public void checkPlatformResponses() {
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, LocalDateTime> entry : lastResponseMap.entrySet()) {
            Duration duration = Duration.between(entry.getValue(), now);
            if (duration.getSeconds() > 30) {
                sendAlarmEmail(entry.getKey(), duration.getSeconds());
                logger.warn("ALERT: No response from {} for {} seconds.", entry.getKey(), duration.getSeconds());
            }
        }
    }

    public void sendAlarmEmail(String platformName, long seconds) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("mtggamer51@gmail.com");  // Alıcı e-posta adresini buraya yazın
            message.setSubject("Platform Response Alarm - " + platformName);
            message.setText("No response from platform " + platformName + " for " + seconds + " seconds.");
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send alarm email: {}", e.getMessage());
        }
    }
}
