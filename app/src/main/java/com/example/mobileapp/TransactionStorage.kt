package com.example.financeapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TransactionStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("finance_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        prefs.edit().putString("transactions", json).apply()
    }

    fun loadTransactions(): MutableList<Transaction> {
        val json = prefs.getString("transactions", "[]")
        val type = object : TypeToken<MutableList<Transaction>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveBalance(balance: Double) {
        prefs.edit().putFloat("balance", balance.toFloat()).apply()
    }

    fun loadBalance(): Double {
        return prefs.getFloat("balance", 0f).toDouble()
    }
}