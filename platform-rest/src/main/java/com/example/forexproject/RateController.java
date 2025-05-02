package com.example.forexproject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

import com.example.forexproject.config.RateSimulationProperties;

/**
 * REST API controller for on-demand Forex rate simulation (PF2).
 * Endpoint: GET /api/rates/{rateName}
 * Valid symbols configured via RateSimulationProperties.rates.
 */
@RestController
public class RateController {
    private static final Logger logger = LogManager.getLogger(RateController.class);

    @Autowired
    private RateSimulationProperties props;
    private final Random random = new Random();

    @GetMapping("/api/rates/{rateName}")
    public ResponseEntity<?> getRate(@PathVariable String rateName) {
        // Geçerli sembol doğrulaması
        if (!props.getRates().contains(rateName)) {
            String errorMsg = "Geçersiz sembol: " + rateName;
            logger.warn(errorMsg);
            Map<String, String> errorBody = Map.of("error", errorMsg);
            return ResponseEntity.badRequest().body(errorBody);
        }

        // Simülasyon algoritması - Geçerli sembol için veri üretimi
        double baseBid = props.getInitialBid().get(rateName);
        double baseAsk = props.getInitialAsk().get(rateName);
        double driftPct = props.getDriftPercentage() / 100.0;
        double driftBid = (random.nextDouble() * 2 - 1) * driftPct * baseBid;
        double driftAsk = (random.nextDouble() * 2 - 1) * driftPct * baseAsk;
        double bid = baseBid + driftBid;
        double ask = baseAsk + driftAsk;
        String timestamp = LocalDateTime.now().toString();
        logger.info("Returning rate for {}: bid={}, ask={}, timestamp={}", rateName, bid, ask, timestamp);
        RateResponse response = new RateResponse(rateName, bid, ask, timestamp);
        return ResponseEntity.ok(response);
    }
}