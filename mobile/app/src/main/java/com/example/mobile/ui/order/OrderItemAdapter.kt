package com.example.mobile.ui.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.CartItemResponse

class OrderItemAdapter : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    private val items: MutableList<CartItemResponse> = mutableListOf()

    fun submitList(newItems: List<CartItemResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMenuName: TextView = itemView.findViewById(R.id.tvOrderItemName)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvOrderItemQuantity)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvOrderItemPrice)

        fun bind(item: CartItemResponse) {
            tvMenuName.text = item.menuName
            tvQuantity.text = "x${item.quantity}"
            tvPrice.text = String.format("%,d원", item.linePrice)
        }
    }
}

