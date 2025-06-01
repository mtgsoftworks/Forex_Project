package com.example.forexproject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

import com.example.forexproject.config.RateSimulationProperties;

/**
 * REST API endpoints for döviz oranı simülasyonu.
 */
@RestController
public class RateController {
    private static final Logger logger = LogManager.getLogger(RateController.class);

    @Autowired
    private RateSimulationProperties props;
    private final Random random = new Random();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Verilen sembol için tek seferlik döviz oranı simülasyonu yapar ve sonucu döner.
     * Ayrıca Coordinator servisine push eder.
     *
     * @param rateName Simülasyon yapılacak döviz sembolü (örn: PF2_USDTRY).
     * @return bid, ask ve timestamp içeren RateResponse veya hata durumunda 400 HTTP.
     */
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
        try {
            restTemplate.postForEntity("http://coordinator:8090/api/push/PF2", response, Void.class);
        } catch (Exception e) {
            logger.warn("Coordinator push failed: {}", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Belirtilen sembol için sürekli SSE (Server-Sent Events) üzerinden döviz oranı akışı sağlar.
     *
     * @param rateName Takip edilecek döviz sembolü.
     * @return SseEmitter objesi, olay akışını içerir.
     * @throws ResponseStatusException Geçersiz sembol için BAD_REQUEST fırlatır.
     */
    @GetMapping(value = "/api/rates/stream/{rateName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRate(@PathVariable String rateName) {
        // Validate symbol
        if (!props.getRates().contains(rateName)) {
            String errorMsg = "Invalid symbol: " + rateName;
            logger.warn(errorMsg);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        new Thread(() -> {
            try {
                while (true) {
                    double baseBid = props.getInitialBid().get(rateName);
                    double baseAsk = props.getInitialAsk().get(rateName);
                    double driftPct = props.getDriftPercentage() / 100.0;
                    double driftBid = (random.nextDouble() * 2 - 1) * driftPct * baseBid;
                    double driftAsk = (random.nextDouble() * 2 - 1) * driftPct * baseAsk;
                    double bid = baseBid + driftBid;
                    double ask = baseAsk + driftAsk;
                    String timestamp = LocalDateTime.now().toString();
                    RateResponse response = new RateResponse(rateName, bid, ask, timestamp);
                    emitter.send(response);
                    Thread.sleep(props.getPollInterval());
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, "rate-sse-stream-" + rateName).start();
        return emitter;
    }
}