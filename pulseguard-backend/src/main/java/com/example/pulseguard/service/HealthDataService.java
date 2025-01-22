package com.example.pulseguard.service;

import com.example.pulseguard.model.HealthData;
import com.example.pulseguard.repository.HealthDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthDataService {
    @Autowired
    private HealthDataRepository repository;

    public void saveHealthData(HealthData data) {
        repository.save(data);
    }

    public List<HealthData> getDataByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
}
