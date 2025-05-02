package com.example.forexproject.coordinator;

import com.example.forexproject.coordinator.service.CalculationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = CoordinatorApplication.class)
public class CalculationServiceDynamicTest {

    @Autowired
    private CalculationService calculationService;

    @Test
    public void testDynamicCalculation() throws ScriptException {
        String formula = "variables.a + variables.b";
        Map<String, Object> vars = new HashMap<>();
        vars.put("a", 10);
        vars.put("b", 15);
        String result = calculationService.computeDynamicCalculation(formula, vars);
        System.out.println("Dinamik hesaplama sonucu: " + result);
        assertEquals("25", result);
    }

    @Test
    public void contextLoads() {
        // Test metodunuz
    }
}