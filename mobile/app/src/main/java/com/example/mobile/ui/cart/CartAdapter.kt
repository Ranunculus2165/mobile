package com.example.mobile.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.CartItemResponse

class CartAdapter(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val onQuantityChange: (Long, Int) -> Unit,
    private val onRemoveClick: (Long) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartItemViewHolder>() {

    private val items: MutableList<CartItemResponse> = mutableListOf()

    fun submitList(newItems: List<CartItemResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMenuName: TextView = itemView.findViewById(R.id.tvCartItemName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvCartItemDesc)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvCartItemQuantity)
        private val tvLinePrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)
        private val btnIncreaseQuantity: Button = itemView.findViewById(R.id.btnIncreaseQuantity)
        private val btnDecreaseQuantity: Button = itemView.findViewById(R.id.btnDecreaseQuantity)
        private val btnRemoveItem: ImageButton = itemView.findViewById(R.id.btnRemoveItem)

        fun bind(item: CartItemResponse) {
            tvMenuName.text = item.menuName
            tvDesc.text = String.format("%,d원 x %d개", item.unitPrice, item.quantity)
            tvQuantity.text = item.quantity.toString()
            tvLinePrice.text = String.format("%,d원", item.linePrice)

            // 수량 증가 버튼
            btnIncreaseQuantity.setOnClickListener {
                val newQuantity = item.quantity + 1
                onQuantityChange(item.cartItemId, newQuantity)
            }

            // 수량 감소 버튼
            btnDecreaseQuantity.setOnClickListener {
                val newQuantity = item.quantity - 1
                if (newQuantity > 0) {
                    onQuantityChange(item.cartItemId, newQuantity)
                } else {
                    // 수량이 0이 되면 삭제
                    onRemoveClick(item.cartItemId)
                }
            }

            // 삭제 버튼
            btnRemoveItem.setOnClickListener {
                onRemoveClick(item.cartItemId)
            }
        }
    }
}

