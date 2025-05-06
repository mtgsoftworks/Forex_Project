package com.example.forexproject.coordinator.controller;

import com.example.forexproject.model.Rate;
import com.example.forexproject.coordinator.service.CoordinatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RatePushController {

    @Autowired
    private CoordinatorService coordinatorService;

    @PostMapping("/push/{platform}")
    public ResponseEntity<Void> pushRate(@PathVariable String platform, @RequestBody Rate rate) {
        coordinatorService.onRateAvailable(platform, rate.getRateName(), rate);
        return ResponseEntity.ok().build();
    }
}
