package com.example.mobile.data.network

import com.example.mobile.data.model.CartItemRequest
import com.example.mobile.data.model.CartResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CartApi {

    // 장바구니 조회 (404일 경우 null 반환)
    @GET("api/cart")
    suspend fun getMyCart(): CartResponse?

    // 장바구니에 메뉴 추가
    @POST("api/cart/items")
    suspend fun addItemToCart(
        @Body request: CartItemRequest
    ): CartResponse
}

