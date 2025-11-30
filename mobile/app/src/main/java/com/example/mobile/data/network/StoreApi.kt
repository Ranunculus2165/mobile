package com.example.mobile.data.network

import com.example.mobile.data.model.Store
import com.example.mobile.data.model.StoreDetailResponse
import com.example.mobile.data.model.OwnerDashboardResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface StoreApi {

    // 가게 목록
    @GET("api/stores")
    suspend fun getStores(): List<Store>

    // 가게 상세 (가게 + 메뉴들)
    @GET("api/stores/{id}")
    suspend fun getStoreDetail(
        @Path("id") storeId: Long
    ): StoreDetailResponse

    // 🔥 점주 대시보드
    //   /api/stores/owners/{ownerId}/dashboard
    @GET("api/stores/owners/{ownerId}/dashboard")
    suspend fun getOwnerDashboard(
        @Path("ownerId") ownerId: Long
    ): OwnerDashboardResponse


}
