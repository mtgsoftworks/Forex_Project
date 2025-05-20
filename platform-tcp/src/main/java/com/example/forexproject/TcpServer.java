package com.example.forexproject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PreDestroy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;

import com.example.forexproject.config.TcpStreamingProperties;
import com.example.forexproject.model.Rate;

/**
 * TCP sunucusunu çalıştıran bileşen.
 * Gelen isteklere göre bağlantı açar, subscribe/unsubscribe komutlarını yönetir ve rate verilerini yayınlar.
 */
@Component
public class TcpServer implements Runnable {

    private static final Logger logger = LogManager.getLogger(TcpServer.class);

    @Autowired
    private TcpStreamingProperties props;
    private final RestTemplate restTemplate = new RestTemplate();

    private ServerSocket serverSocket;

    /** Default constructor for Spring injection */
    public TcpServer() {}

    /** Test constructor to set port manually */
    public TcpServer(int port) {
        this.props = new TcpStreamingProperties();
        this.props.setPort(port);
    }

    /**
     * Server soketini başlatır ve gelen bağlantıları dinler.
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(props.getPort());
            logger.info("TCP Server started on port {}", props.getPort());
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected: {}", clientSocket.getRemoteSocketAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            logger.error("Error in TCP Server: {}", e.getMessage(), e);
        } finally {
            closeServerSocket();
        }
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket: {}", e.getMessage(), e);
        }
    }

    /**
     * Uygulama kapanırken server soketini kapatmak için çağrılır.
     */
    @PreDestroy
    private void shutdown() {
        closeServerSocket();
    }

    /**
     * Her bir istemci bağlantısı için komutları işleyen ve rate akışı sağlayan handler sınıfı.
     */
    private class ClientHandler implements Runnable {

        private Socket clientSocket;
        private Map<String, Thread> subscriptionThreads = new ConcurrentHashMap<>();
        private final Set<String> validSymbols;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.validSymbols = new HashSet<>(props.getRates());
        }

        /**
         * Komut satırından subscribe/unsubscribe isteklerini işleyip,
         * aboneliklere göre rate verilerini istemciye gönderir.
         */
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // İstemciye hoş geldiniz mesajı gönderilir.
                out.println(props.getWelcomeMessage());
                String command;
                while ((command = in.readLine()) != null) {
                    logger.info("Received command from {}: {}", clientSocket.getRemoteSocketAddress(), command);
                    // Heartbeat command support
                    if ("heartbeat".equalsIgnoreCase(command.trim())) {
                        out.println("heartbeat|timestamp:" + LocalDateTime.now());
                        continue;
                    }
                    if (command.contains("|")) {
                        String[] tokens = command.split("\\|");
                        String mainCmd = tokens[0].trim().toLowerCase();
                        if ("subscribe".equals(mainCmd)) {
                            if (tokens.length < 2) {
                                out.println("ERROR|Invalid request format");
                                continue;
                            }
                            String symbol = tokens[1].trim();
                            if (!validSymbols.contains(symbol)) {
                                out.println("ERROR|Rate data not found for " + symbol);
                                continue;
                            }
                            if (subscriptionThreads.containsKey(symbol)) {
                                out.println("Already subscribed to " + symbol);
                                continue;
                            }
                            // Abonelik için veri yayınlayacak thread oluşturuluyor.
                            Thread t = new Thread(() -> {
                                try {
                                    while (!Thread.currentThread().isInterrupted()) {
                                        double baseBid = props.getInitialBid().get(symbol);
                                        double baseAsk = props.getInitialAsk().get(symbol);
                                        double driftPct = props.getDriftPercentage() / 100.0;
                                        double driftBid = (Math.random()*2 - 1) * driftPct * baseBid;
                                        double driftAsk = (Math.random()*2 - 1) * driftPct * baseAsk;
                                        double bid = baseBid + driftBid;
                                        double ask = baseAsk + driftAsk;
                                        String timestamp = LocalDateTime.now().toString();
                                        String msg = String.format("%s|22:number:%.8f|25:number:%.8f|5:timestamp:%s",
                                                symbol, bid, ask, timestamp);
                                        // Log streamed message to console
                                        logger.info("Streaming to {}: {}", clientSocket.getRemoteSocketAddress(), msg);
                                        out.println(msg);
                                        try {
                                            Rate rateObj = new Rate();
                                            rateObj.setRateName(symbol);
                                            rateObj.setBid(bid);
                                            rateObj.setAsk(ask);
                                            rateObj.setTimestamp(timestamp);
                                            restTemplate.postForEntity("http://coordinator:8090/api/push/TCP", rateObj, Void.class);
                                        } catch (Exception e) {
                                            logger.warn("Coordinator push failed: {}", e.getMessage());
                                        }
                                        // Throttle: sleep between messages
                                        Thread.sleep(props.getMessageInterval());
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });
                            subscriptionThreads.put(symbol, t);
                            t.start();
                            out.println("Subscribed to " + symbol);
                        } else if ("unsubscribe".equals(mainCmd)) {
                            if (tokens.length < 2) {
                                out.println("ERROR|Invalid request format");
                                continue;
                            }
                            String symbol = tokens[1].trim();
                            Thread t = subscriptionThreads.get(symbol);
                            if (t != null) {
                                t.interrupt();
                                subscriptionThreads.remove(symbol);
                                out.println("Unsubscribed from " + symbol);
                                // Notify client of rate status closed
                                out.println("status|" + symbol + "|CLOSED");
                            } else {
                                out.println("ERROR|Not subscribed to " + symbol);
                            }
                        } else {
                            out.println("ERROR|Invalid request format");
                        }
                    } else {
                        out.println("ERROR|Invalid request format");
                    }
                }
            } catch (Exception e) {
                logger.error("Error handling client {}: {}", clientSocket.getRemoteSocketAddress(), e.getMessage(), e);
            } finally {
                // Açık abonelikleri sonlandır.
                for (Thread t : subscriptionThreads.values()) {
                    t.interrupt();
                }
                try {
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    logger.error("Error closing client socket: {}", e.getMessage(), e);
                }
            }
        }
    }
}