package com.example.mobile.ui.supportticket

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.example.mobile.R
import com.example.mobile.data.model.SupportTicketResponse
import com.example.mobile.data.network.ApiClient
import com.example.mobile.ui.base.BaseActivity
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class SupportTicketDetailActivity : BaseActivity() {

    private lateinit var tvTicketTitle: TextView
    private lateinit var tvStoreName: TextView
    private lateinit var tvCreatedAt: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvMessage: TextView

    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        const val EXTRA_TICKET_ID = "ticket_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_ticket_detail)

        val ticketId = intent.getLongExtra(EXTRA_TICKET_ID, -1L)
        if (ticketId == -1L) {
            finish()
            return
        }

        initViews()
        setupClickListeners()
        loadTicketDetail(ticketId)
    }

    private fun initViews() {
        tvTicketTitle = findViewById(R.id.tvTicketTitle)
        tvStoreName = findViewById(R.id.tvStoreName)
        tvCreatedAt = findViewById(R.id.tvCreatedAt)
        tvStatus = findViewById(R.id.tvStatus)
        tvMessage = findViewById(R.id.tvMessage)
    }

    private fun setupClickListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun loadTicketDetail(ticketId: Long) {
        uiScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.myPageApi.getMySupportTickets().execute()
                }

                if (response.isSuccessful && response.body() != null) {
                    val tickets = response.body()!!
                    val ticket = tickets.find { it.id == ticketId }
                    
                    if (ticket != null) {
                        displayTicket(ticket)
                    } else {
                        // 티켓을 찾을 수 없음
                        finish()
                    }
                } else {
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        }
    }

    private fun displayTicket(ticket: SupportTicketResponse) {
        tvTicketTitle.text = ticket.title
        tvStoreName.text = ticket.storeName
        tvMessage.text = ticket.message

        // 날짜 포맷팅
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dateFormatWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val displayFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        try {
            val date = try {
                dateFormat.parse(ticket.createdAt)
            } catch (e: Exception) {
                dateFormatWithMillis.parse(ticket.createdAt)
            }
            tvCreatedAt.text = if (date != null) {
                displayFormat.format(date)
            } else {
                ticket.createdAt
            }
        } catch (e: Exception) {
            tvCreatedAt.text = ticket.createdAt
        }

        // 상태 표시
        when (ticket.status) {
            "ANSWERED" -> {
                tvStatus.text = "답변완료"
                tvStatus.setTextColor(0xFF4CAF50.toInt())
                tvStatus.setBackgroundColor(0xFFE8F5E9.toInt())
            }
            "OPEN" -> {
                tvStatus.text = "답변대기"
                tvStatus.setTextColor(0xFFFF9800.toInt())
                tvStatus.setBackgroundColor(0xFFFFE0B2.toInt())
            }
            "CLOSED" -> {
                tvStatus.text = "답변완료"
                tvStatus.setTextColor(0xFF4CAF50.toInt())
                tvStatus.setBackgroundColor(0xFFE8F5E9.toInt())
            }
            else -> {
                tvStatus.text = ticket.status
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
