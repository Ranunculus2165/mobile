package com.example.mobile.data.model

/**
 * 점주 대시보드 API 응답용 데이터 클래스
 * GET /api/stores/owners/{ownerId}/dashboard
 */
data class OwnerDashboardResponse(
    val ownerId: Long,
    val stores: List<OwnerStoreSummary>, // 점주가 가진 점포 요약 리스트
    val todaySalesTotal: Int,            // 오늘 전체 매출
    val todayOrderCount: Int,            // 오늘 주문 건수
    val flag: String?,                   // 특정 ownerId일 때만 내려오는 FLAG (없으면 null)
    val deeplinkFragment: String?        // 특정 ownerId일 때만 내려오는 URL 조각 (예: "th/wheat")
)

/**
 * 대시보드에 표시할 점포 요약 정보
 * (서버 쪽 OwnerDashboardResponse의 StoreSummary에 대응)
 */
data class OwnerStoreSummary(
    val id: Long,
    val name: String,
    val status: String   // 서버에서 StoreStatus(enum)를 문자열로 내려준다고 가정
)
