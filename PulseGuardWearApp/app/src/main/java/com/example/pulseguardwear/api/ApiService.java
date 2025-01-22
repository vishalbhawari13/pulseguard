package com.example.pulseguardwear.api;

import com.example.pulseguardwear.models.HealthData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/v1/health-data")
    Call<Void> sendHealthData(@Body HealthData data);
}
