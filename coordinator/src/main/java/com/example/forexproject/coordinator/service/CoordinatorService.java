package com.example.forexproject.coordinator.service;

import com.example.forexproject.coordinator.CoordinatorCallback;
import com.example.forexproject.coordinator.provider.DataProvider;
import com.example.forexproject.model.Rate;
import com.example.forexproject.model.RateFields;
import com.example.forexproject.model.RateStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationContext;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.forexproject.coordinator.provider.PF2RestProvider;
import com.example.forexproject.coordinator.config.ProviderProperties;
import com.example.forexproject.coordinator.config.Pf2RestProperties;
import com.example.forexproject.coordinator.config.CoordinatorKafkaProperties;
import com.example.forexproject.coordinator.service.CalculationService;
import com.example.forexproject.coordinator.service.AlarmService;

@Service
public class CoordinatorService implements CoordinatorCallback {

    private static final Logger logger = LogManager.getLogger(CoordinatorService.class);

    // Thread-safe local cache for storing rates
    private Map<String, Rate> localCache = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private StreamOperations<String, String, String> streamOps;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private CalculationService calculationService;
    @Autowired
    private AlarmService alarmService;

    @Autowired
    private List<DataProvider> dataProviders;

    @Autowired
    private ProviderProperties providerProperties;
    @Autowired
    private Pf2RestProperties pf2Props;
    @Autowired
    private CoordinatorKafkaProperties kafkaProps;

    @Autowired
    private ApplicationContext applicationContext;  // inject Spring context

    // Track PF2 error start times per symbol
    private Map<String, LocalDateTime> pf2ErrorStartMap = new ConcurrentHashMap<>();
    private static final long PF2_ERROR_THRESHOLD_SECONDS = 60;

