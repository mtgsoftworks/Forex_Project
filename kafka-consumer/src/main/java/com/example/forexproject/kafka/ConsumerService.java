package com.example.forexproject.kafka;

import com.example.forexproject.model.RateEntity;
import com.example.forexproject.repository.RateRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ConsumerService {
    private static final Logger logger = LogManager.getLogger(ConsumerService.class);

    @Autowired
    private RateRepository rateRepository;

    @KafkaListener(topics = "forex_topic", groupId = "forex_group")
    public void listen(String message) {
        logger.info("Received Kafka Message: {}", message);
        try {
            String[] parts = message.split("\\|");
            if (parts.length == 4) {
                String rateName = parts[0];
                double bid = Double.parseDouble(parts[1]);
                double ask = Double.parseDouble(parts[2]);
                String rateTimestamp = parts[3];

                RateEntity rateEntity = new RateEntity();
                rateEntity.setRateName(rateName);
                rateEntity.setBid(bid);
                rateEntity.setAsk(ask);
                rateEntity.setRateUpdateTime(LocalDateTime.parse(rateTimestamp));
                rateEntity.setDbUpdateTime(LocalDateTime.now());

                rateRepository.save(rateEntity);
                logger.info("Rate saved to DB: {}", rateName);
            } else {
                logger.warn("Unexpected message format: {}", message);
            }
        } catch (Exception e) {
            logger.error("Error processing Kafka message: {}", e.getMessage(), e);
        }
    }
}
