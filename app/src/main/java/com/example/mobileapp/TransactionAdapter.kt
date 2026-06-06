package com.example.financeapp

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private var transactions: MutableList<Transaction>,
    private val onDeleteClick: (Transaction, Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvCategory.text = transaction.category
        val sign = if (transaction.type == "expense") "-" else "+"
        holder.tvAmount.text = "$sign ${String.format("%.2f", transaction.amount)} ₽"
        holder.tvAmount.setTextColor(
            if (transaction.type == "expense")
                holder.itemView.context.resources.getColor(android.R.color.holo_red_dark)
            else
                holder.itemView.context.resources.getColor(android.R.color.holo_green_dark)
        )

        holder.btnDelete.setOnClickListener {
            onDeleteClick(transaction, position)
        }
    }

    override fun getItemCount() = transactions.size

    fun updateList(newList: MutableList<Transaction>) {
        transactions = newList
        notifyDataSetChanged()
    }
}