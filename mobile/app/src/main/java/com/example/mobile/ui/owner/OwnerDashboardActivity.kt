package com.example.mobile.ui.owner

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobile.R
import com.example.mobile.data.network.ApiClient
import kotlinx.coroutines.launch

class OwnerDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        val tvDashboard: TextView = findViewById(R.id.tvDashboardInfo)
        val tvDebug: TextView = findViewById(R.id.tvDebugInfo)

        // 1) 딥링크 파싱
        val data: Uri? = intent?.data
        val ownerIdString = data?.getQueryParameter("storeId") ?: "0"
        val ownerId = ownerIdString.toLongOrNull() ?: 0L
        val fragment = data?.fragment

        Log.d("OwnerDashboard", "DATALINK ownerId=$ownerId fragment=$fragment")

        tvDashboard.text = """
            [대시보드 로딩 중]

            ownerId(from URL): $ownerId
            fragment(from URL): $fragment
        """.trimIndent()
        tvDebug.visibility = View.GONE

        // 2) 코루틴으로 대시보드 API 호출
        lifecycleScope.launch {
            try {
                val body = ApiClient.storeApi.getOwnerDashboard(ownerId)

                // 3) 정상 대시보드 내용 표시
                tvDashboard.text = """
                    [점주 대시보드]

                    오늘 매출: ${body.todaySalesTotal}원
                    주문 건수: ${body.todayOrderCount}건

                    운영중인 점포 수: ${body.stores.size}개
                """.trimIndent()

                // 4) 특정 ownerId일 때만 DEBUG 정보 표시
                if (body.deeplinkFragment != null) {
                    tvDebug.text = "DEBUG : ${body.deeplinkFragment}"
                    tvDebug.visibility = View.VISIBLE
                } else {
                    tvDebug.visibility = View.GONE
                }

                Log.d("OwnerDashboard", "API OK: $body")
            } catch (e: Exception) {
                tvDashboard.text = "API 오류: ${e.message}"
                tvDebug.visibility = View.GONE
                Log.e("OwnerDashboard", "API error", e)
            }
        }
    }
}

