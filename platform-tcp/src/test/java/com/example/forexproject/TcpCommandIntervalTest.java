package com.example.forexproject;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class TcpCommandIntervalTest {

    private static final int PORT = 8080;
    // Otomatik IP tespiti: loopback olmayan ilk IPv4 adresi bulunuyor.
    private static final String HOST = getNonLoopbackAddress();

    private static String getNonLoopbackAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netIf : Collections.list(interfaces)) {
                if (netIf.isUp() && !netIf.isLoopback()) {
                    Enumeration<InetAddress> addrs = netIf.getInetAddresses();
                    for (InetAddress addr : Collections.list(addrs)) {
                        if (addr instanceof Inet4Address) {
                            return addr.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to detect non-loopback address: " + e.getMessage());
        }
        return "127.0.0.1"; // fallback
    }

    @Test
    public void testSendCommandsWithInterval() throws Exception {
        Socket socket = new Socket(HOST, PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Okuyucu thread: Sunucudan gelen mesajları ekrana yazdırır.
        Thread readerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("Received: " + line);
                }
            } catch (Exception e) {
                System.out.println("Reader thread ended: " + e.getMessage());
            }
        });
        readerThread.start();

        // Gönderilecek 5 komut, 5 saniye aralıklarla.
        List<String> commands = Arrays.asList(
                "subscribe|PF1_USDTRY",
                "subscribe|PF1_USDTRY",
                "subscribe|PF1_TRYUSD",
                "uns",
                "unsubscribe|PF1_USDTRY"
        );

        for (String cmd : commands) {
            Thread.sleep(5000);
            out.println(cmd);
        }

        // Komutların ardından ek 10 saniye bekleyerek yanıtların alınmasını sağlıyoruz.
        Thread.sleep(10000);

        socket.close();
    }
} 