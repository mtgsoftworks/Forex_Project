package com.example.forexproject.coordinator;

import com.example.forexproject.coordinator.provider.PF2RestProvider;
import com.example.forexproject.coordinator.config.Pf2RestProperties;
import com.example.forexproject.CoordinatorCallback;
import com.example.forexproject.model.Rate;
import com.example.forexproject.model.RateFields;
import com.example.forexproject.model.RateStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PF2RestProviderTest {

    @Mock
    private Pf2RestProperties props;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CoordinatorCallback callback;

    @InjectMocks
    private PF2RestProvider provider;

    @BeforeEach
    void setUp() {
        when(props.getBaseUrl()).thenReturn("http://api/");
        provider.setCallback(callback);
        provider.subscribe("PF2", "SYM");
    }

    @Test
    void pollOnce_success() {
        Rate rate = new Rate();
        rate.setRateName("SYM");
        rate.setBid(1.1);
        rate.setAsk(2.2);
        rate.setTimestamp("ts");
        when(restTemplate.getForEntity("http://api/SYM", Rate.class))
            .thenReturn(new ResponseEntity<>(rate, HttpStatus.OK));

        provider.pollOnce();

        verify(callback).onRateUpdate("PF2", "SYM", new RateFields(1.1, 2.2));
        verify(callback).onRateAvailable("PF2", "SYM", rate);
    }

    @Test
    void pollOnce_error() {
        when(restTemplate.getForEntity("http://api/SYM", Rate.class))
            .thenThrow(new RuntimeException("fail"));

        provider.pollOnce();

        verify(callback).onRateStatus("PF2", "SYM", RateStatus.ERROR);
    }
}
