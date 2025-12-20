package com.example.mobile.data.network

import com.example.mobile.data.model.OrderDetailResponse
import com.example.mobile.data.model.OrderRequest
import com.example.mobile.data.model.OrderResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {

    // 주문 생성 (결제 포함)
    @POST("api/orders")
    suspend fun createOrder(
        @Body request: OrderRequest
    ): OrderResponse

    // 주문 상세/영수증 조회
    @GET("api/orders/{orderId}")
    suspend fun getOrderDetail(
        @Path("orderId") orderId: Long
    ): OrderDetailResponse
}

