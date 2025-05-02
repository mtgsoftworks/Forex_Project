package com.example.forexproject.coordinator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.forexproject.coordinator.service.AlarmService;

@ExtendWith(MockitoExtension.class)
public class AlarmServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private AlarmService alarmService;

    @BeforeEach
    public void setup() {
        // "TestPlatform" için son yanıt zamanı 70 saniye önce olacak şekilde ayarlıyoruz.
        Map<String, LocalDateTime> lastResponseMap = new HashMap<>();
        lastResponseMap.put("TestPlatform", LocalDateTime.now().minusSeconds(70));
        ReflectionTestUtils.setField(alarmService, "lastResponseMap", lastResponseMap);
    }

    @Test
    public void testSendAlarmEmail() {
        // checkPlatformResponses metodu içinde sendAlarmEmail çağrısı yapılacak.
        alarmService.checkPlatformResponses();
        verify(mailSender).send(any(SimpleMailMessage.class));
        System.out.println("AlarmServiceTest: testSendAlarmEmail completed.");
    }
    
    @Test
    public void testArtificialEmailSending() {
        // Metodu doğrudan çağırarak mail gönderiminin tetiklendiğini doğruluyoruz.
        alarmService.sendAlarmEmail("Test Konusu", 70L);
        verify(mailSender).send(any(SimpleMailMessage.class));
        System.out.println("AlarmServiceTest: testArtificialEmailSending completed.");
    }
}
