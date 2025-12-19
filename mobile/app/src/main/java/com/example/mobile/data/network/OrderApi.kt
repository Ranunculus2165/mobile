package com.example.mobile.data.network

import com.example.mobile.data.model.OrderRequest
import com.example.mobile.data.model.OrderResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OrderApi {

    // 주문 생성 (결제 포함)
    @POST("api/orders")
    suspend fun createOrder(
        @Body request: OrderRequest
    ): OrderResponse
}

