package com.example.mobile.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")   // 에뮬레이터에서 localhost
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val storeApi: StoreApi = retrofit.create(StoreApi::class.java)
}
