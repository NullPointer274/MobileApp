package com.example.financeapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var bottomNav: BottomNavigationView

    private var balance = 0.0
    private val transactions = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupBottomNav()
        updateUI()
    }

    private fun initViews() {
        tvBalance = findViewById(R.id.tvBalance)
        rvTransactions = findViewById(R.id.rvTransactions)
        bottomNav = findViewById(R.id.bottomNav)
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_stats -> true
                R.id.nav_categories -> true
                else -> false
            }
        }
    }

    private fun updateUI() {
        tvBalance.text = String.format("%.2f ₽", abs(balance))
        tvBalance.setTextColor(
            if (balance >= 0) resources.getColor(android.R.color.holo_green_dark)
            else resources.getColor(android.R.color.holo_red_dark)
        )
    }
}