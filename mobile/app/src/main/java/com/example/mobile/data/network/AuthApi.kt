package com.example.mobile.data.network

import com.example.mobile.data.model.LoginRequest
import com.example.mobile.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
