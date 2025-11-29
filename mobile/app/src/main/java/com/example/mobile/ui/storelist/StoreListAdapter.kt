package com.example.mobile.ui.storelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.Store

class StoreListAdapter(
    private val onStoreClick: (Store) -> Unit
) : RecyclerView.Adapter<StoreListAdapter.StoreViewHolder>() {

    private val items: MutableList<Store> = mutableListOf()

    fun submitList(newItems: List<Store>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_store, parent, false)
        return StoreViewHolder(view, onStoreClick)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class StoreViewHolder(
        itemView: View,
        private val onStoreClick: (Store) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvStoreName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStoreStatus)
        private val tvMinOrderAndTime: TextView = itemView.findViewById(R.id.tvStoreMinOrder)

        fun bind(item: Store) {
            tvName.text = item.name

            tvStatus.text = when (item.status) {
                "OPEN" -> "영업 중"
                "CLOSED" -> "영업 종료"
                "PREPARING" -> "준비 중"
                else -> "상태 알 수 없음"
            }

            val minOrderText = String.format("최소주문 %,d원", item.minOrderAmount)
            tvMinOrderAndTime.text = "$minOrderText · ${item.estimatedDeliveryTime}"

            itemView.setOnClickListener {
                onStoreClick(item)
            }
        }
    }
}
