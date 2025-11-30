package com.example.mobile.data.model

data class Store(
    val id: Long,
    val name: String,
    val category: String,
    val description: String?,
    val minOrderPrice: Int,
    val deliveryTip: Int,
    val rating: Double,
    val reviewCount: Int,
    val status: String,      // "OPEN" / "CLOSED"
    val imageUrl: String?
)
