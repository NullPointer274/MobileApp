package com.example.financeapp

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private var transactions: MutableList<Transaction>,
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvNote: TextView = itemView.findViewById(R.id.tvNote)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]

        val displayName = if (transaction.name.isNotEmpty()) transaction.name else transaction.category
        holder.tvName.text = displayName
        holder.tvCategory.text = transaction.category

        if (transaction.note.isNotEmpty()) {
            holder.tvNote.text = transaction.note
            holder.tvNote.visibility = View.VISIBLE
        } else {
            holder.tvNote.visibility = View.GONE
        }
        holder.tvDate.text = dateFormat.format(transaction.date)

        val sign = if (transaction.type == "expense") "-" else "+"
        holder.tvAmount.text = "$sign ${String.format("%.2f", transaction.amount)} ₽"
        holder.tvAmount.setTextColor(
            if (transaction.type == "expense")
                holder.itemView.context.resources.getColor(android.R.color.holo_red_dark)
            else
                holder.itemView.context.resources.getColor(android.R.color.holo_green_dark)
        )

        holder.btnEdit.setOnClickListener {
            onEditClick(transaction)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(transaction)
        }
    }

    override fun getItemCount() = transactions.size

    fun updateList(newList: MutableList<Transaction>) {
        transactions = newList
        notifyDataSetChanged()
    }
}