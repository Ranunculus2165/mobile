package com.example.mobile.data.model

/**
 * 1:1 문의 응답 DTO
 * - 서버의 SupportTicketResponse(Java)와 필드 구조를 맞춘 버전
 */
data class SupportTicketResponse(
    val id: Long,
    val storeId: Long,
    val title: String,
    val message: String,
    val status: String,   // 서버에서는 enum(OPEN/ANSWERED/CLOSED)을 문자열로 내려온다고 가정
    val createdAt: String // ISO 8601 날짜 문자열
)


