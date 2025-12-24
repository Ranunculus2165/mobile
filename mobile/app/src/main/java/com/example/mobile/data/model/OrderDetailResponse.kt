package com.example.mobile.data.model

data class OrderDetailResponse(
    val orderId: Long,
    val orderNumber: String,
    val status: String,
    val totalPrice: Int,
    val createdAt: String,
    val paidAt: String?,
    val items: List<OrderItemResponse>,
    val storeName: String,
    val storeAddress: String,
    val deliveryFee: Int,
    val orderAmount: Int,
    val userName: String,
    val userEmail: String,
    val receiptFlag: String?
)

data class OrderItemResponse(
    val menuId: Long,
    val menuName: String,
    val quantity: Int,
    val unitPrice: Int
)
