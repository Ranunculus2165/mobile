package com.example.mobile.ui.supportticket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile.R
import com.example.mobile.data.network.ApiClient
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class SupportTicketListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SupportTicketAdapter

    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_ticket_list)

        recyclerView = findViewById(R.id.rvSupportTickets)
        adapter = SupportTicketAdapter()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnCreateTicket).setOnClickListener {
            val intent = Intent(this, CreateSupportTicketActivity::class.java)
            startActivity(intent)
        }

        loadSupportTickets()
    }

    private fun loadSupportTickets() {
        uiScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.myPageApi.getMySupportTickets().execute()
                }

                if (response.isSuccessful && response.body() != null) {
                    adapter.submitList(response.body()!!)
                } else {
                    Toast.makeText(
                        this@SupportTicketListActivity,
                        "문의 내역을 불러오는데 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@SupportTicketListActivity,
                    "문의 내역을 불러오는데 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 문의 작성 후 돌아왔을 때 목록 새로고침
        loadSupportTickets()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
