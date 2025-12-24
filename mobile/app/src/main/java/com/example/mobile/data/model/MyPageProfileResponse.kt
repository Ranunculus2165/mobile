package com.example.mobile.data.model

/**
 * 내 프로필 응답 DTO
 * - 서버의 MyPageProfileResponse(Java)와 필드 구조를 맞춘 버전
 */
data class MyPageProfileResponse(
    val name: String,
    val email: String,
    val point: Int
)


