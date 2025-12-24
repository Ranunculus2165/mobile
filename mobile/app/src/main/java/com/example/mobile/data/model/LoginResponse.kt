package com.example.mobile.data.model

data class LoginResponse(
    val token: String,
    val userId: Long,
    val name: String,
    val email: String,
    val role: String
)
