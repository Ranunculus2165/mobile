package com.example.mobile.data.model

data class MyPageResponse(
    val name: String,
    val email: String,
    val orderHistory: List<OrderHistoryItem>
)

data class OrderHistoryItem(
    val orderId: Long,
    val storeName: String,
    val itemDescription: String,
    val orderDate: String, // ISO 8601 형식
    val totalPrice: Int,
    val status: String
)

