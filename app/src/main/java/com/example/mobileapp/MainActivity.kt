package com.example.financeapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var storage: TransactionStorage

    private var balance = 0.0
    private val transactions = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionAdapter

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

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
        adapter = TransactionAdapter(
            mutableListOf(),
            { transaction -> showEditDialog(transaction) },
            { transaction ->
                if (transaction.type == "expense") {
                    balance += transaction.amount
                } else {
                    balance -= transaction.amount
                }
                transactions.removeAll { it.id == transaction.id }
                updateUI()
                saveData()
                Toast.makeText(this, "Удалено!", Toast.LENGTH_SHORT).show()
            }
        )
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
        val rvCategories = view.findViewById<RecyclerView>(R.id.rvCategories)
        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etAmount)
        val etNote = view.findViewById<TextInputEditText>(R.id.etNote)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        tvTitle.text = if (type == "expense") "Добавить расход" else "Добавить доход"

        var selectedCategory = categories[0]
        var selectedPosition = 0

        val categoryAdapter = CategoryAdapter(categories, selectedPosition) { category, position ->
            selectedCategory = category
            selectedPosition = position
        }

        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = categoryAdapter

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val operationName = etName.text.toString().trim()

            if (amount != null && amount > 0) {
                val transaction = Transaction(
                    category = selectedCategory,
                    amount = amount,
                    type = type,
                    date = Date(),
                    note = etNote.text.toString(),
                    name = if (operationName.isNotEmpty()) operationName else selectedCategory
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

    private fun showEditDialog(transaction: Transaction) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_edit_transaction, null)

        val etName = view.findViewById<TextInputEditText>(R.id.etEditName)
        val tvCurrentCategory = view.findViewById<TextView>(R.id.tvCurrentCategory)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etEditAmount)
        val etNote = view.findViewById<TextInputEditText>(R.id.etEditNote)
        val tvCurrentDate = view.findViewById<TextView>(R.id.tvCurrentDate)
        val btnChangeDate = view.findViewById<Button>(R.id.btnChangeDate)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)

        var editedDate = transaction.date

        etName.setText(transaction.name)
        tvCurrentCategory.text = transaction.category
        etAmount.setText(transaction.amount.toString())
        etNote.setText(transaction.note)
        tvCurrentDate.text = dateFormat.format(transaction.date)

        btnChangeDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { time = editedDate }
            DatePickerDialog(this, { _, year, month, day ->
                editedDate = GregorianCalendar(year, month, day,
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)).time
                tvCurrentDate.text = dateFormat.format(editedDate)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnUpdate.setOnClickListener {
            val newAmount = etAmount.text.toString().toDoubleOrNull()
            val newName = etName.text.toString().trim()

            if (newAmount != null && newAmount > 0) {
                if (transaction.type == "expense") {
                    balance += transaction.amount
                } else {
                    balance -= transaction.amount
                }

                if (transaction.type == "expense") {
                    balance -= newAmount
                } else {
                    balance += newAmount
                }

                val updatedTransaction = transaction.copy(
                    amount = newAmount,
                    note = etNote.text.toString(),
                    date = editedDate,
                    name = if (newName.isNotEmpty()) newName else transaction.category
                )
                transactions[transactions.indexOfFirst { it.id == transaction.id }] = updatedTransaction

                updateUI()
                saveData()
                dialog.dismiss()
                Toast.makeText(this, "Обновлено!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show()
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
        tvBalance.text = String.format("%.2f ₽", Math.abs(balance))
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