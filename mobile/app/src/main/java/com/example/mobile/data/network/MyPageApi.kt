package com.example.mobile.data.network

import com.example.mobile.data.model.CreateSupportTicketRequest
import com.example.mobile.data.model.MyPageProfileResponse
import com.example.mobile.data.model.SupportTicketResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 마이페이지 관련 API 정의
 *
 * 서버 컨트롤러 기준:
 * - GET  /api/users/me
 * - GET  /api/users/me/support-tickets
 * - POST /api/users/me/support-tickets
 *
 * 지금은 서버에서 userId = 1L 로 하드코딩하므로
 * 클라이언트에서는 별도 path/param 이 필요 없다.
 */
interface MyPageApi {

    /**
     * 내 프로필 조회
     * GET /api/users/me
     */
    @GET("/api/users/me")
    fun getMyProfile(): Call<MyPageProfileResponse>

    /**
     * 내 1:1 문의 목록 조회
     * GET /api/users/me/support-tickets
     */
    @GET("/api/users/me/support-tickets")
    fun getMySupportTickets(): Call<List<SupportTicketResponse>>

    /**
     * 새 1:1 문의 작성
     * POST /api/users/me/support-tickets
     */
    @POST("/api/users/me/support-tickets")
    fun createSupportTicket(
        @Body body: CreateSupportTicketRequest
    ): Call<SupportTicketResponse>
}
