package com.example.forexproject.coordinator.service;

import com.example.forexproject.coordinator.CoordinatorCallback;
import com.example.forexproject.coordinator.provider.DataProvider;
import com.example.forexproject.model.Rate;
import com.example.forexproject.model.RateFields;
import com.example.forexproject.model.RateStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.forexproject.coordinator.service.CalculationService;
import com.example.forexproject.coordinator.service.AlarmService;

@Service
public class CoordinatorService implements CoordinatorCallback {

    private static final Logger logger = LogManager.getLogger(CoordinatorService.class);

    // Thread-safe local cache for storing rates
    private Map<String, Rate> localCache = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private CalculationService calculationService;
    
    @Autowired
    private AlarmService alarmService;

    @Value("${kafka.topic.forex:forex_topic}")
    private String forexTopic;

    // Autowired Spring-managed data providers
    @Autowired
    private List<DataProvider> dataProviders;

    @PostConstruct
    public void initDataProviders() {
        for (DataProvider provider : dataProviders) {
            provider.setCallback(this);
            provider.startProvider();
            logger.info("Loaded and started DataProvider: {}", provider.getClass().getSimpleName());
        }
    }

    @Override
    public void onConnect(String platformName, boolean status) {
        logger.info("Connected to {}: {}", platformName, status);
    }

    @Override
    public void onDisconnect(String platformName, boolean status) {
        logger.info("Disconnected from {}: {}", platformName, status);
    }

    @Override
    public void onRateAvailable(String platformName, String rateName, Rate rate) {
        logger.info("Rate available from {}: {}", platformName, rateName);
        alarmService.updateLastResponse(platformName);
        localCache.put(rateName, rate);
        // Persist raw rate to Redis and Kafka
        String message = formatRateMessage(rate);
        redisTemplate.opsForValue().set("raw:" + rateName, message);
        sendRateToKafka(message);
        processComputedRates();
    }

    @Override
    public void onRateUpdate(String platformName, String rateName, RateFields rateFields) {
        logger.info("Rate update from {}: {}", platformName, rateName);
        alarmService.updateLastResponse(platformName);
        Rate existingRate = localCache.get(rateName);
        if (existingRate == null) {
            existingRate = new Rate();
            existingRate.setRateName(rateName);
            localCache.put(rateName, existingRate);
        }
        existingRate.setBid(rateFields.getBid());
        existingRate.setAsk(rateFields.getAsk());
        existingRate.setTimestamp(LocalDateTime.now().toString());
        String updateMessage = formatRateMessage(existingRate);
        redisTemplate.opsForValue().set("raw:" + rateName, updateMessage);
        sendRateToKafka(updateMessage);
        processComputedRates();
    }

    @Override
    public void onRateStatus(String platformName, String rateName, RateStatus rateStatus) {
        logger.debug("Rate status from {}: {} - {}", platformName, rateName, rateStatus);
    }

