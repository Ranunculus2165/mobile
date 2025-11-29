package com.example.mobile.ui.storedetail

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.MenuItem
import com.example.mobile.data.model.Store

class StoreDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STORE_ID = "extra_store_id"
        const val EXTRA_STORE_NAME = "extra_store_name"
        const val EXTRA_STORE_STATUS = "extra_store_status"
        const val EXTRA_STORE_MIN_ORDER = "extra_store_min_order"
        const val EXTRA_STORE_TIME = "extra_store_time"
    }

    private lateinit var tvStoreName: TextView
    private lateinit var tvStoreStatus: TextView
    private lateinit var tvMinOrderAndTime: TextView
    private lateinit var rvMenu: RecyclerView
    private lateinit var menuAdapter: MenuAdapter

    private var storeId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_detail)

        // View 연결
        tvStoreName = findViewById(R.id.tvDetailStoreName)
        tvStoreStatus = findViewById(R.id.tvDetailStoreStatus)
        tvMinOrderAndTime = findViewById(R.id.tvDetailMinOrderAndTime)
        rvMenu = findViewById(R.id.rvMenuList)

        // 리사이클러뷰 세팅
        menuAdapter = MenuAdapter()
        rvMenu.layoutManager = LinearLayoutManager(this)
        rvMenu.adapter = menuAdapter

        // 인텐트에서 값 받기
        storeId = intent.getLongExtra(EXTRA_STORE_ID, -1L)
        val storeName = intent.getStringExtra(EXTRA_STORE_NAME) ?: "알 수 없는 가게"
        val status = intent.getStringExtra(EXTRA_STORE_STATUS) ?: "UNKNOWN"
        val minOrder = intent.getIntExtra(EXTRA_STORE_MIN_ORDER, 0)
        val time = intent.getStringExtra(EXTRA_STORE_TIME) ?: ""

        // 상단 가게 정보 표시
        tvStoreName.text = storeName
        tvStoreStatus.text = when (status) {
            "OPEN" -> "영업 중"
            "CLOSED" -> "영업 종료"
            "PREPARING" -> "준비 중"
            else -> "상태 알 수 없음"
        }
        val minOrderText = String.format("최소주문 %,d원", minOrder)
        tvMinOrderAndTime.text = "$minOrderText · $time"

        // 더미 메뉴 데이터 로드
        val dummyMenus = createDummyMenus(storeId)
        menuAdapter.submitList(dummyMenus)
    }

    // ★ 가게별로 다른 메뉴를 만든다고 가정한 더미 데이터
    private fun createDummyMenus(storeId: Long): List<MenuItem> {
        return when (storeId) {
            1L -> listOf(
                MenuItem(1, 1, "화이트해커 후라이드", "기본에 충실한 바삭한 후라이드 치킨", 18000),
                MenuItem(2, 1, "화이트해커 양념치킨", "달콤 매콤한 양념", 19000),
                MenuItem(3, 1, "버그 제로 치킨세트", "치킨 + 콜라 1.25L", 22000)
            )
            2L -> listOf(
                MenuItem(4, 2, "버그없는 페퍼로니 피자", "토핑이 듬뿍 올라간 페퍼로니", 21000),
                MenuItem(5, 2, "디버깅 콤비네이션 피자", "다양한 토핑이 조화로운 피자", 23000)
            )
            else -> listOf(
                MenuItem(6, storeId, "기본 메뉴 1", "테스트용 더미 메뉴", 10000),
                MenuItem(7, storeId, "기본 메뉴 2", "테스트용 더미 메뉴", 12000)
            )
        }
    }
}
