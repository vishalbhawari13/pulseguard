package com.example.pulseguard.controller;

import com.example.pulseguard.model.HealthData;
import com.example.pulseguard.service.HealthDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health-data")
public class HealthDataController {
    @Autowired
    private HealthDataService service;

    @PostMapping
    public ResponseEntity<String> saveHealthData(@RequestBody HealthData data) {
        service.saveHealthData(data);
        return ResponseEntity.ok("Data saved successfully!");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<HealthData>> getHealthData(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getDataByUserId(userId));
    }
}
