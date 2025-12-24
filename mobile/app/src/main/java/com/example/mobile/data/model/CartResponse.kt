package com.example.mobile.data.model

data class CartResponse(
    val cartId: Long,
    val storeId: Long,
    val storeName: String,
    val items: List<CartItemResponse>,
    val totalPrice: Int
)

