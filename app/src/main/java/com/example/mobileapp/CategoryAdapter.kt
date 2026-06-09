package com.example.financeapp

import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private var categories: List<String>,
    private var selectedIndex: Int,
    private val onItemClick: (String, Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategoryName)
        val rbSelect: RadioButton = itemView.findViewById(R.id.rbSelect)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.tvCategory.text = category
        holder.rbSelect.isChecked = (position == selectedIndex)

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