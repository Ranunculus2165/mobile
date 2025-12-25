package com.example.mobile.ui.storelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.Store
import java.text.NumberFormat
import java.util.Locale

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
        private val tvEta: TextView = itemView.findViewById(R.id.tvStoreMinOrder)
        private val tvDeliveryFee: TextView = itemView.findViewById(R.id.tvStoreDeliveryFee)

        fun bind(item: Store) {
            tvName.text = item.name

            // (CTF/데모) 배달 예상 시간은 더미로 표시
            tvEta.text = "25-35분"

            val nf = NumberFormat.getNumberInstance(Locale.KOREA)
            val delivery = nf.format(item.deliveryTip)
            tvDeliveryFee.text = if (item.deliveryTip <= 0) "배달비 무료" else "배달비 ${delivery}원"

            itemView.setOnClickListener {
                onStoreClick(item)
            }
        }
    }
}
