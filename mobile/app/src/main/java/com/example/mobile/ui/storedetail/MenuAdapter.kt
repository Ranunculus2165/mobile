package com.example.mobile.ui.storedetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.MenuItem

class MenuAdapter : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private val items: MutableList<MenuItem> = mutableListOf()

    fun submitList(newItems: List<MenuItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvMenuName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvMenuDesc)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvMenuPrice)

        fun bind(item: MenuItem) {
            tvName.text = item.name
            tvDesc.text = item.description ?: ""
            tvPrice.text = String.format("%,d원", item.price)
        }
    }
}
