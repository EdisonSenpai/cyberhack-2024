package com.example.mywalletproject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/enroll_card") // Endpoint-ul serverului
    Call<ApiResponse> enrollCard(@Body EnrollRequest request);
}
