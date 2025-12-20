package com.example.mobile.ui.order

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.CartResponse
import com.example.mobile.data.network.ApiClient
import com.example.mobile.ui.base.BaseActivity
import com.example.mobile.ui.storelist.StoreListActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch

class OrderActivity : BaseActivity() {

    companion object {
        const val EXTRA_CART = "extra_cart"
    }

    private lateinit var tvDeliveryAddress: TextView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var etRequest: EditText
    private lateinit var rvOrderItems: RecyclerView
    private lateinit var tvOrderAmount: TextView
    private lateinit var tvDeliveryFee: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvRemainingPoint: TextView
    private lateinit var btnPay: Button
    private lateinit var orderAdapter: OrderItemAdapter

    private var cart: CartResponse? = null
    private var deliveryFee: Int = 3000 // 더미 배달료
    private var remainingPoint: Int = 50000 // 더미 포인트 잔액

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        // View 연결
        tvDeliveryAddress = findViewById(R.id.tvOrderDeliveryAddress)
        tvPaymentMethod = findViewById(R.id.tvOrderPaymentMethod)
        etRequest = findViewById(R.id.etOrderRequest)
        rvOrderItems = findViewById(R.id.rvOrderItems)
        tvOrderAmount = findViewById(R.id.tvOrderAmount)
        tvDeliveryFee = findViewById(R.id.tvOrderDeliveryFee)
        tvTotalPrice = findViewById(R.id.tvOrderTotalPrice)
        tvRemainingPoint = findViewById(R.id.tvOrderRemainingPoint)
        btnPay = findViewById(R.id.btnOrderPay)

        // 뒤로가기 버튼
        findViewById<android.view.View>(R.id.btnOrderBack).setOnClickListener {
            finish()
        }

        // 배달 주소 변경 버튼 (현재는 기능 없음)
        findViewById<android.view.View>(R.id.btnChangeAddress).setOnClickListener {
            Toast.makeText(this, "주소 변경 기능은 준비 중입니다", Toast.LENGTH_SHORT).show()
        }

        // 인텐트에서 장바구니 정보 받기
        val cartJson = intent.getStringExtra(EXTRA_CART)
        if (cartJson != null) {
            try {
                cart = Gson().fromJson(cartJson, CartResponse::class.java)
                setupOrderInfo()
            } catch (e: Exception) {
                Log.e("OrderActivity", "장바구니 정보 파싱 실패", e)
                Toast.makeText(this, "주문 정보를 불러오는데 실패했습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this, "주문 정보가 없습니다", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 결제 버튼 클릭
        btnPay.setOnClickListener {
            processPayment()
        }
    }

    private fun setupOrderInfo() {
        cart?.let { cartData ->
            // 배달 주소 (더미 데이터)
            tvDeliveryAddress.text = "서울시 강남구 테헤란로 123, 101호"

            // 결제 수단 (포인트로 고정)
            tvPaymentMethod.text = "포인트"

            // 주문 상품 목록
            orderAdapter = OrderItemAdapter()
            rvOrderItems.layoutManager = LinearLayoutManager(this)
            rvOrderItems.adapter = orderAdapter
            orderAdapter.submitList(cartData.items)

            // 결제 정보 계산
            val orderAmount = cartData.totalPrice
            val totalPrice = orderAmount + deliveryFee

            tvOrderAmount.text = String.format("%,d원", orderAmount)
            tvDeliveryFee.text = String.format("%,d원", deliveryFee)
            tvTotalPrice.text = String.format("%,d원", totalPrice)
            tvRemainingPoint.text = String.format("잔여 포인트: %,d원", remainingPoint)

            // 결제 버튼 텍스트 업데이트
            btnPay.text = "${String.format("%,d", totalPrice)}원 결제하기"

            // 포인트 잔액 확인
            if (remainingPoint < totalPrice) {
                btnPay.isEnabled = false
                btnPay.text = "포인트가 부족합니다"
                btnPay.setBackgroundColor(android.graphics.Color.parseColor("#CCCCCC"))
            } else {
                btnPay.isEnabled = true
                btnPay.setBackgroundColor(android.graphics.Color.parseColor("#20B2AA"))
            }
        }
    }

    private fun processPayment() {
        cart?.let { cartData ->
            lifecycleScope.launch {
                try {
                    btnPay.isEnabled = false
                    btnPay.text = "결제 중..."

                    val request = com.example.mobile.data.model.OrderRequest(
                        cartId = cartData.cartId
                    )

                    val response = ApiClient.orderApi.createOrder(request)

                    // 주문 성공
                    Toast.makeText(
                        this@OrderActivity,
                        "주문이 완료되었습니다!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 주문 완료 후 홈 화면으로 이동
                    val intent = Intent(this@OrderActivity, StoreListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()

                } catch (e: retrofit2.HttpException) {
                    val errorBody = try {
                        e.response()?.errorBody()?.string()
                    } catch (ex: Exception) {
                        "에러 본문 읽기 실패: ${ex.message}"
                    }
                    Log.e("OrderActivity", "주문 실패: ${e.code()}", e)
                    Log.e("OrderActivity", "에러 응답: $errorBody")

                    val errorMessage = when (e.code()) {
                        400 -> "주문 요청이 잘못되었습니다: $errorBody"
                        500 -> "서버 오류가 발생했습니다"
                        else -> "주문 실패: ${e.code()}"
                    }

                    Toast.makeText(
                        this@OrderActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()

                    btnPay.isEnabled = true
                    btnPay.text = "${String.format("%,d", cartData.totalPrice + deliveryFee)}원 결제하기"

                } catch (e: Exception) {
                    Log.e("OrderActivity", "주문 실패", e)
                    Toast.makeText(
                        this@OrderActivity,
                        "주문 처리 중 오류가 발생했습니다: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                    btnPay.isEnabled = true
                    btnPay.text = "${String.format("%,d", cartData.totalPrice + deliveryFee)}원 결제하기"
                }
            }
        }
    }
}

