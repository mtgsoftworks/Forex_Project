package com.example.forexproject.kafka;

import com.example.forexproject.model.RawRateEntity;
import com.example.forexproject.model.CalculatedRateEntity;
import com.example.forexproject.repository.RawRateRepository;
import com.example.forexproject.repository.CalculatedRateRepository;
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
    private RawRateRepository rawRateRepository;
    @Autowired
    private CalculatedRateRepository calculatedRateRepository;

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
                boolean isRaw = rateName.startsWith("PF1_") || rateName.startsWith("PF2_");
                if (isRaw) {
                    RawRateEntity raw = new RawRateEntity();
                    raw.setRateName(rateName);
                    raw.setBid(bid);
                    raw.setAsk(ask);
                    raw.setRateUpdateTime(LocalDateTime.parse(rateTimestamp));
                    raw.setDbUpdateTime(LocalDateTime.now());
                    rawRateRepository.save(raw);
                    logger.info("Raw rate saved to DB: {}", rateName);
                } else {
                    rateName = "calculated_" + rateName;
                    CalculatedRateEntity calc = new CalculatedRateEntity();
                    calc.setRateName(rateName);
                    calc.setBid(bid);
                    calc.setAsk(ask);
                    calc.setRateUpdateTime(LocalDateTime.parse(rateTimestamp));
                    calc.setDbUpdateTime(LocalDateTime.now());
                    calculatedRateRepository.save(calc);
                    logger.info("Calculated rate saved to DB: {}", rateName);
                }
            } else {
                logger.warn("Unexpected message format: {}", message);
            }
        } catch (Exception e) {
            logger.error("Error processing Kafka message: {}", e.getMessage(), e);
        }
    }
}
