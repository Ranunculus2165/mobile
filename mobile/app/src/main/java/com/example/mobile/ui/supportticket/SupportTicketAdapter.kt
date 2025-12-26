package com.example.mobile.ui.supportticket

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.SupportTicketResponse
import java.text.SimpleDateFormat
import java.util.*

class SupportTicketAdapter : ListAdapter<SupportTicketResponse, SupportTicketAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvStoreName: TextView = view.findViewById(R.id.tvStoreName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_support_ticket, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.tvTitle.text = item.title
        holder.tvStoreName.text = item.storeName

        // 클릭 리스너 추가
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, SupportTicketDetailActivity::class.java)
            intent.putExtra(SupportTicketDetailActivity.EXTRA_TICKET_ID, item.id)
            holder.itemView.context.startActivity(intent)
        }

        // 날짜 포맷팅
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dateFormatWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val displayFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        try {
            val date = try {
                dateFormat.parse(item.createdAt)
            } catch (e: Exception) {
                dateFormatWithMillis.parse(item.createdAt)
            }
            holder.tvDate.text = if (date != null) {
                displayFormat.format(date)
            } else {
                item.createdAt
            }
        } catch (e: Exception) {
            holder.tvDate.text = item.createdAt
        }

        // 상태 표시
        when (item.status) {
            "ANSWERED" -> {
                holder.tvStatus.text = "답변완료"
                holder.tvStatus.setTextColor(Color.parseColor("#0F766E"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_store_status_open)
            }
            "OPEN" -> {
                holder.tvStatus.text = "답변대기"
                holder.tvStatus.setTextColor(Color.parseColor("#C2410C"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_store_status_closed)
            }
            "CLOSED" -> {
                holder.tvStatus.text = "답변완료"
                holder.tvStatus.setTextColor(Color.parseColor("#0F766E"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_store_status_open)
            }
            else -> {
                holder.tvStatus.text = item.status
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SupportTicketResponse>() {
        override fun areItemsTheSame(oldItem: SupportTicketResponse, newItem: SupportTicketResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SupportTicketResponse, newItem: SupportTicketResponse): Boolean {
            return oldItem == newItem
        }
    }
}
