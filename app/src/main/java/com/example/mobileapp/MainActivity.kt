package com.example.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Date
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var storage: TransactionStorage

    private var balance = 0.0
    private val transactions = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionAdapter

    private val expenseCategories = listOf(
        "🍔 Еда", "🏠 Жилье", "🚗 Транспорт", "👕 Одежда",
        "📱 Связь", "🎮 Развлечения", "💊 Здоровье", "📚 Образование"
    )

    private val incomeCategories = listOf(
        "💰 Зарплата", "💼 Подработка", "📈 Инвестиции", "🎁 Подарки",
        "💸 Кэшбэк", "🏦 Проценты", "🔄 Возврат долга"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        storage = TransactionStorage(this)
        setupRecyclerView()
        setupButtons()
        setupBottomNav()
        loadData()
    }

    private fun initViews() {
        tvBalance = findViewById(R.id.tvBalance)
        rvTransactions = findViewById(R.id.rvTransactions)
        bottomNav = findViewById(R.id.bottomNav)
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(transactions) { transaction, position ->
            if (transaction.type == "expense") {
                balance += transaction.amount
            } else {
                balance -= transaction.amount
            }
            transactions.removeAt(position)
            updateUI()
            saveData()
            Toast.makeText(this, "Удалено!", Toast.LENGTH_SHORT).show()
        }
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = adapter
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnExpense).setOnClickListener {
            showAddDialog("expense")
        }

        findViewById<Button>(R.id.btnIncome).setOnClickListener {
            showAddDialog("income")
        }
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_stats -> {
                    showStatsDialog()
                    true
                }
                R.id.nav_categories -> {
                    showCategoriesDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showAddDialog(type: String) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_transaction, null)

        val categories = if (type == "expense") expenseCategories else incomeCategories
        val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val btnCategory1 = view.findViewById<MaterialButton>(R.id.btnCategory1)
        val btnCategory2 = view.findViewById<MaterialButton>(R.id.btnCategory2)
        val btnCategory3 = view.findViewById<MaterialButton>(R.id.btnCategory3)
        val btnCategory4 = view.findViewById<MaterialButton>(R.id.btnCategory4)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etAmount)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        tvTitle.text = if (type == "expense") "Добавить расход" else "Добавить доход"

        val cats = categories.toMutableList()
        btnCategory1.text = cats.getOrNull(0) ?: "Другое"
        btnCategory2.text = cats.getOrNull(1) ?: "Другое"
        btnCategory3.text = cats.getOrNull(2) ?: "Другое"
        btnCategory4.text = cats.getOrNull(3) ?: "Другое"

        var selectedCategory = btnCategory1.text.toString()

        btnCategory1.setOnClickListener { selectedCategory = btnCategory1.text.toString() }
        btnCategory2.setOnClickListener { selectedCategory = btnCategory2.text.toString() }
        btnCategory3.setOnClickListener { selectedCategory = btnCategory3.text.toString() }
        btnCategory4.setOnClickListener { selectedCategory = btnCategory4.text.toString() }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            if (amount != null && amount > 0) {
                val transaction = Transaction(
                    category = selectedCategory,
                    amount = amount,
                    type = type,
                    date = Date()
                )
                transactions.add(0, transaction)
                if (type == "expense") {
                    balance -= amount
                } else {
                    balance += amount
                }
                updateUI()
                saveData()
                dialog.dismiss()
                Toast.makeText(this, "Добавлено!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showStatsDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_stats, null)

        val tvTotalIncome = view.findViewById<TextView>(R.id.tvTotalIncome)
        val tvTotalExpense = view.findViewById<TextView>(R.id.tvTotalExpense)
        val tvBalanceStats = view.findViewById<TextView>(R.id.tvBalanceStats)

        val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val bal = totalIncome - totalExpense

        tvTotalIncome.text = String.format("%.2f ₽", totalIncome)
        tvTotalExpense.text = String.format("%.2f ₽", totalExpense)
        tvBalanceStats.text = String.format("%.2f ₽", bal)

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showCategoriesDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_categories, null)

        val tvExpenseCats = view.findViewById<TextView>(R.id.tvExpenseCategories)
        val tvIncomeCats = view.findViewById<TextView>(R.id.tvIncomeCategories)

        tvExpenseCats.text = expenseCategories.joinToString("\n")
        tvIncomeCats.text = incomeCategories.joinToString("\n")

        dialog.setContentView(view)
        dialog.show()
    }

    private fun updateUI() {
        tvBalance.text = String.format("%.2f ₽", abs(balance))
        tvBalance.setTextColor(
            if (balance >= 0) resources.getColor(android.R.color.holo_green_dark)
            else resources.getColor(android.R.color.holo_red_dark)
        )
        adapter.updateList(transactions)
    }

    private fun saveData() {
        storage.saveTransactions(transactions)
        storage.saveBalance(balance)
    }

    private fun loadData() {
        balance = storage.loadBalance()
        val loadedTransactions = storage.loadTransactions()
        transactions.clear()
        transactions.addAll(loadedTransactions)
        updateUI()
    }
}