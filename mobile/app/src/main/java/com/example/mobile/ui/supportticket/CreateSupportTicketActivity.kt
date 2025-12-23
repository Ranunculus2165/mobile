package com.example.mobile.ui.supportticket

import android.os.Bundle
import android.widget.*
import com.example.mobile.R
import com.example.mobile.data.model.CreateSupportTicketRequest
import com.example.mobile.data.model.Store
import com.example.mobile.data.network.ApiClient
import com.example.mobile.ui.base.BaseActivity
import kotlinx.coroutines.*
import java.util.*

class CreateSupportTicketActivity : BaseActivity() {

    private lateinit var spinnerStore: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnSubmit: Button

    private var stores: List<Store> = emptyList()
    private var selectedStoreId: Long = -1L

    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_support_ticket)

        spinnerStore = findViewById(R.id.spinnerStore)
        etTitle = findViewById(R.id.etTitle)
        etMessage = findViewById(R.id.etMessage)
        btnSubmit = findViewById(R.id.btnSubmit)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            submitTicket()
        }

        loadStores()
    }

    private fun loadStores() {
        uiScope.launch {
            try {
                stores = withContext(Dispatchers.IO) {
                    ApiClient.storeApi.getStores()
                }

                if (stores.isEmpty()) {
                    Toast.makeText(
                        this@CreateSupportTicketActivity,
                        "가게 목록을 불러올 수 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                // 스피너 어댑터 설정
                val storeNames = stores.map { it.name }
                val adapter = ArrayAdapter(
                    this@CreateSupportTicketActivity,
                    android.R.layout.simple_spinner_item,
                    storeNames
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerStore.adapter = adapter

                // 첫 번째 가게를 기본 선택
                if (stores.isNotEmpty()) {
                    selectedStoreId = stores[0].id
                }

                spinnerStore.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        if (position < stores.size) {
                            selectedStoreId = stores[position].id
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@CreateSupportTicketActivity,
                    "가게 목록을 불러오는데 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun submitTicket() {
        val title = etTitle.text.toString().trim()
        val message = etMessage.text.toString().trim()

        if (selectedStoreId == -1L) {
            Toast.makeText(this, "가게를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            etTitle.requestFocus()
            return
        }

        if (message.isEmpty()) {
            Toast.makeText(this, "문의 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            etMessage.requestFocus()
            return
        }

        uiScope.launch {
            try {
                val request = CreateSupportTicketRequest(
                    storeId = selectedStoreId,
                    title = title,
                    message = message
                )

                val response = withContext(Dispatchers.IO) {
                    ApiClient.myPageApi.createSupportTicket(request).execute()
                }

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@CreateSupportTicketActivity,
                        "문의가 등록되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@CreateSupportTicketActivity,
                        "문의 등록에 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@CreateSupportTicketActivity,
                    "문의 등록에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
