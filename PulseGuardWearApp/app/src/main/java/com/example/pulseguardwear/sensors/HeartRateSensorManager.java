package com.example.pulseguardwear.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class HeartRateSensorManager {

    private SensorManager sensorManager;
    private Sensor heartRateSensor;
    private SensorEventListener listener;

    public HeartRateSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    }

    public void startMonitoring(SensorEventListener listener) {
        this.listener = listener;
        sensorManager.registerListener(listener, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopMonitoring() {
        if (listener != null) {
            sensorManager.unregisterListener(listener);
        }
    }
}
