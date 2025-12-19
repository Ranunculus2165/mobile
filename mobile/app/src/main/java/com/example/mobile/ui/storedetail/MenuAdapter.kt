package com.example.mobile.ui.storedetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.MenuItem
import com.example.mobile.data.network.ApiClient
import kotlinx.coroutines.launch

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

        private fun addToCart(item: MenuItem) {
            lifecycleScope.launch {
                try {
                    val request = com.example.mobile.data.model.CartItemRequest(
                        storeId = storeId,
                        menuId = item.id,
                        quantity = 1
                    )
                    android.util.Log.d("MenuAdapter", "=== 장바구니 추가 요청 시작 ===")
                    android.util.Log.d("MenuAdapter", "요청 데이터: storeId=$storeId, menuId=${item.id}, menuName=${item.name}, quantity=1")
                    android.util.Log.d("MenuAdapter", "요청 JSON: ${com.google.gson.Gson().toJson(request)}")
                    
                    val response = ApiClient.cartApi.addItemToCart(request)
                    android.util.Log.d("MenuAdapter", "장바구니 추가 성공: $response")
                    // 장바구니 업데이트 콜백 호출
                    onCartUpdated?.invoke()
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
                    
                    val errorMessage = when (e.code()) {
                        500 -> "서버 내부 오류가 발생했습니다. 서버 로그를 확인해주세요.\n에러: $errorBody"
                        404 -> "요청한 리소스를 찾을 수 없습니다."
                        400 -> "잘못된 요청입니다: $errorBody"
                        else -> "서버 오류 (${e.code()}): $errorBody"
                    }
                    
                    Toast.makeText(
                        itemView.context,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
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
    }
}
