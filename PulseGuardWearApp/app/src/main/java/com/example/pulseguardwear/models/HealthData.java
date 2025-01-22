package com.example.pulseguardwear.models;

public class HealthData {
    private int heartRate;
    private int steps;
    private String timestamp;

    public HealthData(int heartRate, int steps, String timestamp) {
        this.heartRate = heartRate;
        this.steps = steps;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
