package com.example.mobile.data.model

/**
 * 새 1:1 문의 작성 요청 DTO
 * - 서버의 CreateSupportTicketRequest(Java)와 필드 구조를 맞춘 버전
 */
data class CreateSupportTicketRequest(
    val storeId: Long, // 어떤 가게에 대한 문의인지
    val title: String, // 문의 제목
    val message: String // 문의 내용
)


