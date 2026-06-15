package com.example.financeapp

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryStatsAdapter(
    private val categories: List<Pair<String, Double>>,
    private val totalExpense: Double
) : RecyclerView.Adapter<CategoryStatsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvStatsCategory)
        val tvAmount: TextView = itemView.findViewById(R.id.tvStatsAmount)
        val tvPercentage: TextView = itemView.findViewById(R.id.tvStatsPercentage)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarStats)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_stats, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (category, amount) = categories[position]
        val percentage = if (totalExpense > 0) (amount / totalExpense * 100) else 0.0

        holder.tvCategory.text = category
        holder.tvAmount.text = String.format("%.2f ₽", amount)
        holder.tvPercentage.text = String.format("%.1f%%", percentage)
        holder.progressBar.progress = percentage.toInt()
    }

    override fun getItemCount() = categories.size
}