package com.example.financeapp

import java.util.Date

data class Transaction(
    val category: String,
    val amount: Double,
    val type: String,
    val date: Date
)