package com.example.mobile.ui.cart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.CartResponse
import com.example.mobile.data.network.ApiClient
import com.example.mobile.ui.base.BaseActivity
import com.example.mobile.ui.order.OrderActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch

class CartActivity : BaseActivity() {

    private lateinit var tvStoreName: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var rvCartItems: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var btnOrder: Button

    private var cart: CartResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // View 연결
        tvStoreName = findViewById(R.id.tvCartStoreName)
        tvTotalPrice = findViewById(R.id.tvCartTotalPrice)
        rvCartItems = findViewById(R.id.rvCartItems)
        btnOrder = findViewById(R.id.btnOrder)

        // 리사이클러뷰 세팅
        cartAdapter = CartAdapter(lifecycleScope) { cartItemId ->
            removeCartItem(cartItemId)
        }
        rvCartItems.layoutManager = LinearLayoutManager(this)
        rvCartItems.adapter = cartAdapter

        // 뒤로가기 버튼
        findViewById<android.view.View>(R.id.btnCartBack).setOnClickListener {
            finish()
        }

        // 주문하기 버튼
        btnOrder.setOnClickListener {
            openOrderActivity()
        }

        // 장바구니 불러오기
        loadCart()
    }

    private fun loadCart() {
        lifecycleScope.launch {
            try {
                val cartData = ApiClient.cartApi.getMyCart()
                if (cartData != null) {
                    cart = cartData
                    tvStoreName.text = cartData.storeName
                    tvTotalPrice.text = String.format("%,d원", cartData.totalPrice)
                    cartAdapter.submitList(cartData.items)
                } else {
                    Toast.makeText(
                        this@CartActivity,
                        "장바구니가 비어있습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    // 장바구니가 비어있음
                    Toast.makeText(
                        this@CartActivity,
                        "장바구니가 비어있습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Log.e("CartActivity", "장바구니 불러오기 실패", e)
                    Toast.makeText(
                        this@CartActivity,
                        "장바구니를 불러오는데 실패했습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("CartActivity", "장바구니 불러오기 실패", e)
                Toast.makeText(
                    this@CartActivity,
                    "장바구니를 불러오는데 실패했습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun removeCartItem(cartItemId: Long) {
        lifecycleScope.launch {
            try {
                // TODO: 삭제 API 호출 구현 필요
                // ApiClient.cartApi.removeItem(cartItemId)
                // 장바구니 다시 불러오기
                loadCart()
            } catch (e: Exception) {
                Log.e("CartActivity", "장바구니 항목 삭제 실패", e)
            }
        }
    }

    private fun openOrderActivity() {
        cart?.let { cartData ->
            val intent = Intent(this, OrderActivity::class.java).apply {
                // 장바구니 정보를 JSON으로 전달
                val cartJson = Gson().toJson(cartData)
                putExtra(OrderActivity.EXTRA_CART, cartJson)
            }
            startActivity(intent)
        } ?: run {
            Toast.makeText(
                this,
                "장바구니 정보를 불러올 수 없습니다",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

