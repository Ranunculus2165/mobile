package com.example.mobile.ui.cart

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.network.ApiClient
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {

    private lateinit var tvStoreName: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var rvCartItems: RecyclerView
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // View 연결
        tvStoreName = findViewById(R.id.tvCartStoreName)
        tvTotalPrice = findViewById(R.id.tvCartTotalPrice)
        rvCartItems = findViewById(R.id.rvCartItems)

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

        // 장바구니 불러오기
        loadCart()
    }

    private fun loadCart() {
        lifecycleScope.launch {
            try {
                val cart = ApiClient.cartApi.getMyCart()
                if (cart != null) {
                    tvStoreName.text = cart.storeName
                    tvTotalPrice.text = String.format("%,d원", cart.totalPrice)
                    cartAdapter.submitList(cart.items)
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
}

