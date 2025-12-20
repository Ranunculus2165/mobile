package com.example.mobile.ui.mypage

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.OrderHistoryItem
import com.example.mobile.ui.receipt.ReceiptActivity
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter : ListAdapter<OrderHistoryItem, OrderHistoryAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val storeName: TextView = view.findViewById(R.id.tvStoreName)
        val itemDescription: TextView = view.findViewById(R.id.tvItemDescription)
        val orderDate: TextView = view.findViewById(R.id.tvOrderDate)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val btnReceipt: Button = view.findViewById(R.id.btnReceipt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.storeName.text = item.storeName
        holder.itemDescription.text = item.itemDescription

        // 날짜 포맷팅 (2025.11.28 형식)
        // 서버에서 LocalDateTime이 JSON으로 직렬화되면 ISO 8601 형식이 됨
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dateFormatWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val displayFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        try {
            val date = try {
                dateFormat.parse(item.orderDate)
            } catch (e: Exception) {
                dateFormatWithMillis.parse(item.orderDate)
            }
            holder.orderDate.text = if (date != null) {
                "${displayFormat.format(date)} · ${String.format("%,d", item.totalPrice)}원"
            } else {
                "${item.orderDate} · ${String.format("%,d", item.totalPrice)}원"
            }
        } catch (e: Exception) {
            holder.orderDate.text = "${item.orderDate} · ${String.format("%,d", item.totalPrice)}원"
        }

        holder.status.text = item.status

        // 영수증 출력 버튼 클릭 리스너
        holder.btnReceipt.setOnClickListener {
            val intent = Intent(holder.itemView.context, ReceiptActivity::class.java)
            intent.putExtra(ReceiptActivity.EXTRA_ORDER_ID, item.orderId)
            holder.itemView.context.startActivity(intent)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<OrderHistoryItem>() {
        override fun areItemsTheSame(oldItem: OrderHistoryItem, newItem: OrderHistoryItem): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: OrderHistoryItem, newItem: OrderHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}

