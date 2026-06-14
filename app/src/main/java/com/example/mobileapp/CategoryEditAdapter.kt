package com.example.financeapp

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CategoryEditAdapter(
    private var categories: MutableList<String>,
    private val onDeleteClick: (String, Int) -> Unit,
    private val onEditClick: (String, Int, String) -> Unit
) : RecyclerView.Adapter<CategoryEditAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategoryName)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteCategory)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEditCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.tvCategory.text = category

        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Удаление категории")
                .setMessage("Вы уверены, что хотите удалить категорию \"$category\"?")
                .setPositiveButton("Да") { _, _ ->
                    onDeleteClick(category, position)
                }
                .setNegativeButton("Нет", null)
                .show()
        }

        holder.btnEdit.setOnClickListener {
            val input = EditText(holder.itemView.context)
            input.setText(category)
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Редактировать категорию")
                .setView(input)
                .setPositiveButton("Сохранить") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotEmpty() && newName != category) {
                        if (!categories.contains(newName)) {
                            onEditClick(category, position, newName)
                        } else {
                            Toast.makeText(holder.itemView.context, "Категория '$newName' уже существует", Toast.LENGTH_SHORT).show()
                        }
                    } else if (newName.isEmpty()) {
                        Toast.makeText(holder.itemView.context, "Название не может быть пустым", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    override fun getItemCount() = categories.size

    fun updateList(newList: MutableList<String>) {
        categories = newList
        notifyDataSetChanged()
    }
}