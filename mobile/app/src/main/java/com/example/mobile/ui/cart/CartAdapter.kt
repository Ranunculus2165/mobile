package com.example.mobile.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.CartItemResponse

class CartAdapter(
    private val lifecycleScope: LifecycleCoroutineScope,
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
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvCartItemQuantity)
        private val tvLinePrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)

        fun bind(item: CartItemResponse) {
            tvMenuName.text = item.menuName
            tvQuantity.text = "${item.quantity}개"
            tvLinePrice.text = String.format("%,d원", item.linePrice)
        }
    }
}

