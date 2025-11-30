package com.example.mobile.data.model

data class MenuItem(
    val id: Long,
    val name: String,
    val price: Int,
    val description: String?,
    val available: Boolean,
    val imageUrl: String?
)