    /**
     * Processes computed rates (USDTRY, EURTRY, GBPTRY) if data from both platforms is available.
     */
    private void processComputedRates() {
        // USD/TRY computation (dynamic if script available)
        Rate pf1_usdtry = localCache.get("PF1_USDTRY");
        Rate pf2_usdtry = localCache.get("PF2_USDTRY");
        if (pf1_usdtry != null && pf2_usdtry != null) {
            String timestamp = pf1_usdtry.getTimestamp();
            String usdtryMessage;
            try {
                Map<String, Object> vars = Map.of(
                    "pf1UsdBid", pf1_usdtry.getBid(),
                    "pf2UsdBid", pf2_usdtry.getBid(),
                    "pf1UsdAsk", pf1_usdtry.getAsk(),
                    "pf2UsdAsk", pf2_usdtry.getAsk(),
                    "timestamp", timestamp
                );
                usdtryMessage = calculationService.computeDynamicCalculation("USDTRY", vars);
            } catch (Exception ex) {
                usdtryMessage = calculationService.prepareUsdTryMessage(
                    pf1_usdtry.getBid(), pf2_usdtry.getBid(),
                    pf1_usdtry.getAsk(), pf2_usdtry.getAsk(),
                    timestamp
                );
            }
            redisTemplate.opsForValue().set("computed:USDTRY", usdtryMessage);
            sendRateToKafka(usdtryMessage);
        }
        // EUR/TRY computation
        Rate pf1_eurusd = localCache.get("PF1_EURUSD");
        Rate pf2_eurusd = localCache.get("PF2_EURUSD");
        if (pf1_usdtry != null && pf2_usdtry != null 
                && pf1_eurusd != null && pf2_eurusd != null) {
            String timestamp = pf1_usdtry.getTimestamp();
            String eurtryMessage;
            try {
                Map<String, Object> vars = Map.of(
                    "pf1UsdBid", pf1_usdtry.getBid(),
                    "pf2UsdBid", pf2_usdtry.getBid(),
                    "pf1UsdAsk", pf1_usdtry.getAsk(),
                    "pf2UsdAsk", pf2_usdtry.getAsk(),
                    "pf1EurUsdBid", pf1_eurusd.getBid(),
                    "pf2EurUsdBid", pf2_eurusd.getBid(),
                    "pf1EurUsdAsk", pf1_eurusd.getAsk(),
                    "pf2EurUsdAsk", pf2_eurusd.getAsk(),
                    "timestamp", timestamp
                );
                eurtryMessage = calculationService.computeDynamicCalculation("EURTRY", vars);
            } catch (Exception ex) {
                eurtryMessage = calculationService.prepareEurTryMessage(
                    pf1_usdtry.getBid(), pf2_usdtry.getBid(),
                    pf1_usdtry.getAsk(), pf2_usdtry.getAsk(),
                    pf1_eurusd.getBid(), pf2_eurusd.getBid(),
                    pf1_eurusd.getAsk(), pf2_eurusd.getAsk(),
                    timestamp
                );
            }
            redisTemplate.opsForValue().set("computed:EURTRY", eurtryMessage);
            sendRateToKafka(eurtryMessage);
        }
        // GBP/TRY computation
        Rate pf1_gbpusd = localCache.get("PF1_GBPUSD");
        Rate pf2_gbpusd = localCache.get("PF2_GBPUSD");
        if (pf1_usdtry != null && pf2_usdtry != null 
                && pf1_gbpusd != null && pf2_gbpusd != null) {
            String timestamp = pf1_usdtry.getTimestamp();
            String gbptryMessage;
            try {
                Map<String, Object> vars = Map.of(
                    "pf1UsdBid", pf1_usdtry.getBid(),
                    "pf2UsdBid", pf2_usdtry.getBid(),
                    "pf1UsdAsk", pf1_usdtry.getAsk(),
                    "pf2UsdAsk", pf2_usdtry.getAsk(),
                    "pf1GbpUsdBid", pf1_gbpusd.getBid(),
                    "pf2GbpUsdBid", pf2_gbpusd.getBid(),
                    "pf1GbpUsdAsk", pf1_gbpusd.getAsk(),
                    "pf2GbpUsdAsk", pf2_gbpusd.getAsk(),
                    "timestamp", timestamp
                );
                gbptryMessage = calculationService.computeDynamicCalculation("GBPTRY", vars);
            } catch (Exception ex) {
                gbptryMessage = calculationService.prepareGbpTryMessage(
                    pf1_usdtry.getBid(), pf2_usdtry.getBid(),
                    pf1_usdtry.getAsk(), pf2_usdtry.getAsk(),
                    pf1_gbpusd.getBid(), pf2_gbpusd.getBid(),
                    pf1_gbpusd.getAsk(), pf2_gbpusd.getAsk(),
                    timestamp
                );
            }
            redisTemplate.opsForValue().set("computed:GBPTRY", gbptryMessage);
            sendRateToKafka(gbptryMessage);
        }
    }

    // Helper to format rate message for Kafka/Redis
    private String formatRateMessage(Rate rate) {
        String timestamp = rate.getTimestamp() != null ? rate.getTimestamp() : LocalDateTime.now().toString();
        return String.format("%s|%.6f|%.6f|%s", rate.getRateName(), rate.getBid(), rate.getAsk(), timestamp);
    }

    private void sendRateToKafka(String message) {
        try {
            kafkaTemplate.send(forexTopic, message);
            logger.debug("Sent message to Kafka: {}", message);
        } catch (Exception ex) {
            logger.error("Error sending to Kafka", ex);
        }
    }
    
    // Getter for testing dynamic data provider loading
    public List<DataProvider> getDataProviders() {
        return dataProviders;
    }
}
