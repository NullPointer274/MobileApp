package com.example.financeapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("categories_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val defaultExpenseCategories = mutableListOf(
        "🍔 Еда", "🏠 Жилье", "🚗 Транспорт", "👕 Одежда",
        "📱 Связь", "🎮 Развлечения", "💊 Здоровье", "📚 Образование"
    )

    private val defaultIncomeCategories = mutableListOf(
        "💰 Зарплата", "💼 Подработка", "📈 Инвестиции", "🎁 Подарки",
        "💸 Кэшбэк", "🏦 Проценты", "🔄 Возврат долга"
    )

    fun getExpenseCategories(): MutableList<String> {
        val json = prefs.getString("expense_categories", null)
        if (json == null) return defaultExpenseCategories.toMutableList()

        val type = object : TypeToken<MutableList<String>>() {}.type
        val saved = gson.fromJson<MutableList<String>>(json, type)
        return if (saved.isEmpty()) defaultExpenseCategories.toMutableList() else saved
    }

    fun getIncomeCategories(): MutableList<String> {
        val json = prefs.getString("income_categories", null)
        if (json == null) return defaultIncomeCategories.toMutableList()

        val type = object : TypeToken<MutableList<String>>() {}.type
        val saved = gson.fromJson<MutableList<String>>(json, type)
        return if (saved.isEmpty()) defaultIncomeCategories.toMutableList() else saved
    }

    fun saveExpenseCategories(categories: MutableList<String>) {
        val json = gson.toJson(categories)
        prefs.edit().putString("expense_categories", json).apply()
    }

    fun saveIncomeCategories(categories: MutableList<String>) {
        val json = gson.toJson(categories)
        prefs.edit().putString("income_categories", json).apply()
    }

    fun addExpenseCategory(category: String): Boolean {
        val list = getExpenseCategories()
        if (!list.contains(category)) {
            list.add(category)
            saveExpenseCategories(list)
            return true
        }
        return false
    }

    fun addIncomeCategory(category: String): Boolean {
        val list = getIncomeCategories()
        if (!list.contains(category)) {
            list.add(category)
            saveIncomeCategories(list)
            return true
        }
        return false
    }

    fun removeExpenseCategory(category: String): Boolean {
        val list = getExpenseCategories()
        if (list.remove(category)) {
            saveExpenseCategories(list)
            return true
        }
        return false
    }

    fun removeIncomeCategory(category: String): Boolean {
        val list = getIncomeCategories()
        if (list.remove(category)) {
            saveIncomeCategories(list)
            return true
        }
        return false
    }

    fun updateExpenseCategory(oldName: String, newName: String): Boolean {
        val list = getExpenseCategories()
        val index = list.indexOf(oldName)
        if (index != -1 && !list.contains(newName)) {
            list[index] = newName
            saveExpenseCategories(list)
            return true
        }
        return false
    }

    fun updateIncomeCategory(oldName: String, newName: String): Boolean {
        val list = getIncomeCategories()
        val index = list.indexOf(oldName)
        if (index != -1 && !list.contains(newName)) {
            list[index] = newName
            saveIncomeCategories(list)
            return true
        }
        return false
    }
}