    @PostConstruct
    public void initDataProviders() {
        for (DataProvider provider : dataProviders) {
            provider.setCallback(this);
            provider.startProvider();
            logger.info("Loaded and started DataProvider: {}", provider.getClass().getSimpleName());
            if (provider instanceof PF2RestProvider) {
                for (String rate : pf2Props.getRates()) {
                    provider.subscribe("PF2", rate);
                }
            }
        }

        if (providerProperties.getClasses() != null) {
            for (String className : providerProperties.getClasses()) {
                try {
                    Class<?> clazz = Class.forName(className);
                    @SuppressWarnings("unchecked")
                    Class<? extends DataProvider> providerClass = (Class<? extends DataProvider>) clazz;
                    DataProvider provider = applicationContext.getBean(providerClass);
                    provider.setCallback(this);
                    provider.startProvider();
                    logger.info("Loaded dynamic DataProvider: {}", className);
                    if (provider instanceof PF2RestProvider) {
                        for (String rate : pf2Props.getRates()) {
                            provider.subscribe("PF2", rate);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to load dynamic provider {}: {}", className, e.getMessage(), e);
                }
            }
        }
    }

    @PostConstruct
    public void initStreams() {
        this.streamOps = redisTemplate.opsForStream();
    }

    @Override
    public void onConnect(String platformName, boolean status) {
        logger.info("Connected to {}: {}", platformName, status);
        alarmService.updateLastResponse(platformName);
    }

    @Override
    public void onDisconnect(String platformName, boolean status) {
        logger.info("Disconnected from {}: {}", platformName, status);
    }

    @Override
    public void onRateAvailable(String platformName, String rateName, Rate rate) {
        // Clear PF2 error tracking on successful data receipt
        if ("PF2".equals(platformName)) {
            pf2ErrorStartMap.remove(rateName);
        }
        logger.info("Rate available from {}: {}", platformName, rateName);
        alarmService.updateLastResponse(platformName);
        localCache.put(rateName, rate);
        // Persist raw rate to Redis and Kafka
        String message = formatRateMessage(rate);
        try {
            redisTemplate.opsForList().rightPush("raw:" + rateName, message);
        } catch (Exception e) {
            logger.error("Redis error onRateAvailable for {}: {}", rateName, e.getMessage(), e);
        }
        sendRateToKafka(message);
        try {
            streamOps.add("raw_stream", Map.of("message", message));
        } catch (Exception e) {
            logger.error("Redis Stream error onRateAvailable for {}: {}", rateName, e.getMessage(), e);
        }
        try {
            processComputedRates();
        } catch (Exception e) {
            logger.error("Error in processComputedRates: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onRateUpdate(String platformName, String rateName, RateFields rateFields) {
        // Clear PF2 error tracking on update
        if ("PF2".equals(platformName)) {
            pf2ErrorStartMap.remove(rateName);
        }
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
        try {
            redisTemplate.opsForList().rightPush("raw:" + rateName, updateMessage);
        } catch (Exception e) {
            logger.error("Redis error onRateUpdate for {}: {}", rateName, e.getMessage(), e);
        }
        sendRateToKafka(updateMessage);
        try {
            streamOps.add("raw_stream", Map.of("message", updateMessage));
        } catch (Exception e) {
            logger.error("Redis Stream error onRateUpdate for {}: {}", rateName, e.getMessage(), e);
        }
        try {
            processComputedRates();
        } catch (Exception e) {
            logger.error("Error in processComputedRates: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onRateStatus(String platformName, String rateName, RateStatus rateStatus) {
        logger.debug("Rate status from {}: {} - {}", platformName, rateName, rateStatus);
        // If PF2 errors persist beyond threshold, unsubscribe and trigger alarm
        if ("PF2".equals(platformName) && rateStatus == RateStatus.ERROR) {
            LocalDateTime now = LocalDateTime.now();
            // Record first error time if not already tracked
            if (!pf2ErrorStartMap.containsKey(rateName)) {
                pf2ErrorStartMap.put(rateName, now);
            }
            LocalDateTime firstErrorTime = pf2ErrorStartMap.get(rateName);
            Duration duration = Duration.between(firstErrorTime, now);
            if (duration.getSeconds() >= PF2_ERROR_THRESHOLD_SECONDS) {
                logger.warn("Unsubscribing from {} after {} seconds of errors", rateName, duration.getSeconds());
                for (DataProvider provider : dataProviders) {
                    if (provider instanceof PF2RestProvider) {
                        provider.unSubscribe(platformName, rateName);
                    }
                }
                alarmService.sendAlarmEmail(platformName, duration.getSeconds());
                pf2ErrorStartMap.remove(rateName);
            }
        } else {
            // Clear error tracking on any non-error event
            pf2ErrorStartMap.remove(rateName);
        }
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
            redisTemplate.opsForList().rightPush("computed:USDTRY", usdtryMessage);
            streamOps.add("computed_stream", Map.of("message", usdtryMessage));
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
            redisTemplate.opsForList().rightPush("computed:EURTRY", eurtryMessage);
            streamOps.add("computed_stream", Map.of("message", eurtryMessage));
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
            redisTemplate.opsForList().rightPush("computed:GBPTRY", gbptryMessage);
            streamOps.add("computed_stream", Map.of("message", gbptryMessage));
            sendRateToKafka(gbptryMessage);
        }
    }

    // Helper to format rate message for Kafka/Redis
    private String formatRateMessage(Rate rate) {
        String timestamp = rate.getTimestamp() != null ? rate.getTimestamp() : LocalDateTime.now().toString();
        return String.format("%s|%.6f|%.6f|%s", rate.getRateName(), rate.getBid(), rate.getAsk(), timestamp);
    }

    private void sendRateToKafka(String message) {
        kafkaTemplate.send(kafkaProps.getForex(), message)
            .whenComplete((metadata, throwable) -> {
                if (throwable != null) {
                    logger.error("Kafka send failed for topic {}: {}", kafkaProps.getForex(), throwable.getMessage(), throwable);
                } else {
                    logger.info("Sent message to topic {}: {} | metadata: {}", kafkaProps.getForex(), message, metadata);
                }
            });
    }
    
    // Getter for testing dynamic data provider loading
    public List<DataProvider> getDataProviders() {
        return dataProviders;
    }
}
