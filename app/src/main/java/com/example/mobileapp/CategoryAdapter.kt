package com.example.financeapp

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private var categories: List<String>,
    private var selectedIndex: Int,
    private val onItemClick: (String, Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategoryName)
        val cardView: CardView = itemView.findViewById(R.id.cardCategory)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.tvCategory.text = category

        if (position == selectedIndex) {
            holder.cardView.setCardBackgroundColor(
                holder.itemView.context.resources.getColor(android.R.color.holo_blue_light)
            )
            holder.tvCategory.setTextColor(android.graphics.Color.WHITE)
        } else {
            holder.cardView.setCardBackgroundColor(
                holder.itemView.context.resources.getColor(android.R.color.white)
            )
            holder.tvCategory.setTextColor(android.graphics.Color.BLACK)
        }

        holder.itemView.setOnClickListener {
            if (selectedIndex != position) {
                selectedIndex = position
                notifyDataSetChanged()
                onItemClick(category, position)
            }
        }
    }

    override fun getItemCount() = categories.size
}