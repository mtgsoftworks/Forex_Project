package com.example.forexproject.coordinator.provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.example.forexproject.coordinator.CoordinatorCallback;
import com.example.forexproject.model.Rate;
import com.example.forexproject.model.RateFields;
import com.example.forexproject.model.RateStatus;

import java.util.Arrays;
import java.util.List;

/**
 * A stub implementation of {@link DataProvider} used for development / testing.
 * It periodically generates random rates and reports them back to the coordinator.
 */
@Component
public class SampleDataProvider implements DataProvider {

    private static final Logger logger = LogManager.getLogger(SampleDataProvider.class);

    // List of mock symbols to emit
    private static final List<String> SYMBOLS = Arrays.asList("PF1_USDTRY", "PF2_USDTRY");

    private CoordinatorCallback callback;
    private volatile boolean running = false;
    private Thread workerThread;

    @Override
    public void setCallback(CoordinatorCallback callback) {
        this.callback = callback;
    }

    @Override
    public void connect(String platformName, String userId, String password) {
        logger.info("Connected to {} as {}", platformName, userId);
        if (callback != null) {
            callback.onConnect(platformName, true);
        }
    }

    @Override
    public void disconnect(String platformName, String userId, String password) {
        running = false;
        logger.info("Disconnected from {}", platformName);
        if (callback != null) {
            callback.onDisconnect(platformName, true);
        }
    }

    @Override
    public void subscribe(String platformName, String rateName) {
        logger.info("Subscribed {} to {}", platformName, rateName);
        // send dummy available callback
        if (callback != null) {
            Rate rate = new Rate();
            rate.setRateName(rateName);
            rate.setBid(Math.random() * 10);
            rate.setAsk(rate.getBid() + 0.01);
            rate.setTimestamp(java.time.LocalDateTime.now().toString());
            callback.onRateAvailable(platformName, rateName, rate);
        }
    }

    @Override
    public void unSubscribe(String platformName, String rateName) {
        logger.info("Unsubscribed {} from {}", platformName, rateName);
        if (callback != null) {
            callback.onRateStatus(platformName, rateName, RateStatus.CLOSED);
        }
    }

    @Override
    public void startProvider() {
        if (workerThread == null) {
            running = true;
            workerThread = new Thread(this, "SampleDataProvider-Worker");
            workerThread.start();
        }
    }

    @Override
    public void stopProvider() {
        running = false;
        if (workerThread != null) {
            try {
                workerThread.join(1000);
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(2000);
                if (callback != null) {
                    for (String symbol : SYMBOLS) {
                        RateFields update = new RateFields(Math.random() * 10, Math.random() * 10 + 0.01);
                        String platform = symbol.startsWith("PF1") ? "PF1" : "PF2";
                        callback.onRateUpdate(platform, symbol, update);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}