package com.example.mobile.ui.storedetail

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.CartResponse
import com.example.mobile.data.model.MenuItem
import com.example.mobile.data.network.ApiClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenuAdapter(
    private val storeId: Long,
    private val lifecycleScope: androidx.lifecycle.LifecycleCoroutineScope,
    private val onCartUpdated: (() -> Unit)? = null
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private val items: MutableList<MenuItem> = mutableListOf()

    fun submitList(newItems: List<MenuItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view, storeId, lifecycleScope, onCartUpdated)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class MenuViewHolder(
        itemView: View,
        private val storeId: Long,
        private val lifecycleScope: androidx.lifecycle.LifecycleCoroutineScope,
        private val onCartUpdated: (() -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvMenuName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvMenuDesc)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvMenuPrice)
        private val btnAddToCart: Button = itemView.findViewById(R.id.btnAddToCart)

        fun bind(item: MenuItem) {
            tvName.text = item.name
            tvDesc.text = item.description ?: ""
            tvPrice.text = String.format("%,d원", item.price)

            btnAddToCart.setOnClickListener {
                addToCart(item)
            }
        }

        private fun addToCart(item: MenuItem, force: Boolean = false) {
            lifecycleScope.launch {
                try {
                    val request = com.example.mobile.data.model.CartItemRequest(
                        storeId = storeId,
                        menuId = item.id,
                        quantity = 1
                    )
                    android.util.Log.d("MenuAdapter", "=== 장바구니 추가 요청 시작 ===")
                    android.util.Log.d("MenuAdapter", "요청 데이터: storeId=$storeId, menuId=${item.id}, menuName=${item.name}, quantity=1, force=$force")
                    android.util.Log.d("MenuAdapter", "요청 JSON: ${com.google.gson.Gson().toJson(request)}")
                    
                    val response = ApiClient.cartApi.addItemToCart(request, force)
                    android.util.Log.d("MenuAdapter", "장바구니 추가 성공: $response")
                    
                    // 메인 스레드에서 Toast 표시 및 콜백 호출
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            itemView.context,
                            "장바구니에 추가되었습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                        // 장바구니 업데이트 콜백 호출
                        onCartUpdated?.invoke()
                    }
                } catch (e: retrofit2.HttpException) {
                    // HTTP 에러 응답 처리
                    val errorBody = try {
                        e.response()?.errorBody()?.string()
                    } catch (ex: Exception) {
                        "에러 본문 읽기 실패: ${ex.message}"
                    }
                    android.util.Log.e("MenuAdapter", "=== HTTP 에러 발생 ===")
                    android.util.Log.e("MenuAdapter", "HTTP 상태 코드: ${e.code()}")
                    android.util.Log.e("MenuAdapter", "에러 응답 본문: $errorBody")
                    android.util.Log.e("MenuAdapter", "요청 URL: ${e.response()?.raw()?.request?.url}")
                    android.util.Log.e("MenuAdapter", "스택 트레이스:", e)
                    
                    when (e.code()) {
                        409 -> {
                            // 409 Conflict: 다른 가게의 장바구니가 존재할 때 팝업 표시
                            try {
                                val gson = Gson()
                                val errorJson = gson.fromJson(errorBody, Map::class.java) as? Map<*, *>
                                val existingCartJson = errorJson?.get("existingCart") as? Map<*, *>
                                
                                if (existingCartJson != null) {
                                    val existingCart = gson.fromJson(
                                        gson.toJson(existingCartJson),
                                        CartResponse::class.java
                                    )
                                    // 메인 스레드에서 다이얼로그 표시
                                    withContext(Dispatchers.Main) {
                                        showCartConflictDialog(item, existingCart)
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            itemView.context,
                                            "다른 가게의 장바구니가 이미 존재합니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (ex: Exception) {
                                android.util.Log.e("MenuAdapter", "409 에러 파싱 실패", ex)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        itemView.context,
                                        "다른 가게의 장바구니가 이미 존재합니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        500 -> {
                            Toast.makeText(
                                itemView.context,
                                "서버 내부 오류가 발생했습니다. 서버 로그를 확인해주세요.\n에러: $errorBody",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        404 -> {
                            Toast.makeText(
                                itemView.context,
                                "요청한 리소스를 찾을 수 없습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        400 -> {
                            Toast.makeText(
                                itemView.context,
                                "잘못된 요청입니다: $errorBody",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                itemView.context,
                                "서버 오류 (${e.code()}): $errorBody",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    android.util.Log.e("MenuAdapter", "타임아웃 오류", e)
                    Toast.makeText(
                        itemView.context,
                        "서버 연결 시간이 초과되었습니다. 네트워크를 확인해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: java.net.UnknownHostException) {
                    android.util.Log.e("MenuAdapter", "호스트 연결 오류", e)
                    Toast.makeText(
                        itemView.context,
                        "서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    android.util.Log.e("MenuAdapter", "장바구니 추가 오류", e)
                    e.printStackTrace()
                    Toast.makeText(
                        itemView.context,
                        "장바구니 추가 실패: ${e.message ?: "알 수 없는 오류"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        private fun showCartConflictDialog(item: MenuItem, existingCart: CartResponse) {
            AlertDialog.Builder(itemView.context)
                .setTitle("장바구니 변경")
                .setMessage(
                    "${existingCart.storeName}의 장바구니에 ${existingCart.items.size}개의 상품이 담겨있습니다.\n" +
                            "기존 장바구니를 비우고 새로운 장바구니를 생성하시겠습니까?"
                )
                .setPositiveButton("확인") { _, _ ->
                    // 사용자가 수락하면 force=true로 재호출
                    addToCart(item, force = true)
                }
                .setNegativeButton("취소") { dialog, _ ->
                    // 사용자가 거부하면 화면에 머무름 (아무 동작 없음)
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
        }
    }
}
