package com.example.navigationwalkers

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api")
    fun getRoute(@Body requestData: RequestData): Call<List<ServerResponse>>
}

