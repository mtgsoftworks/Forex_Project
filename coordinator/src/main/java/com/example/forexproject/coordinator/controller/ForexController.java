package com.example.forexproject.coordinator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forex")
public class ForexController {

    @GetMapping("/{fromCurrency}/{toCurrency}")
    public ResponseEntity<String> getForexData(@PathVariable String fromCurrency,
                                               @PathVariable String toCurrency) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                             .body("Forex verisi sağlayıcısı olarak AlphaVantage kullanılmamaktadır.");
    }
}
