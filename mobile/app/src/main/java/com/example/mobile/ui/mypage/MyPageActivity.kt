package com.example.mobile.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.auth.AuthStateManager
import com.example.mobile.data.model.MyPageResponse
import com.example.mobile.data.network.ApiClient
import com.example.mobile.ui.auth.LoginActivity
import com.example.mobile.ui.base.BaseActivity
import com.example.mobile.ui.orderhistory.OrderHistoryActivity
import com.example.mobile.ui.supportticket.SupportTicketListActivity
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MyPageActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderHistoryAdapter

    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        recyclerView = findViewById(R.id.rvOrderHistory)
        adapter = OrderHistoryAdapter()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 문의하기 버튼 클릭 리스너
        findViewById<Button>(R.id.btnInquiry).setOnClickListener {
            val intent = Intent(this, SupportTicketListActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃 버튼 클릭 리스너
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }

        // 전체 주문 내역 보기 버튼 클릭 리스너
        findViewById<Button>(R.id.btnViewAllOrders).setOnClickListener {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            startActivity(intent)
        }

        loadMyPageData()
    }

    private fun loadMyPageData() {
        uiScope.launch {
            try {
                val response: MyPageResponse = withContext(Dispatchers.IO) {
                    ApiClient.myPageApi.getMyPage().execute().body()!!
                }

                // 사용자 정보 표시
                findViewById<android.widget.TextView>(R.id.tvUserName).text = response.name
                findViewById<android.widget.TextView>(R.id.tvUserPhone).text = response.email
                
                // 포인트 정보 표시 (천 단위 구분 기호 포함)
                val pointText = String.format("%,dP", response.point)
                findViewById<android.widget.TextView>(R.id.tvUserPoint).text = pointText

                // 주문 내역 표시
                adapter.submitList(response.orderHistory)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@MyPageActivity,
                    "마이페이지 정보를 불러오는데 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun logout() {
        // OAuth 인증 상태 삭제
        val authStateManager = AuthStateManager.getInstance(this)
        authStateManager.clear()

        // 로그인 화면으로 이동
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

