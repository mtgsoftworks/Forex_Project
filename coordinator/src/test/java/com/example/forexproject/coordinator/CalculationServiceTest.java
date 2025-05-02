package com.example.forexproject.coordinator;

import com.example.forexproject.coordinator.service.CalculationService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CalculationServiceTest {

    private final CalculationService calculationService = new CalculationService();

    @Test
    public void testPrepareUsdTryMessage() {
        String message = calculationService.prepareUsdTryMessage(33.6, 34.8, 35.9, 35.1, "2024-12-16T16:07:15.504");
        System.out.println("USDTRY Mesaji: " + message);
        assertTrue(message.startsWith("USDTRY|"));
        // Ortalama hesaplamalar:
        // Bid: (33.6+34.8)/2 = 34.2, Ask: (35.9+35.1)/2 = 35.5
        String expected = "USDTRY|34.200000|35.500000|2024-12-16T16:07:15.504";
        assertEquals(expected, message);
    }

    @Test
    public void testCheckTolerance() {
        double newValue = 35.0;
        double oldValue = 34.5;
        double result = calculationService.checkTolerance(newValue, oldValue);
        System.out.println("Tolerance Test 1: newValue=" + newValue + ", oldValue=" + oldValue + ", Sonuc=" + result);
        // Fark %1'den küçükse yeni değer döndürülmeli
        assertEquals(35.0, result, 0.0001);
        
        newValue = 36.0; // Yaklaşık çeyrek fark
        result = calculationService.checkTolerance(newValue, oldValue);
        System.out.println("Tolerance Test 2: newValue=" + newValue + ", oldValue=" + oldValue + ", Sonuc=" + result);
        // Fark büyükse eski değer korunmalı
        assertEquals(34.5, result, 0.0001);
    }
}
