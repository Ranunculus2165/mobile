package com.example.mobile.data.model

data class CartItemResponse(
    val cartItemId: Long,
    val menuId: Long,
    val menuName: String,
    val quantity: Int,
    val unitPrice: Int,
    val linePrice: Int
)

