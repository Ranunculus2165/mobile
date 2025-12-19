package com.example.mobile.data.model

data class OrderResponse(
    val orderId: Long,
    val orderNumber: String,
    val totalPrice: Int,
    val status: String,
    val createdAt: String,
    val paidAt: String?
)

