package com.example.mobile.ui.storedetail

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.network.ApiClient
import kotlinx.coroutines.launch

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

        // 인텐트에서 값 받기 (목록 화면에서 넘겨준 값)
        storeId = intent.getLongExtra(EXTRA_STORE_ID, -1L)
        val storeNameFromList = intent.getStringExtra(EXTRA_STORE_NAME) ?: "알 수 없는 가게"
        val statusFromList = intent.getStringExtra(EXTRA_STORE_STATUS) ?: "UNKNOWN"
        val minOrderFromList = intent.getIntExtra(EXTRA_STORE_MIN_ORDER, 0)
        val time = intent.getStringExtra(EXTRA_STORE_TIME) ?: ""

        if (storeId == -1L) {
            finish()
            return
        }

        // 일단 목록에서 받은 값으로 먼저 보여주고
        tvStoreName.text = storeNameFromList
        tvStoreStatus.text = when (statusFromList) {
            "OPEN" -> "영업 중"
            "CLOSED" -> "영업 종료"
            "PREPARING" -> "준비 중"
            else -> "상태 알 수 없음"
        }
        val minOrderText = String.format("최소주문 %,d원", minOrderFromList)
        tvMinOrderAndTime.text = "$minOrderText · $time"

        // 🔥 실제 API에서 상세 정보 + 메뉴 목록 불러오기
        loadStoreDetail(storeId)
    }

    private fun loadStoreDetail(storeId: Long) {
        lifecycleScope.launch {
            try {
                // /api/stores/{id} 호출 (StoreDetailResponse 받음)
                val response = ApiClient.storeApi.getStoreDetail(storeId)

                val store = response.store

                // 서버에서 내려준 최신 정보로 다시 덮어쓰기
                tvStoreName.text = store.name
                tvStoreStatus.text = when (store.status) {
                    "OPEN" -> "영업 중"
                    "CLOSED" -> "영업 종료"
                    "PREPARING" -> "준비 중"
                    else -> "상태 알 수 없음"
                }
                val minOrderText = String.format("최소주문 %,d원", store.minOrderPrice)
                // 시간 정보는 서버에 없으니 인텐트에서 받은 time 그대로 유지
                val currentText = tvMinOrderAndTime.text.toString()
                // "최소주문 ~원 · ~" 형식 유지
                tvMinOrderAndTime.text = currentText.replace(Regex("최소주문 .*원")) {
                    minOrderText
                }

                // ✅ 여기에서 진짜 DB 메뉴 목록을 어댑터에 넣어줌
                menuAdapter.submitList(response.menus)

            } catch (e: Exception) {
                Log.e("StoreDetailActivity", "상세 불러오기 실패", e)
                // ❌ 예전처럼 여기서 dummy 메뉴를 넣지 않는다
            }
        }
    }
}
