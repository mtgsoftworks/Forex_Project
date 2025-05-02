package com.example.forexproject.coordinator;

import com.example.forexproject.coordinator.service.AlarmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// Eğer bu testi default çalıştırma sırasında istemiyorsanız @Disabled ekleyebilirsiniz.
// import org.junit.jupiter.api.Disabled;

@SpringBootTest
// @Disabled("Gerçek e-posta gönderimi yapıyor, yalnızca entegrasyon testi olarak çalıştırın.")
public class AlarmServiceIntegrationTest {

    @Autowired
    private AlarmService alarmService;

    @Test
    public void testActualEmailSending() {
        // Gerçek SMTP ayarları kullanılarak e-posta gönderimi yapılır.
        alarmService.sendAlarmEmail("IntegrationTestPlatform", 70L);
        System.out.println("AlarmServiceIntegrationTest: testActualEmailSending completed. Check your inbox.");
    }
} 