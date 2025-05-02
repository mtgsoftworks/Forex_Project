package com.example.forexproject.coordinator.provider;

import com.example.forexproject.coordinator.config.TcpStreamingProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.forexproject.coordinator.CoordinatorCallback;
import com.example.forexproject.model.Rate;
import com.example.forexproject.model.RateStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * DataProvider implementation that connects to PF1 TCP Streaming simulator.
 * It opens a socket connection, subscribes to symbols and continuously reads
 * streaming messages, forwarding them to the Coordinator via callbacks.
 */
@Component
public class PF1TcpProvider implements DataProvider {

    private static final Logger logger = LogManager.getLogger(PF1TcpProvider.class);

    @Autowired
    private TcpStreamingProperties props;

    private CoordinatorCallback callback;
    private volatile boolean running;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private final Set<String> subscribedRates = new HashSet<>();

    @Override
    public void setCallback(CoordinatorCallback callback) {
        this.callback = callback;
    }

    @Override
    public void connect(String platformName, String userId, String password) {
        try {
            String host = "127.0.0.1"; // Optionally make this configurable via TcpStreamingProperties
            int port = props.getPort();
            socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.info("PF1TcpProvider connected to {}:{}", host, port);
            if (callback != null) callback.onConnect(platformName, true);
        } catch (Exception e) {
            logger.error("PF1TcpProvider failed to connect: {}", e.getMessage());
            if (callback != null) callback.onConnect(platformName, false);
        }
    }

    @Override
    public void disconnect(String platformName, String userId, String password) {
        try {
            running = false;
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
            logger.info("PF1TcpProvider disconnected from PF1 server");
            if (callback != null) callback.onDisconnect(platformName, true);
        } catch (Exception e) {
            logger.error("Error while disconnecting PF1TcpProvider: {}", e.getMessage());
            if (callback != null) callback.onDisconnect(platformName, false);
        }
    }

    @Override
    public void subscribe(String platformName, String rateName) {
        if (writer != null) {
            writer.println("subscribe|" + rateName);
            subscribedRates.add(rateName);
            logger.info("PF1TcpProvider subscribed to {}", rateName);
        }
    }

    @Override
    public void unSubscribe(String platformName, String rateName) {
        if (writer != null) {
            writer.println("unsubscribe|" + rateName);
            subscribedRates.remove(rateName);
            if (callback != null) callback.onRateStatus(platformName, rateName, RateStatus.CLOSED);
            logger.info("PF1TcpProvider unsubscribed from {}", rateName);
        }
    }

    @Override
    public void startProvider() {
        running = true;
        // Connect on start
        connect("PF1", "user", "pass");
        new Thread(this, "PF1TcpProvider-Reader").start();
    }

    @Override
    public void stopProvider() {
        disconnect("PF1", "user", "pass");
    }

    @Override
    public void run() {
        String line;
        try {
            while (running && (line = reader.readLine()) != null) {
                // Example format: PF1_USDTRY|22:number:34.4013|25:number:35.4013|5:timestamp:2024-12-15T11:31:34.509
                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;
                String symbol = parts[0];
                double bid = Double.parseDouble(parts[1].split(":")[2]);
                double ask = Double.parseDouble(parts[2].split(":")[2]);
                String timestamp = parts[3].substring(parts[3].lastIndexOf(":") + 1);

                if (!subscribedRates.contains(symbol)) continue;

                // First time arrival => available
                if (callback != null) {
                    Rate rate = new Rate();
                    rate.setRateName(symbol);
                    rate.setBid(bid);
                    rate.setAsk(ask);
                    rate.setTimestamp(timestamp);
                    callback.onRateAvailable("PF1", symbol, rate);
                }
            }
        } catch (Exception e) {
            logger.error("PF1TcpProvider reader error: {}", e.getMessage());
        }
    }
}
