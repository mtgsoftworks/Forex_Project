package com.example.forexproject.coordinator.provider;

import com.example.forexproject.coordinator.CoordinatorCallback;
import com.example.forexproject.coordinator.config.Pf2RestProperties;
import com.example.forexproject.model.Rate;
import com.example.forexproject.model.RateFields;
import com.example.forexproject.model.RateStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * DataProvider implementation that polls PF2 REST API simulator.
 */
@Component
public class PF2RestProvider implements DataProvider {

    private static final Logger logger = LogManager.getLogger(PF2RestProvider.class);

    @Autowired
    private Pf2RestProperties props;

    @Autowired
    private RestTemplate restTemplate;

    private CoordinatorCallback callback;
    private final Set<String> subscriptions = new ConcurrentSkipListSet<>();
    private volatile boolean running;
    private Thread poller;

    @Override
    public void setCallback(CoordinatorCallback callback) {
        this.callback = callback;
    }

    @Override
    public void connect(String platformName, String userId, String password) {
        // REST provider has no persistent connection; assume success
        if (callback != null) callback.onConnect(platformName, true);
    }

    @Override
    public void disconnect(String platformName, String userId, String password) {
        running = false;
        if (callback != null) callback.onDisconnect(platformName, true);
    }

    @Override
    public void subscribe(String platformName, String rateName) {
        subscriptions.add(rateName);
        logger.info("PF2RestProvider subscribed to {}", rateName);
    }

    @Override
    public void unSubscribe(String platformName, String rateName) {
        subscriptions.remove(rateName);
        if (callback != null) callback.onRateStatus(platformName, rateName, RateStatus.CLOSED);
    }

    @Override
    public void startProvider() {
        if (!props.isEnabled() || props.isManualMode()) {
            logger.info("PF2RestProvider auto-start disabled (enabled={}, manualMode={})",
                    props.isEnabled(), props.isManualMode());
            return;
        }
        running = true;
        poller = new Thread(this, "PF2RestProvider-Poller");
        poller.start();
    }

    @Override
    public void stopProvider() {
        running = false;
    }

    /**
     * Polls all subscribed symbols once.
     */
    public void pollOnce() {
        for (String symbol : subscriptions) {
            String url = props.getBaseUrl() + symbol;
            try {
                ResponseEntity<Rate> response = restTemplate.getForEntity(url, Rate.class);
                Rate rate = response.getBody();
                if (response.getStatusCode().is2xxSuccessful() && rate != null) {
                    if (callback != null) {
                        callback.onRateUpdate("PF2", symbol, new RateFields(rate.getBid(), rate.getAsk()));
                        callback.onRateAvailable("PF2", symbol, rate);
                    }
                }
            } catch (Exception ex) {
                logger.warn("Error fetching {} from PF2: {}", symbol, ex.getMessage());
                if (callback != null) callback.onRateStatus("PF2", symbol, RateStatus.ERROR);
            }
        }
    }

    /**
     * Manual poll for a single symbol.
     */
    public Rate poll(String symbol) {
        String url = props.getBaseUrl() + symbol;
        ResponseEntity<Rate> response = restTemplate.getForEntity(url, Rate.class);
        Rate rate = response.getBody();
        if (response.getStatusCode().is2xxSuccessful() && rate != null) {
            if (callback != null) {
                callback.onRateUpdate("PF2", symbol, new RateFields(rate.getBid(), rate.getAsk()));
                callback.onRateAvailable("PF2", symbol, rate);
            }
            return rate;
        }
        throw new RuntimeException("PF2 manual poll failed for " + symbol);
    }

    @Override
    public void run() {
        while (running) {
            pollOnce();
            try {
                Thread.sleep(props.getPollInterval());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
