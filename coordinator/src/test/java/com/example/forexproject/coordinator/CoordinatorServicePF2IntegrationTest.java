package com.example.forexproject.coordinator;

import com.example.forexproject.coordinator.provider.PF2RestProvider;
import com.example.forexproject.coordinator.service.CoordinatorService;
import com.example.forexproject.coordinator.config.Pf2RestProperties;
import com.example.forexproject.model.Rate;
import com.example.forexproject.model.RateFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
public class CoordinatorServicePF2IntegrationTest {

    @Autowired
    private PF2RestProvider pf2RestProvider;

    @Autowired
    private CoordinatorService coordinatorService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private Pf2RestProperties props;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    private ValueOperations<String, Object> valueOps;

    @BeforeEach
    void setUp() {
        when(props.getBaseUrl()).thenReturn("http://api/");
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        // stop background polling thread
        pf2RestProvider.stopProvider();
        pf2RestProvider.setCallback(coordinatorService);
        pf2RestProvider.subscribe("PF2", "SYM");
    }

    @AfterEach
    void tearDown() {
        pf2RestProvider.stopProvider();
    }

    @Test
    void testCoordinatorProcessesPF2Rate() {
        Rate rate = new Rate();
        rate.setRateName("SYM");
        rate.setBid(1.1);
        rate.setAsk(1.2);
        rate.setTimestamp("ts");
        when(restTemplate.getForEntity("http://api/SYM", Rate.class))
            .thenReturn(new ResponseEntity<>(rate, HttpStatus.OK));

        pf2RestProvider.pollOnce();

        verify(valueOps).set(eq("raw:SYM"), contains("SYM|1.100000|1.200000|ts"));
        verify(kafkaTemplate).send(anyString(), contains("SYM|1.100000|1.200000|ts"));
    }
}
