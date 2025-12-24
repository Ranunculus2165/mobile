package com.example.mobile.ui.receipt

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile.R
import com.example.mobile.data.network.ApiClient
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ReceiptActivity : AppCompatActivity() {

    private lateinit var tvOrderNumber: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvStoreName: TextView
    private lateinit var tvStoreAddress: TextView
    private lateinit var llOrderItems: LinearLayout
    private lateinit var tvOrderAmount: TextView
    private lateinit var tvDeliveryFee: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var tvFlag: TextView

    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        const val EXTRA_ORDER_ID = "order_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        val orderId = intent.getLongExtra(EXTRA_ORDER_ID, -1L)
        if (orderId == -1L) {
            Toast.makeText(this, "주문 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupClickListeners()
        loadReceiptData(orderId)
    }

    private fun initViews() {
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        tvOrderDate = findViewById(R.id.tvOrderDate)
        tvStoreName = findViewById(R.id.tvStoreName)
        tvStoreAddress = findViewById(R.id.tvStoreAddress)
        llOrderItems = findViewById(R.id.llOrderItems)
        tvOrderAmount = findViewById(R.id.tvOrderAmount)
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod)
        tvFlag = findViewById(R.id.tvFlag)
    }

    private fun setupClickListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnPrint).setOnClickListener {
            // TODO: 실제 프린터 출력 기능 구현
            Toast.makeText(this, "출력 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadReceiptData(orderId: Long) {
        uiScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.orderApi.getOrderDetail(orderId)
                }

                displayReceipt(response)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ReceiptActivity,
                    "영수증 정보를 불러오는데 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayReceipt(response: com.example.mobile.data.model.OrderDetailResponse) {
        // 주문번호
        tvOrderNumber.text = "주문번호: ${response.orderNumber}"

        // 주문일시 포맷팅
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dateFormatWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val displayFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        try {
            val date = try {
                dateFormat.parse(response.createdAt)
            } catch (e: Exception) {
                dateFormatWithMillis.parse(response.createdAt)
            }
            tvOrderDate.text = "주문일시: ${if (date != null) displayFormat.format(date) else response.createdAt}"
        } catch (e: Exception) {
            tvOrderDate.text = "주문일시: ${response.createdAt}"
        }

        // 매장 정보
        tvStoreName.text = response.storeName
        tvStoreAddress.text = response.storeAddress

        // 주문 내역
        llOrderItems.removeAllViews()
        response.items.forEach { item ->
            val itemView = layoutInflater.inflate(R.layout.item_receipt_order_item, llOrderItems, false)
            val tvItemName = itemView.findViewById<TextView>(R.id.tvItemName)
            val tvItemPrice = itemView.findViewById<TextView>(R.id.tvItemPrice)

            tvItemName.text = "${item.menuName} x ${item.quantity}"
            tvItemPrice.text = String.format("%,d원", item.unitPrice * item.quantity)

            llOrderItems.addView(itemView)
        }

        // 결제 요약
        tvOrderAmount.text = String.format("%,d원", response.orderAmount)
        tvDeliveryFee.text = String.format("%,d원", response.deliveryFee)
        tvTotalPrice.text = String.format("%,d원", response.totalPrice)
        tvPaymentMethod.text = "신용카드" // 기본값

        // FLAG
        val flagValue = response.receiptFlag ?: ""
        tvFlag.text = "FLAG = \"$flagValue\""
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
