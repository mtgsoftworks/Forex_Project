package com.example.forexproject.coordinator.controller;

import com.example.forexproject.model.Rate;
import com.example.forexproject.coordinator.provider.PF2RestProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/manual")
public class ManualController {

    @Autowired
    private PF2RestProvider pf2RestProvider;

    @GetMapping("/pf2/{symbol}")
    public ResponseEntity<?> pollPf2(@PathVariable String symbol) {
        try {
            Rate rate = pf2RestProvider.poll(symbol);
            return ResponseEntity.ok(rate);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
