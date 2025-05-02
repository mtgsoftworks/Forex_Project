package com.example.forexproject.coordinator;

import com.example.forexproject.coordinator.service.CoordinatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class CoordinatorServiceTest {

    @Autowired
    private CoordinatorService coordinatorService;

    @Test
    public void testDataProvidersLoaded() {
        System.out.println("Yuklenen data provider'lar: " + coordinatorService.getDataProviders());
        assertFalse(coordinatorService.getDataProviders().isEmpty(), "Data provider'lar yüklenmiş olmalı.");
    }
}