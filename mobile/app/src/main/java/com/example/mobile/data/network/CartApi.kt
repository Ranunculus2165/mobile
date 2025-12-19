package com.example.mobile.data.network

import com.example.mobile.data.model.CartItemRequest
import com.example.mobile.data.model.CartResponse
import com.example.mobile.data.model.UpdateCartItemQuantityRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CartApi {

    // 장바구니 조회 (404일 경우 null 반환)
    @GET("api/cart")
    suspend fun getMyCart(): CartResponse?

    // 장바구니에 메뉴 추가
    @POST("api/cart/items")
    suspend fun addItemToCart(
        @Body request: CartItemRequest
    ): CartResponse

    // 장바구니 아이템 수량 수정
    @PATCH("api/cart/items/{cartItemId}")
    suspend fun updateItemQuantity(
        @Path("cartItemId") cartItemId: Long,
        @Body request: UpdateCartItemQuantityRequest
    ): CartResponse

    // 장바구니 아이템 삭제
    @DELETE("api/cart/items/{cartItemId}")
    suspend fun removeItem(
        @Path("cartItemId") cartItemId: Long
    ): CartResponse
}

