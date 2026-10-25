package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.UserPreferencesState
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.AccaFeeEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.DebtEntity
import com.example.data.model.DebtPaymentEntity
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.FeeCategory
import com.example.data.model.ReminderEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.util.DateFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardSummary(
    val totalAvailableMoney: Double = 0.0,
    val cashInHand: Double = 0.0,
    val bankBalance: Double = 0.0,
    val upiAndOtherBalance: Double = 0.0,
    val incomeThisMonth: Double = 0.0,
    val expensesToday: Double = 0.0,
    val expensesThisMonth: Double = 0.0,
    val totalSavings: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val monthlyBudget: Double = 12000.0,
    val budgetUsagePercent: Float = 0.0f,
    val netCashFlow: Double = 0.0,
    val accaTotalFees: Double = 0.0,
    val accaPaidFees: Double = 0.0,
    val accaRemainingFees: Double = 0.0,
    val accaProgressPercent: Float = 0.0f,
    val upcomingDueCount: Int = 0,
    val totalReceivable: Double = 0.0,
    val totalPayable: Double = 0.0,
    val netDebtBalance: Double = 0.0
)

data class DebtSummary(
    val totalReceivable: Double = 0.0, // Money owed to me
    val totalPayable: Double = 0.0,    // Money I owe
    val netDebtPosition: Double = 0.0, // Receivable - Payable
    val activeDebtorsCount: Int = 0,
    val activeCreditorsCount: Int = 0,
    val overdueCount: Int = 0
)

data class InsightItem(
    val title: String,
    val description: String,
    val type: InsightType, // FACT, SUGGESTION, ALERT
    val iconTag: String
)

enum class InsightType { FACT, SUGGESTION, ALERT }

