package com.example.financeapp

import java.util.Date

data class Transaction(
    val id: String = System.currentTimeMillis().toString(),
    val category: String,
    val amount: Double,
    val type: String,
    val date: Date,
    val note: String = "",
    val name: String = ""
)