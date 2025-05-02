package com.example.forexproject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = TcpServerTest.TestConfig.class)
public class TcpServerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    public static class TestConfig {
        // Minimal konfigürasyon: Sadece test ortamında ihtiyaç duyulan bean'ler
    }

    private static Thread serverThread;
    private static final int PORT = 8080;

    @BeforeAll
    public static void startServer() throws InterruptedException {
        TcpServer server = new TcpServer(PORT);
        serverThread = new Thread(server);
        serverThread.start();
        Thread.sleep(2000);
    }

    @AfterAll
    public static void stopServer() {
        serverThread.interrupt();
    }

    @Test
    public void testTcpConnection() throws Exception {
        try (Socket socket = new Socket("localhost", PORT)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("ping");
            String response = in.readLine();
            assertTrue(response != null && !response.isEmpty());
        }
    }
}