enum class ReportPeriod { THIS_WEEK, THIS_MONTH, THIS_YEAR, ALL_TIME }

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = FinanceRepository(database)
    val preferences = AppPreferences(application)

    val userPreferences: StateFlow<UserPreferencesState> = preferences.state

    val accounts: StateFlow<List<AccountEntity>> = repository.allAccountsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accaFees: StateFlow<List<AccaFeeEntity>> = repository.allFeesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.allGoalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = repository.allRemindersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = repository.allDebtsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Debt summary
    val debtSummary: StateFlow<DebtSummary> = debts.combine(userPreferences) { debtList, _ ->
        val activeDebtors = debtList.filter { it.type == DebtType.DEBTOR && it.status != DebtStatus.SETTLED }
        val activeCreditors = debtList.filter { it.type == DebtType.CREDITOR && it.status != DebtStatus.SETTLED }

        val receivable = activeDebtors.sumOf { it.remainingBalance }
        val payable = activeCreditors.sumOf { it.remainingBalance }
        val net = receivable - payable
        val overdue = debtList.count { it.isOverdue }

        DebtSummary(
            totalReceivable = receivable,
            totalPayable = payable,
            netDebtPosition = net,
            activeDebtorsCount = activeDebtors.size,
            activeCreditorsCount = activeCreditors.size,
            overdueCount = overdue
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DebtSummary())

    // Search and filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter: StateFlow<TransactionType?> = _selectedTypeFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedAccountFilter = MutableStateFlow<Long?>(null)
    val selectedAccountFilter: StateFlow<Long?> = _selectedAccountFilter.asStateFlow()

    private val _reportPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    // Filtered transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        searchQuery,
        selectedTypeFilter,
        selectedCategoryFilter,
        selectedAccountFilter
    ) { txList, query, type, cat, accId ->
        txList.filter { tx ->
            val matchesQuery = query.isBlank() ||
                tx.category.contains(query, ignoreCase = true) ||
                tx.notes.contains(query, ignoreCase = true) ||
                tx.paymentMethod.contains(query, ignoreCase = true)

            val matchesType = type == null || tx.type == type
            val matchesCat = cat == null || tx.category.equals(cat, ignoreCase = true)
            val matchesAcc = accId == null || tx.accountId == accId || tx.toAccountId == accId

            matchesQuery && matchesType && matchesCat && matchesAcc
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard summary calculations
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        accounts,
        transactions,
        accaFees,
        savingsGoals,
        debts
    ) { accList, txList, feeList, goalList, debtList ->
        val totalAvailable = accList.sumOf { it.balance }
        val cash = accList.filter { it.type == AccountType.CASH }.sumOf { it.balance }
        val bank = accList.filter { it.type == AccountType.BANK }.sumOf { it.balance }
        val upiAndOther = accList.filter { it.type != AccountType.CASH && it.type != AccountType.BANK }.sumOf { it.balance }

        val startOfToday = DateFormatter.getStartToday()
        val startOfMonth = DateFormatter.getStartOfMonth()
        val endOfMonth = DateFormatter.getEndOfMonth()

        val expensesToday = txList
            .filter { it.type == TransactionType.EXPENSE && it.timestamp >= startOfToday }
            .sumOf { it.amount }

        val monthTransactions = txList.filter { it.timestamp in startOfMonth..endOfMonth }

        val incomeThisMonth = monthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val expensesThisMonth = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val netCashFlow = incomeThisMonth - expensesThisMonth
        val totalSavings = goalList.sumOf { it.savedAmount }

        val budgetTarget = preferences.state.value.monthlyBudget
        val remainingBudget = budgetTarget - expensesThisMonth
        val budgetUsage = if (budgetTarget > 0) (expensesThisMonth / budgetTarget).toFloat() else 0.0f

        val totalAcca = feeList.sumOf { it.totalAmount }
        val paidAcca = feeList.sumOf { it.paidAmount }
        val remainingAcca = (totalAcca - paidAcca).coerceAtLeast(0.0)
        val accaProgress = if (totalAcca > 0) (paidAcca / totalAcca).toFloat() else 0.0f

        val upcomingCount = feeList.count { it.status != "COMPLETED" && (it.dueDate ?: Long.MAX_VALUE) <= (System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30) }

        val receivable = debtList.filter { it.type == DebtType.DEBTOR && it.status != DebtStatus.SETTLED }.sumOf { it.remainingBalance }
        val payable = debtList.filter { it.type == DebtType.CREDITOR && it.status != DebtStatus.SETTLED }.sumOf { it.remainingBalance }

        DashboardSummary(
            totalAvailableMoney = totalAvailable,
            cashInHand = cash,
            bankBalance = bank,
            upiAndOtherBalance = upiAndOther,
            incomeThisMonth = incomeThisMonth,
            expensesToday = expensesToday,
            expensesThisMonth = expensesThisMonth,
            totalSavings = totalSavings,
            remainingBudget = remainingBudget,
            monthlyBudget = budgetTarget,
            budgetUsagePercent = budgetUsage,
            netCashFlow = netCashFlow,
            accaTotalFees = totalAcca,
            accaPaidFees = paidAcca,
            accaRemainingFees = remainingAcca,
            accaProgressPercent = accaProgress,
            upcomingDueCount = upcomingCount,
            totalReceivable = receivable,
            totalPayable = payable,
            netDebtBalance = receivable - payable
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // Financial Insights (rule-based from actual recorded data)
    val insights: StateFlow<List<InsightItem>> = combine(
        transactions,
        userPreferences,
        accaFees,
        dashboardSummary
    ) { txList, pref, feeList, summary ->
        val list = mutableListOf<InsightItem>()
        val startOfMonth = DateFormatter.getStartOfMonth()
        val monthExpenses = txList.filter { it.type == TransactionType.EXPENSE && it.timestamp >= startOfMonth }

        // 1. Highest Spending Category
        val byCat = monthExpenses.groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .toList()
            .sortedByDescending { it.second }

        if (byCat.isNotEmpty()) {
            val top = byCat.first()
            val totalMonthExp = summary.expensesThisMonth.coerceAtLeast(1.0)
            val pct = (top.second / totalMonthExp * 100).toInt()
            list.add(
                InsightItem(
                    title = "Highest Spending: ${top.first}",
                    description = "You spent ₹${top.second.toInt()} on ${top.first} this month ($pct% of your total expenses).",
                    type = InsightType.FACT,
                    iconTag = "category"
                )
            )
        }

        // 2. Food & Canteen spend
        val foodSpend = monthExpenses.filter { it.category.contains("Food", true) || it.category.contains("Tea", true) }
            .sumOf { it.amount }
        if (foodSpend > 0) {
            list.add(
                InsightItem(
                    title = "Monthly Food & Snacks",
                    description = "Total spent on food, canteen, and snacks: ₹${foodSpend.toInt()}.",
                    type = InsightType.FACT,
                    iconTag = "food"
                )
            )
        }

        // 3. Travel & Commute (Bus fare, Petrol)
        val travelSpend = monthExpenses.filter {
            it.category.contains("Bus", true) || it.category.contains("Petrol", true) || it.category.contains("Transport", true)
        }.sumOf { it.amount }
        if (travelSpend > 0) {
            list.add(
                InsightItem(
                    title = "Commute & Travel Cost",
                    description = "Bus fares and petrol cost ₹${travelSpend.toInt()} this month.",
                    type = InsightType.FACT,
                    iconTag = "commute"
                )
            )
        }

        // 4. Receivables / Debt alert
        if (summary.totalReceivable > 0) {
            list.add(
                InsightItem(
                    title = "Pending Receivables",
                    description = "Friends & debtors owe you ₹${summary.totalReceivable.toInt()}. Follow up to collect your funds for ACCA fees!",
                    type = InsightType.SUGGESTION,
                    iconTag = "debt"
                )
            )
        }

        // 5. Budget Warning or Savings Suggestion
        if (summary.budgetUsagePercent >= 1.0f) {
            list.add(
                InsightItem(
                    title = "Budget Exceeded",
                    description = "You have exceeded your monthly budget of ₹${summary.monthlyBudget.toInt()} by ₹${kotlin.math.abs(summary.remainingBudget).toInt()}.",
                    type = InsightType.ALERT,
                    iconTag = "warning"
                )
            )
        } else if (summary.budgetUsagePercent >= 0.8f) {
            list.add(
                InsightItem(
                    title = "Budget Alert: 80% Used",
                    description = "You have spent 80%+ of your budget. Remaining buffer is ₹${summary.remainingBudget.toInt()}.",
                    type = InsightType.ALERT,
                    iconTag = "warning"
                )
            )
        } else if (summary.monthlyBudget > 0) {
            val potentialSavings = (summary.remainingBudget).coerceAtLeast(0.0)
            list.add(
                InsightItem(
                    title = "Projected Savings Potential",
                    description = "If you maintain your current discipline and stay within budget, you will save ₹${potentialSavings.toInt()} this month.",
                    type = InsightType.SUGGESTION,
                    iconTag = "savings"
                )
            )
        }

        // 6. ACCA Upcoming Payment Alert
        val nextDue = feeList.filter { it.status != "COMPLETED" && it.dueDate != null }
            .minByOrNull { it.dueDate ?: Long.MAX_VALUE }
        if (nextDue != null && nextDue.dueDate != null) {
            val remainingAmt = nextDue.totalAmount - nextDue.paidAmount
            list.add(
                InsightItem(
                    title = "Upcoming Due: ${nextDue.title}",
                    description = "Due on ${DateFormatter.formatDate(nextDue.dueDate)}: ₹${remainingAmt.toInt()} remaining.",
                    type = InsightType.ALERT,
                    iconTag = "calendar"
                )
            )
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
        }
    }

    // Filter controls
    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setTypeFilter(type: TransactionType?) { _selectedTypeFilter.value = type }
    fun setCategoryFilter(cat: String?) { _selectedCategoryFilter.value = cat }
    fun setAccountFilter(accId: Long?) { _selectedAccountFilter.value = accId }
    fun setReportPeriod(period: ReportPeriod) { _reportPeriod.value = period }

    // Actions
    fun addTransaction(tx: TransactionEntity) = viewModelScope.launch {
        repository.addTransaction(tx)
    }

    fun updateTransaction(oldTx: TransactionEntity, newTx: TransactionEntity) = viewModelScope.launch {
        repository.updateTransaction(oldTx, newTx)
    }

    fun deleteTransaction(tx: TransactionEntity) = viewModelScope.launch {
        repository.deleteTransaction(tx)
    }

    fun addDebt(debt: DebtEntity, affectAccountBalance: Boolean = false) = viewModelScope.launch {
        repository.addDebt(debt, affectAccountBalance)
    }

    fun updateDebt(debt: DebtEntity) = viewModelScope.launch {
        repository.updateDebt(debt)
    }

    fun deleteDebt(debt: DebtEntity) = viewModelScope.launch {
        repository.deleteDebt(debt)
    }

    fun recordDebtPayment(payment: DebtPaymentEntity, debt: DebtEntity) = viewModelScope.launch {
        repository.recordDebtPayment(payment, debt)
    }

    fun deleteDebtPayment(payment: DebtPaymentEntity, debt: DebtEntity) = viewModelScope.launch {
        repository.deleteDebtPayment(payment, debt)
    }

    fun getPaymentsForDebtFlow(debtId: Long): Flow<List<DebtPaymentEntity>> =
        repository.getPaymentsForDebtFlow(debtId)

    fun addAccount(account: AccountEntity) = viewModelScope.launch {
        repository.addAccount(account)
    }

    fun updateAccount(account: AccountEntity) = viewModelScope.launch {
        repository.updateAccount(account)
    }

    fun deleteAccount(account: AccountEntity) = viewModelScope.launch {
        repository.deleteAccount(account)
    }

    fun addAccaFee(fee: AccaFeeEntity) = viewModelScope.launch {
        repository.addAccaFee(fee)
    }

    fun updateAccaFee(fee: AccaFeeEntity) = viewModelScope.launch {
        repository.updateAccaFee(fee)
    }

    fun deleteAccaFee(fee: AccaFeeEntity) = viewModelScope.launch {
        repository.deleteAccaFee(fee)
    }

    fun addSavingsGoal(goal: SavingsGoalEntity) = viewModelScope.launch {
        repository.addGoal(goal)
    }

    fun updateSavingsGoal(goal: SavingsGoalEntity) = viewModelScope.launch {
        repository.updateGoal(goal)
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) = viewModelScope.launch {
        repository.deleteGoal(goal)
    }

    fun addReminder(reminder: ReminderEntity) = viewModelScope.launch {
        repository.addReminder(reminder)
    }

    fun updateReminder(reminder: ReminderEntity) = viewModelScope.launch {
        repository.updateReminder(reminder)
    }

    fun deleteReminder(reminder: ReminderEntity) = viewModelScope.launch {
        repository.deleteReminder(reminder)
    }

    fun loadDemoData() = viewModelScope.launch {
        repository.loadDemoData()
        preferences.setDemoLoaded(true)
    }

    fun clearAllData() = viewModelScope.launch {
        repository.clearAllData()
        preferences.setDemoLoaded(false)
    }

    fun carryForwardBudget() = viewModelScope.launch {
        val summary = dashboardSummary.value
        val unspent = summary.remainingBudget.coerceAtLeast(0.0)
        val newTarget = summary.monthlyBudget + unspent
        preferences.updateMonthlyBudget(newTarget)
    }
}
