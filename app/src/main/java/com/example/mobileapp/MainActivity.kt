package com.example.financeapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var storage: TransactionStorage
    private lateinit var categoryManager: CategoryManager
    private lateinit var tvFilterInfo: TextView
    private lateinit var tvTransactionCount: TextView

    private var balance = 0.0
    private val transactions = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionAdapter

    private var currentFilterType: String? = null
    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        storage = TransactionStorage(this)
        categoryManager = CategoryManager(this)
        setupRecyclerView()
        setupButtons()
        setupBottomNav()
        setupFilters()
        loadData()
    }

    private fun initViews() {
        tvBalance = findViewById(R.id.tvBalance)
        rvTransactions = findViewById(R.id.rvTransactions)
        bottomNav = findViewById(R.id.bottomNav)
        tvFilterInfo = findViewById(R.id.tvFilterInfo)
        tvTransactionCount = findViewById(R.id.tvTransactionCount)
    }

    private fun setupFilters() {
        findViewById<Button>(R.id.btnFilterAll).setOnClickListener {
            currentFilterType = null
            updateFilterInfo()
            applyFilters()
        }
        findViewById<Button>(R.id.btnFilterExpense).setOnClickListener {
            currentFilterType = "expense"
            updateFilterInfo()
            applyFilters()
        }
        findViewById<Button>(R.id.btnFilterIncome).setOnClickListener {
            currentFilterType = "income"
            updateFilterInfo()
            applyFilters()
        }
        findViewById<Button>(R.id.btnFilterMonth).setOnClickListener {
            showMonthPicker()
        }
        updateFilterInfo()
    }

    private fun showMonthPicker() {
        DatePickerDialog(this, { _, year, month, _ ->
            currentYear = year
            currentMonth = month
            updateFilterInfo()
            applyFilters()
        }, currentYear, currentMonth, 1).show()
    }

    private fun updateFilterInfo() {
        val monthNames = arrayOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")

        val typeText = when(currentFilterType) {
            null -> "Все"
            "expense" -> "Расходы"
            "income" -> "Доходы"
            else -> "Все"
        }
        tvFilterInfo.text = "${monthNames[currentMonth]} $currentYear | $typeText"
    }

    private fun applyFilters() {
        var filtered = transactions

        if (currentFilterType != null) {
            filtered = filtered.filter { it.type == currentFilterType }.toMutableList()
        }

        filtered = filtered.filter { transaction ->
            val cal = Calendar.getInstance().apply { time = transaction.date }
            cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth
        }.toMutableList()

        adapter.updateList(filtered)
        tvTransactionCount.text = "Операций: ${filtered.size}"
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
                    showAdvancedStatsDialog()
                    true
                }
                R.id.nav_categories -> {
                    showManageCategoriesDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showAddDialog(type: String) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_transaction, null)

        val categories = if (type == "expense")
            categoryManager.getExpenseCategories()
        else
            categoryManager.getIncomeCategories()

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

        rvCategories.layoutManager = GridLayoutManager(this, 2)
        rvCategories.adapter = categoryAdapter

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val operationName = etName.text.toString().trim()

            if (amount != null && amount > 0) {
                val calendar = Calendar.getInstance()
                val now = Calendar.getInstance()
                calendar.set(currentYear, currentMonth, now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND))
                val transactionDate = calendar.time

                val transaction = Transaction(
                    category = selectedCategory,
                    amount = amount,
                    type = type,
                    date = transactionDate,
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
        val btnChangeCategory = view.findViewById<Button>(R.id.btnChangeCategory)

        var editedDate = transaction.date
        var editedCategory = transaction.category

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

        btnChangeCategory.setOnClickListener {
            val categoryDialog = BottomSheetDialog(this)
            val categoryView = layoutInflater.inflate(R.layout.dialog_edit_category, null)

            val rvCategories = categoryView.findViewById<RecyclerView>(R.id.rvEditCategories)
            val btnClose = categoryView.findViewById<Button>(R.id.btnCloseCategorySelect)

            val categories = if (transaction.type == "expense")
                categoryManager.getExpenseCategories()
            else
                categoryManager.getIncomeCategories()

            var selectedPos = categories.indexOf(editedCategory)
            if (selectedPos == -1) selectedPos = 0

            val categoryAdapter = CategoryAdapter(categories, selectedPos) { category, _ ->
                editedCategory = category
                tvCurrentCategory.text = category
                categoryDialog.dismiss()
                Toast.makeText(this, "Категория изменена", Toast.LENGTH_SHORT).show()
            }

            rvCategories.layoutManager = GridLayoutManager(this, 2)
            rvCategories.adapter = categoryAdapter

            btnClose.setOnClickListener {
                categoryDialog.dismiss()
            }

            categoryDialog.setContentView(categoryView)
            categoryDialog.show()
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
                    category = editedCategory,
                    amount = newAmount,
                    note = etNote.text.toString(),
                    date = editedDate,
                    name = if (newName.isNotEmpty()) newName else editedCategory
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

    private fun showAdvancedStatsDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_stats_advanced, null)

        val btnSelectMonth = view.findViewById<Button>(R.id.btnSelectMonthStats)
        val tvSelectedMonth = view.findViewById<TextView>(R.id.tvSelectedMonthStats)
        val pieChart = view.findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.pieChart)
        val rvCategoryStats = view.findViewById<RecyclerView>(R.id.rvCategoryStats)
        val tvNoExpenses = view.findViewById<TextView>(R.id.tvNoExpenses)
        val tvTopCategory = view.findViewById<TextView>(R.id.tvTopCategory)

        var selectedYear = currentYear
        var selectedMonth = currentMonth

        fun updateStats(year: Int, month: Int) {
            val calendar = Calendar.getInstance()

            calendar.set(year, month, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthStart = calendar.time

            calendar.set(year, month + 1, 1, 0, 0, 0)
            calendar.add(Calendar.MILLISECOND, -1)
            val monthEnd = calendar.time

            val monthTransactions = transactions.filter { transaction ->
                transaction.date in monthStart..monthEnd
            }

            val expenses = monthTransactions.filter { it.type == "expense" }
            val incomes = monthTransactions.filter { it.type == "income" }

            val totalExpense = expenses.sumOf { it.amount }
            val totalIncome = incomes.sumOf { it.amount }
            val bal = totalIncome - totalExpense

            val expenseByCategory = expenses.groupBy { it.category }
                .mapValues { it.value.sumOf { trans -> trans.amount } }
                .toList()
                .sortedByDescending { it.second }

            view.findViewById<TextView>(R.id.tvTotalIncomeStats).text = String.format("%.2f ₽", totalIncome)
            view.findViewById<TextView>(R.id.tvTotalExpenseStats).text = String.format("%.2f ₽", totalExpense)
            view.findViewById<TextView>(R.id.tvBalanceStats).text = String.format("%.2f ₽", bal)

            if (expenseByCategory.isNotEmpty()) {
                tvNoExpenses.visibility = android.view.View.GONE
                rvCategoryStats.visibility = android.view.View.VISIBLE
                pieChart.visibility = android.view.View.VISIBLE

                val entries = expenseByCategory.map { (category, amount) ->
                    PieEntry(amount.toFloat(), category)
                }
                val dataSet = PieDataSet(entries, "Расходы по категориям")
                dataSet.colors = listOf(
                    android.graphics.Color.rgb(255, 99, 132),
                    android.graphics.Color.rgb(54, 162, 235),
                    android.graphics.Color.rgb(255, 206, 86),
                    android.graphics.Color.rgb(75, 192, 192),
                    android.graphics.Color.rgb(153, 102, 255)
                )
                dataSet.valueTextSize = 12f
                dataSet.valueTextColor = android.graphics.Color.BLACK

                pieChart.data = PieData(dataSet)
                pieChart.description.isEnabled = false
                pieChart.setUsePercentValues(true)
                pieChart.animateY(1000)
                pieChart.invalidate()

                val topCategory = expenseByCategory.first()
                tvTopCategory.text = "🏆 ${topCategory.first}: ${String.format("%.2f", topCategory.second)} ₽"

                val adapter = CategoryStatsAdapter(expenseByCategory, totalExpense)
                rvCategoryStats.layoutManager = LinearLayoutManager(this)
                rvCategoryStats.adapter = adapter
            } else {
                tvNoExpenses.visibility = android.view.View.VISIBLE
                rvCategoryStats.visibility = android.view.View.GONE
                pieChart.visibility = android.view.View.GONE
                tvTopCategory.text = "Нет расходов за выбранный период"
            }

            val monthNames = arrayOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
            tvSelectedMonth.text = "${monthNames[month]} $year"
        }

        btnSelectMonth.setOnClickListener {
            DatePickerDialog(this, { _, year, month, _ ->
                selectedYear = year
                selectedMonth = month
                updateStats(year, month)
            }, selectedYear, selectedMonth, 1).show()
        }

        updateStats(selectedYear, selectedMonth)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun showManageCategoriesDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_manage_categories, null)

        val etNewCategory = view.findViewById<TextInputEditText>(R.id.etNewCategory)
        val btnAddExpense = view.findViewById<Button>(R.id.btnAddExpenseCategory)
        val btnAddIncome = view.findViewById<Button>(R.id.btnAddIncomeCategory)
        val rvExpense = view.findViewById<RecyclerView>(R.id.rvExpenseCategories)
        val rvIncome = view.findViewById<RecyclerView>(R.id.rvIncomeCategories)

        val expenseCategories = categoryManager.getExpenseCategories().toMutableList()
        val incomeCategories = categoryManager.getIncomeCategories().toMutableList()

        lateinit var expenseAdapter: CategoryEditAdapter
        lateinit var incomeAdapter: CategoryEditAdapter

        expenseAdapter = CategoryEditAdapter(
            expenseCategories,
            { category, position ->
                val hasTransactions = transactions.any { it.category == category && it.type == "expense" }
                if (!hasTransactions) {
                    categoryManager.removeExpenseCategory(category)
                    expenseCategories.removeAt(position)
                    expenseAdapter.updateList(expenseCategories)
                    Toast.makeText(this, "Категория удалена", Toast.LENGTH_SHORT).show()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Категория используется")
                        .setMessage("Удалить все транзакции с этой категорией?")
                        .setPositiveButton("Да") { _, _ ->
                            val iterator = transactions.iterator()
                            while (iterator.hasNext()) {
                                val transaction = iterator.next()
                                if (transaction.category == category && transaction.type == "expense") {
                                    balance += transaction.amount
                                    iterator.remove()
                                }
                            }
                            categoryManager.removeExpenseCategory(category)
                            expenseCategories.removeAt(position)
                            expenseAdapter.updateList(expenseCategories)
                            saveData()
                            updateUI()
                            Toast.makeText(this, "Категория и транзакции удалены", Toast.LENGTH_LONG).show()
                        }
                        .setNegativeButton("Нет", null)
                        .show()
                }
            },
            { oldName, _, newName ->
                var updatedCount = 0
                for (i in transactions.indices) {
                    if (transactions[i].category == oldName && transactions[i].type == "expense") {
                        transactions[i] = transactions[i].copy(category = newName)
                        updatedCount++
                    }
                }
                categoryManager.removeExpenseCategory(oldName)
                categoryManager.addExpenseCategory(newName)
                expenseCategories[expenseCategories.indexOf(oldName)] = newName
                expenseAdapter.updateList(expenseCategories)
                saveData()
                updateUI()
                Toast.makeText(this, "Категория переименована. Обновлено $updatedCount транзакций", Toast.LENGTH_LONG).show()
            }
        )

        incomeAdapter = CategoryEditAdapter(
            incomeCategories,
            { category, position ->
                val hasTransactions = transactions.any { it.category == category && it.type == "income" }
                if (!hasTransactions) {
                    categoryManager.removeIncomeCategory(category)
                    incomeCategories.removeAt(position)
                    incomeAdapter.updateList(incomeCategories)
                    Toast.makeText(this, "Категория удалена", Toast.LENGTH_SHORT).show()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Категория используется")
                        .setMessage("Удалить все транзакции с этой категорией?")
                        .setPositiveButton("Да") { _, _ ->
                            val iterator = transactions.iterator()
                            while (iterator.hasNext()) {
                                val transaction = iterator.next()
                                if (transaction.category == category && transaction.type == "income") {
                                    balance -= transaction.amount
                                    iterator.remove()
                                }
                            }
                            categoryManager.removeIncomeCategory(category)
                            incomeCategories.removeAt(position)
                            incomeAdapter.updateList(incomeCategories)
                            saveData()
                            updateUI()
                            Toast.makeText(this, "Категория и транзакции удалены", Toast.LENGTH_LONG).show()
                        }
                        .setNegativeButton("Нет", null)
                        .show()
                }
            },
            { oldName, _, newName ->
                var updatedCount = 0
                for (i in transactions.indices) {
                    if (transactions[i].category == oldName && transactions[i].type == "income") {
                        transactions[i] = transactions[i].copy(category = newName)
                        updatedCount++
                    }
                }
                categoryManager.removeIncomeCategory(oldName)
                categoryManager.addIncomeCategory(newName)
                incomeCategories[incomeCategories.indexOf(oldName)] = newName
                incomeAdapter.updateList(incomeCategories)
                saveData()
                updateUI()
                Toast.makeText(this, "Категория переименована. Обновлено $updatedCount транзакций", Toast.LENGTH_LONG).show()
            }
        )

        rvExpense.layoutManager = LinearLayoutManager(this)
        rvExpense.adapter = expenseAdapter
        rvIncome.layoutManager = LinearLayoutManager(this)
        rvIncome.adapter = incomeAdapter

        btnAddExpense.setOnClickListener {
            val newCategory = etNewCategory.text.toString().trim()
            if (newCategory.isNotEmpty()) {
                if (!expenseCategories.contains(newCategory)) {
                    categoryManager.addExpenseCategory(newCategory)
                    expenseCategories.add(newCategory)
                    expenseAdapter.updateList(expenseCategories)
                    etNewCategory.text?.clear()
                    Toast.makeText(this, "✅ Категория добавлена в расходы", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "⚠️ Категория уже существует", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show()
            }
        }

        btnAddIncome.setOnClickListener {
            val newCategory = etNewCategory.text.toString().trim()
            if (newCategory.isNotEmpty()) {
                if (!incomeCategories.contains(newCategory)) {
                    categoryManager.addIncomeCategory(newCategory)
                    incomeCategories.add(newCategory)
                    incomeAdapter.updateList(incomeCategories)
                    etNewCategory.text?.clear()
                    Toast.makeText(this, "✅ Категория добавлена в доходы", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "⚠️ Категория уже существует", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun updateUI() {
        tvBalance.text = String.format("%.2f ₽", Math.abs(balance))
        tvBalance.setTextColor(
            if (balance >= 0) resources.getColor(android.R.color.holo_green_dark)
            else resources.getColor(android.R.color.holo_red_dark)
        )
        applyFilters()
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