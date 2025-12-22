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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class OrderActivity : BaseActivity() {

    companion object {
        const val EXTRA_CART = "extra_cart"
    }

    private lateinit var tvStoreName: TextView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var etRequest: EditText
    private lateinit var rvOrderItems: RecyclerView
    private lateinit var tvOrderAmount: TextView
    private lateinit var tvDeliveryFee: TextView
    private lateinit var tvAvailablePoint: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvRemainingPoint: TextView
    private lateinit var btnPay: Button
    private lateinit var orderAdapter: OrderItemAdapter

    private var cart: CartResponse? = null
    private var deliveryFee: Int = 3000 // 더미 배달료
    private var availablePoint: Int = 0 // 보유 포인트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        // View 연결
        tvStoreName = findViewById(R.id.tvOrderStoreName)
        tvPaymentMethod = findViewById(R.id.tvOrderPaymentMethod)
        etRequest = findViewById(R.id.etOrderRequest)
        rvOrderItems = findViewById(R.id.rvOrderItems)
        tvOrderAmount = findViewById(R.id.tvOrderAmount)
        tvDeliveryFee = findViewById(R.id.tvOrderDeliveryFee)
        tvAvailablePoint = findViewById(R.id.tvAvailablePoint)
        tvTotalPrice = findViewById(R.id.tvOrderTotalPrice)
        tvRemainingPoint = findViewById(R.id.tvOrderRemainingPoint)
        btnPay = findViewById(R.id.btnOrderPay)

        // 뒤로가기 버튼
        findViewById<android.view.View>(R.id.btnOrderBack).setOnClickListener {
            finish()
        }

        // 인텐트에서 장바구니 정보 받기
        val cartJson = intent.getStringExtra(EXTRA_CART)
        if (cartJson != null) {
            try {
                cart = Gson().fromJson(cartJson, CartResponse::class.java)
                loadUserPointAndSetupOrder()
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

    private fun loadUserPointAndSetupOrder() {
        lifecycleScope.launch {
            try {
                // 회원 정보에서 포인트 가져오기 (IO 스레드에서 실행)
                val myPageResponse = withContext(Dispatchers.IO) {
                    ApiClient.myPageApi.getMyPage().execute().body()
                }
                if (myPageResponse != null) {
                    availablePoint = myPageResponse.point
                } else {
                    Log.e("OrderActivity", "회원 정보를 불러올 수 없습니다")
                    Toast.makeText(
                        this@OrderActivity,
                        "회원 정보를 불러오는데 실패했습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }
            } catch (e: Exception) {
                Log.e("OrderActivity", "포인트 조회 실패", e)
                Toast.makeText(
                    this@OrderActivity,
                    "포인트 정보를 불러오는데 실패했습니다",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            setupOrderInfo()
        }
    }

    private fun setupOrderInfo() {
        cart?.let { cartData ->
            // 가게 정보
            tvStoreName.text = cartData.storeName

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
            val remainingPoint = availablePoint - totalPrice

            // 주문 금액, 배달료 표시
            tvOrderAmount.text = String.format("%,d원", orderAmount)
            tvDeliveryFee.text = String.format("%,d원", deliveryFee)

            // 보유 포인트 표시
            tvAvailablePoint.text = String.format("+ %,dP", availablePoint)

            // 총 결제 금액 표시 (포인트 형식)
            tvTotalPrice.text = String.format("- %,dP", totalPrice)

            // 잔여 포인트 표시
            tvRemainingPoint.text = if (remainingPoint >= 0) {
                String.format("%,dP", remainingPoint)
            } else {
                String.format("-%sP", String.format("%,d", kotlin.math.abs(remainingPoint)))
            }
            if (remainingPoint < 0) {
                tvRemainingPoint.setTextColor(android.graphics.Color.parseColor("#20B2AA"))
            } else {
                tvRemainingPoint.setTextColor(android.graphics.Color.parseColor("#666666"))
            }

            // 결제 버튼 텍스트 업데이트
            btnPay.text = "${String.format("%,d", totalPrice)}P 결제하기"

            // 포인트 잔액 확인
            if (availablePoint < totalPrice) {
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
                        e.response()?.errorBody()?.string() ?: ""
                    } catch (ex: Exception) {
                        ""
                    }
                    Log.e("OrderActivity", "주문 실패: ${e.code()}", e)
                    Log.e("OrderActivity", "에러 응답: $errorBody")

                    // 에러 메시지에 "포인트가 부족합니다" 또는 "포인트가 부족"이 포함되어 있는지 확인
                    val isInsufficientPoint = errorBody.contains("포인트가 부족", ignoreCase = true) ||
                            errorBody.contains("잔여 포인트", ignoreCase = true)

                    val errorMessage = when {
                        isInsufficientPoint -> {
                            // 포인트 부족 에러 메시지 추출 또는 기본 메시지
                            val pointMessage = if (errorBody.isNotEmpty()) {
                                // 서버에서 보낸 메시지에서 포인트 정보 추출 시도
                                errorBody
                            } else {
                                "포인트가 부족합니다. 잔여 포인트를 확인해주세요."
                            }
                            pointMessage
                        }
                        e.code() == 400 -> "주문 요청이 잘못되었습니다. 다시 시도해주세요."
                        e.code() == 500 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                        else -> "주문 처리 중 오류가 발생했습니다. (오류 코드: ${e.code()})"
                    }

                    Toast.makeText(
                        this@OrderActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()

                    btnPay.isEnabled = true
                    val totalPrice = cartData.totalPrice + deliveryFee
                    btnPay.text = "${String.format("%,d", totalPrice)}P 결제하기"

                } catch (e: Exception) {
                    Log.e("OrderActivity", "주문 실패", e)
                    Toast.makeText(
                        this@OrderActivity,
                        "주문 처리 중 오류가 발생했습니다: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                    btnPay.isEnabled = true
                    val totalPrice = cartData.totalPrice + deliveryFee
                    btnPay.text = "${String.format("%,d", totalPrice)}P 결제하기"
                }
            }
        }
    }
}

