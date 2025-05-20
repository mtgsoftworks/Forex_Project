package com.example.forexproject.coordinator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.forexproject.coordinator.config.AlarmProperties;

/**
 * Coordinator service for handling alarms.
 */
@Service
public class AlarmService {
    private static final Logger logger = LogManager.getLogger(AlarmService.class);

    // Her platform için son yanıt zamanlarını tutan thread-safe map
    private Map<String, LocalDateTime> lastResponseMap = new ConcurrentHashMap<>();

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AlarmProperties alarmProperties;

    @PostConstruct
    public void init() {
        logger.info("AlarmService initialized: enabled={}, interval={}ms, threshold={}s, recipient={}",
            alarmProperties.isEnabled(), alarmProperties.getCheckInterval(), alarmProperties.getThresholdSeconds(),
            alarmProperties.getRecipientEmail());
    }

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
    @Scheduled(fixedRateString = "${coordinator.alarm.check-interval}")
    public void checkPlatformResponses() {
        if (!alarmProperties.isEnabled()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        logger.info("[AlarmService] scheduled check at {} - tracking platforms: {}", now, lastResponseMap.keySet());
        for (Map.Entry<String, LocalDateTime> entry : lastResponseMap.entrySet()) {
            Duration duration = Duration.between(entry.getValue(), now);
            long threshold = alarmProperties.getThresholdSeconds();
            if (duration.getSeconds() > threshold) {
                String msg = "ALERT: No response from " + entry.getKey() + " for " + duration.getSeconds() + " seconds.";
                System.out.println(msg);
                logger.warn(msg);
                sendAlarmEmail(entry.getKey(), duration.getSeconds());
            }
        }
    }

    public void sendAlarmEmail(String platformName, long seconds) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alarmProperties.getRecipientEmail());  // Configured recipient email
            message.setSubject("Platform Response Alarm - " + platformName);
            message.setText("No response from platform " + platformName + " for " + seconds + " seconds.");
            mailSender.send(message);
            System.out.println("Mail gönderildi: Platform " + platformName + " için alarm e-postası başarıyla gönderildi.");
            logger.info("Alarm email sent for {}.", platformName);
        } catch (Exception e) {
            System.out.println("Mail gönderilemedi: " + e.getMessage());
            logger.error("Failed to send alarm email: {}", e.getMessage());
        }
    }
}
