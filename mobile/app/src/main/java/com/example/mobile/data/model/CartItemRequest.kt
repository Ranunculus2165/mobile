package com.example.mobile.data.model

data class CartItemRequest(
    val storeId: Long,
    val menuId: Long,
    val quantity: Int = 1
)

