package com.example.data.repository

import com.example.data.local.AppDatabase
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
import com.example.util.CurrencyFormatter
import com.example.util.DateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FinanceRepository(private val database: AppDatabase) {
    private val accountDao = database.accountDao()
    private val transactionDao = database.transactionDao()
    private val accaFeeDao = database.accaFeeDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val budgetDao = database.budgetDao()
    private val reminderDao = database.reminderDao()
    private val debtDao = database.debtDao()
    private val debtPaymentDao = database.debtPaymentDao()

    val allAccountsFlow: Flow<List<AccountEntity>> = accountDao.getAllAccountsFlow()
    val allTransactionsFlow: Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()
    val allFeesFlow: Flow<List<AccaFeeEntity>> = accaFeeDao.getAllFeesFlow()
    val allGoalsFlow: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoalsFlow()
    val allRemindersFlow: Flow<List<ReminderEntity>> = reminderDao.getAllRemindersFlow()
    val allDebtsFlow: Flow<List<DebtEntity>> = debtDao.getAllDebtsFlow()

    fun getPaymentsForDebtFlow(debtId: Long): Flow<List<DebtPaymentEntity>> =
        debtPaymentDao.getPaymentsForDebtFlow(debtId)

    fun getBudgetsForMonthFlow(monthKey: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForMonthFlow(monthKey)

    suspend fun initializeDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        val accounts = accountDao.getAllAccounts()
        if (accounts.isEmpty()) {
            accountDao.insertAccount(
                AccountEntity(
                    name = "Cash in Hand",
                    type = AccountType.CASH,
                    balance = 1850.0,
                    accountDetail = "Wallet Cash",
                    colorHex = "#10B981",
                    isDefault = true
                )
            )
            accountDao.insertAccount(
                AccountEntity(
                    name = "SBI Savings A/c",
                    type = AccountType.BANK,
                    balance = 14500.0,
                    accountDetail = "A/c **4219",
                    colorHex = "#0284C7"
                )
            )
            accountDao.insertAccount(
                AccountEntity(
                    name = "Google Pay (UPI)",
                    type = AccountType.UPI,
                    balance = 3450.0,
                    accountDetail = "student@oksbi",
                    colorHex = "#14B8A6"
                )
            )
            accountDao.insertAccount(
                AccountEntity(
                    name = "Federal Bank",
                    type = AccountType.BANK,
                    balance = 8200.0,
                    accountDetail = "A/c **9031",
                    colorHex = "#6366F1"
                )
            )
        }

        val fees = accaFeeDao.getAllFees()
        if (fees.isEmpty()) {
            accaFeeDao.insertFee(
                AccaFeeEntity(
                    title = "ACCA Applied Skills Coaching Tuition",
                    totalAmount = 65000.0,
                    paidAmount = 35000.0,
                    category = FeeCategory.TUITION,
                    dueDate = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 35,
                    status = "PARTIAL",
                    referenceNote = "Installment 1 & 2 paid at Kochi Center"
                )
            )
            accaFeeDao.insertFee(
                AccaFeeEntity(
                    title = "ACCA Annual Student Subscription 2026",
                    totalAmount = 14200.0,
                    paidAmount = 14200.0,
                    category = FeeCategory.ANNUAL_SUBSCRIPTION,
                    dueDate = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 10,
                    status = "COMPLETED",
                    referenceNote = "Paid via International Forex Card - Ref #ACC88319"
                )
            )
            accaFeeDao.insertFee(
                AccaFeeEntity(
                    title = "Financial Reporting (FR) Exam Entry Fee",
                    totalAmount = 15800.0,
                    paidAmount = 0.0,
                    category = FeeCategory.EXAM_FEE,
                    dueDate = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 20,
                    status = "PENDING",
                    referenceNote = "Standard entry deadline for next exam session"
                )
            )
        }

        val goals = savingsGoalDao.getAllGoals()
        if (goals.isEmpty()) {
            savingsGoalDao.insertGoal(
                SavingsGoalEntity(
                    title = "Emergency Fund",
                    targetAmount = 25000.0,
                    savedAmount = 12000.0,
                    targetDate = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 90,
                    colorHex = "#10B981",
                    notes = "Hostel & medical backup buffer"
                )
            )
            savingsGoalDao.insertGoal(
                SavingsGoalEntity(
                    title = "ACCA Dec Exam Session Fund",
                    targetAmount = 32000.0,
                    savedAmount = 18500.0,
                    targetDate = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 45,
                    colorHex = "#0284C7",
                    notes = "Exam entries + Kaplan Mock Exam"
                )
            )
            savingsGoalDao.insertGoal(
                SavingsGoalEntity(
                    title = "Study Laptop Upgrade",
                    targetAmount = 55000.0,
                    savedAmount = 22000.0,
                    targetDate = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 180,
                    colorHex = "#F59E0B",
                    notes = "For Computer-Based Exams (CBE) Practice & Excel Spreadsheets"
                )
            )
        }

        val reminders = reminderDao.getAllReminders()
        if (reminders.isEmpty()) {
            reminderDao.insertReminder(
                ReminderEntity(
                    title = "ACCA Exam Entry Deadline",
                    note = "Book CBE exam center in Ernakulam/Trivandrum",
                    dueDate = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 15,
                    category = "EXAM",
                    isEnabled = true
                )
            )
            reminderDao.insertReminder(
                ReminderEntity(
                    title = "Hostel Rent & Mess Bill",
                    note = "Transfer to landlord via UPI",
                    dueDate = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 5,
                    category = "RECURRING",
                    isEnabled = true
                )
            )
        }

        val debts = debtDao.getAllDebts()
        if (debts.isEmpty()) {
            val debtorId = debtDao.insertDebt(
                DebtEntity(
                    personName = "Rahul (ACCA Study Partner)",
                    phoneNumber = "+91 98470 12345",
                    type = DebtType.DEBTOR,
                    initialAmount = 1500.0,
                    paidOrReceivedAmount = 500.0,
                    date = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 14,
                    dueDate = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7,
                    notes = "Split cost for Kaplan FR & TX BPP Exam Kits",
                    status = DebtStatus.PARTIALLY_PAID
                )
            )
            debtPaymentDao.insertPayment(
                DebtPaymentEntity(
                    debtId = debtorId,
                    amount = 500.0,
                    timestamp = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 3,
                    accountId = accounts.firstOrNull { it.type == AccountType.UPI }?.id ?: 1L,
                    paymentMethod = "Google Pay",
                    notes = "1st partial repayment via GPay"
                )
            )

            debtDao.insertDebt(
                DebtEntity(
                    personName = "Anil Brother (Hostel)",
                    phoneNumber = "+91 97455 67890",
                    type = DebtType.CREDITOR,
                    initialAmount = 800.0,
                    paidOrReceivedAmount = 0.0,
                    date = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 8,
                    dueDate = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 10,
                    notes = "Borrowed for bus fare & printing when UPI was down",
                    status = DebtStatus.ACTIVE
                )
            )
        }
    }

    suspend fun addTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        val insertedId = transactionDao.insertTransaction(transaction)

        when (transaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.adjustBalance(transaction.accountId, -transaction.amount)
                if (transaction.isAccaRelated && transaction.accaFeeId != null) {
                    val fee = accaFeeDao.getFeeById(transaction.accaFeeId)
                    if (fee != null) {
                        val newPaid = fee.paidAmount + transaction.amount
                        val newStatus = if (newPaid >= fee.totalAmount) "COMPLETED" else "PARTIAL"
                        accaFeeDao.updateFee(fee.copy(paidAmount = newPaid, status = newStatus))
                    }
                }
            }
            TransactionType.INCOME -> {
                accountDao.adjustBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.TRANSFER -> {
                // Internal transfer: deduct from sender, add to receiver. NO income or expense!
                accountDao.adjustBalance(transaction.accountId, -transaction.amount)
                transaction.toAccountId?.let { toId ->
                    accountDao.adjustBalance(toId, transaction.amount)
                }
            }
            TransactionType.GOAL_SAVING -> {
                // Deduct from account, allocate to savings goal. NO expense or income!
                accountDao.adjustBalance(transaction.accountId, -transaction.amount)
                transaction.goalId?.let { goalId ->
                    savingsGoalDao.adjustGoalAmount(goalId, transaction.amount)
                }
            }
            TransactionType.DEBT_COLLECTED -> {
                // Debtor repaid me: increase account balance. Classified as debt recovery, not income!
                accountDao.adjustBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.DEBT_REPAID -> {
                // I repaid creditor: decrease account balance. Classified as liability settlement, not expense!
                accountDao.adjustBalance(transaction.accountId, -transaction.amount)
            }
        }
        insertedId
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.adjustBalance(transaction.accountId, transaction.amount)
                if (transaction.isAccaRelated && transaction.accaFeeId != null) {
                    val fee = accaFeeDao.getFeeById(transaction.accaFeeId)
                    if (fee != null) {
                        val newPaid = (fee.paidAmount - transaction.amount).coerceAtLeast(0.0)
                        val newStatus = when {
                            newPaid <= 0.0 -> "PENDING"
                            newPaid >= fee.totalAmount -> "COMPLETED"
                            else -> "PARTIAL"
                        }
                        accaFeeDao.updateFee(fee.copy(paidAmount = newPaid, status = newStatus))
                    }
                }
            }
            TransactionType.INCOME -> {
                accountDao.adjustBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.TRANSFER -> {
                accountDao.adjustBalance(transaction.accountId, transaction.amount)
                transaction.toAccountId?.let { toId ->
                    accountDao.adjustBalance(toId, -transaction.amount)
                }
            }
            TransactionType.GOAL_SAVING -> {
                accountDao.adjustBalance(transaction.accountId, transaction.amount)
                transaction.goalId?.let { goalId ->
                    savingsGoalDao.adjustGoalAmount(goalId, -transaction.amount)
                }
            }
            TransactionType.DEBT_COLLECTED -> {
                accountDao.adjustBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.DEBT_REPAID -> {
                accountDao.adjustBalance(transaction.accountId, transaction.amount)
            }
        }
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun updateTransaction(oldTx: TransactionEntity, newTx: TransactionEntity) = withContext(Dispatchers.IO) {
        deleteTransaction(oldTx)
        addTransaction(newTx)
    }

    // DEBT MANAGEMENT METHODS
    suspend fun addDebt(debt: DebtEntity, affectAccountBalance: Boolean = false): Long = withContext(Dispatchers.IO) {
        val debtId = debtDao.insertDebt(debt)
        if (affectAccountBalance && debt.initialAccountId != null) {
            when (debt.type) {
                DebtType.DEBTOR -> {
                    // I lent money to someone: money left my account
                    accountDao.adjustBalance(debt.initialAccountId, -debt.initialAmount)
                    transactionDao.insertTransaction(
                        TransactionEntity(
                            amount = debt.initialAmount,
                            type = TransactionType.DEBT_REPAID, // classified separately from ordinary expense
                            category = "Loan Given: ${debt.personName}",
                            timestamp = debt.date,
                            accountId = debt.initialAccountId,
                            debtId = debtId,
                            notes = "Lent to ${debt.personName}. ${debt.notes}".trim()
                        )
                    )
                }
                DebtType.CREDITOR -> {
                    // I borrowed money: money entered my account
                    accountDao.adjustBalance(debt.initialAccountId, debt.initialAmount)
                    transactionDao.insertTransaction(
                        TransactionEntity(
                            amount = debt.initialAmount,
                            type = TransactionType.DEBT_COLLECTED, // classified separately from ordinary income
                            category = "Loan Taken: ${debt.personName}",
                            timestamp = debt.date,
                            accountId = debt.initialAccountId,
                            debtId = debtId,
                            notes = "Borrowed from ${debt.personName}. ${debt.notes}".trim()
                        )
                    )
                }
            }
        }
        debtId
    }

    suspend fun updateDebt(debt: DebtEntity) = withContext(Dispatchers.IO) {
        debtDao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: DebtEntity) = withContext(Dispatchers.IO) {
        debtPaymentDao.deletePaymentsForDebt(debt.id)
        debtDao.deleteDebt(debt)
    }

    suspend fun recordDebtPayment(payment: DebtPaymentEntity, debt: DebtEntity): Long = withContext(Dispatchers.IO) {
        val paymentId = debtPaymentDao.insertPayment(payment)

        val newPaidOrReceived = debt.paidOrReceivedAmount + payment.amount
        val newStatus = if (newPaidOrReceived >= debt.initialAmount) DebtStatus.SETTLED else DebtStatus.PARTIALLY_PAID

        debtDao.updateDebt(
            debt.copy(
                paidOrReceivedAmount = newPaidOrReceived,
                status = newStatus
            )
        )

        // Financial balance integration:
        // Debtor repaid me -> money increases in account (DEBT_COLLECTED, not ordinary income)
        // I repaid creditor -> money decreases in account (DEBT_REPAID, not ordinary expense)
        when (debt.type) {
            DebtType.DEBTOR -> {
                accountDao.adjustBalance(payment.accountId, payment.amount)
                transactionDao.insertTransaction(
                    TransactionEntity(
                        amount = payment.amount,
                        type = TransactionType.DEBT_COLLECTED,
                        category = "Debt Repayment from ${debt.personName}",
                        timestamp = payment.timestamp,
                        accountId = payment.accountId,
                        debtId = debt.id,
                        paymentMethod = payment.paymentMethod,
                        notes = payment.notes.ifBlank { "Repayment received from ${debt.personName}" }
                    )
                )
            }
            DebtType.CREDITOR -> {
                accountDao.adjustBalance(payment.accountId, -payment.amount)
                transactionDao.insertTransaction(
                    TransactionEntity(
                        amount = payment.amount,
                        type = TransactionType.DEBT_REPAID,
                        category = "Debt Repayment to ${debt.personName}",
                        timestamp = payment.timestamp,
                        accountId = payment.accountId,
                        debtId = debt.id,
                        paymentMethod = payment.paymentMethod,
                        notes = payment.notes.ifBlank { "Repayment made to ${debt.personName}" }
                    )
                )
            }
        }

        paymentId
    }

    suspend fun deleteDebtPayment(payment: DebtPaymentEntity, debt: DebtEntity) = withContext(Dispatchers.IO) {
        // Reverse account balance effect
        when (debt.type) {
            DebtType.DEBTOR -> {
                accountDao.adjustBalance(payment.accountId, -payment.amount)
            }
            DebtType.CREDITOR -> {
                accountDao.adjustBalance(payment.accountId, payment.amount)
            }
        }
        val newPaid = (debt.paidOrReceivedAmount - payment.amount).coerceAtLeast(0.0)
        val newStatus = when {
            newPaid <= 0.0 -> DebtStatus.ACTIVE
            newPaid >= debt.initialAmount -> DebtStatus.SETTLED
            else -> DebtStatus.PARTIALLY_PAID
        }
        debtDao.updateDebt(debt.copy(paidOrReceivedAmount = newPaid, status = newStatus))
        debtPaymentDao.deletePayment(payment)
    }

    suspend fun addAccount(account: AccountEntity): Long = withContext(Dispatchers.IO) {
        accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        accountDao.deleteAccount(account)
    }

    suspend fun addAccaFee(fee: AccaFeeEntity): Long = withContext(Dispatchers.IO) {
        accaFeeDao.insertFee(fee)
    }

    suspend fun updateAccaFee(fee: AccaFeeEntity) = withContext(Dispatchers.IO) {
        accaFeeDao.updateFee(fee)
    }

    suspend fun deleteAccaFee(fee: AccaFeeEntity) = withContext(Dispatchers.IO) {
        accaFeeDao.deleteFee(fee)
    }

    suspend fun addGoal(goal: SavingsGoalEntity): Long = withContext(Dispatchers.IO) {
        savingsGoalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        savingsGoalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        savingsGoalDao.deleteGoal(goal)
    }

    suspend fun setBudget(budget: BudgetEntity): Long = withContext(Dispatchers.IO) {
        budgetDao.insertBudget(budget)
    }

    suspend fun addReminder(reminder: ReminderEntity): Long = withContext(Dispatchers.IO) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        transactionDao.clearAll()
        debtPaymentDao.clearAll()
        debtDao.clearAll()
        accountDao.clearAll()
        accaFeeDao.clearAll()
        savingsGoalDao.clearAll()
        budgetDao.clearAll()
        reminderDao.clearAll()
        initializeDefaultDataIfNeeded()
    }

    suspend fun loadDemoData() = withContext(Dispatchers.IO) {
        transactionDao.clearAll()
        debtPaymentDao.clearAll()
        debtDao.clearAll()

        val accounts = accountDao.getAllAccounts()
        val cashAcc = accounts.firstOrNull { it.type == AccountType.CASH } ?: accounts.first()
        val bankAcc = accounts.firstOrNull { it.type == AccountType.BANK } ?: accounts.first()
        val upiAcc = accounts.firstOrNull { it.type == AccountType.UPI } ?: accounts.first()

        val now = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000

        // Incomes
        addTransaction(
            TransactionEntity(
                amount = 12000.0,
                type = TransactionType.INCOME,
                category = "Pocket Money / Allowance",
                timestamp = now - dayMs * 12,
                accountId = bankAcc.id,
                paymentMethod = "Bank Transfer",
                notes = "Monthly support from parents"
            )
        )
        addTransaction(
            TransactionEntity(
                amount = 8000.0,
                type = TransactionType.INCOME,
                category = "Accounting Internship",
                timestamp = now - dayMs * 5,
                accountId = bankAcc.id,
                paymentMethod = "NEFT",
                notes = "Stipend from CA firm in Ernakulam"
            )
        )

        // Internal Transfer (Bank to UPI) - zero income/expense
        addTransaction(
            TransactionEntity(
                amount = 3000.0,
                type = TransactionType.TRANSFER,
                category = "Account Transfer",
                timestamp = now - dayMs * 10,
                accountId = bankAcc.id,
                toAccountId = upiAcc.id,
                paymentMethod = "UPI Load",
                notes = "Transferred from SBI to Google Pay for daily expenses"
            )
        )

        // Expenses
        addTransaction(
            TransactionEntity(
                amount = 120.0,
                type = TransactionType.EXPENSE,
                category = "Food & Mess",
                timestamp = now - 1000L * 60 * 60 * 3,
                accountId = cashAcc.id,
                paymentMethod = "Cash",
                notes = "Lunch at Kerala College Canteen (Meals + Omelette)"
            )
        )
        addTransaction(
            TransactionEntity(
                amount = 45.0,
                type = TransactionType.EXPENSE,
                category = "Tea & Bakery",
                timestamp = now - 1000L * 60 * 60 * 1,
                accountId = upiAcc.id,
                paymentMethod = "Google Pay",
                notes = "Evening Sulaimani & Pazham Pori"
            )
        )
        addTransaction(
            TransactionEntity(
                amount = 75.0,
                type = TransactionType.EXPENSE,
                category = "Bus Fare (KSRTC/Private)",
                timestamp = now - dayMs * 1,
                accountId = cashAcc.id,
                paymentMethod = "Cash",
                notes = "KSRTC Fast Passenger bus to ACCA coaching academy"
            )
        )
        addTransaction(
            TransactionEntity(
                amount = 350.0,
                type = TransactionType.EXPENSE,
                category = "Petrol & Bike",
                timestamp = now - dayMs * 3,
                accountId = upiAcc.id,
                paymentMethod = "Google Pay",
                notes = "Petrol for scooter"
            )
        )
        addTransaction(
            TransactionEntity(
                amount = 1800.0,
                type = TransactionType.EXPENSE,
                category = "Books & Study Materials",
                timestamp = now - dayMs * 7,
                accountId = upiAcc.id,
                paymentMethod = "Google Pay",
                notes = "Kaplan Financial Reporting (FR) Exam Kit & Study Text"
            )
        )
        addTransaction(
            TransactionEntity(
                amount = 299.0,
                type = TransactionType.EXPENSE,
                category = "Mobile Recharge & WiFi",
                timestamp = now - dayMs * 8,
                accountId = upiAcc.id,
                paymentMethod = "Google Pay",
                notes = "Jio 1.5GB/day 28-days student recharge"
            )
        )

        // Debts Demo Data
        val debtorId = addDebt(
            DebtEntity(
                personName = "Rahul (ACCA Study Partner)",
                phoneNumber = "+91 98470 12345",
                type = DebtType.DEBTOR,
                initialAmount = 2500.0,
                paidOrReceivedAmount = 1000.0,
                date = now - dayMs * 10,
                dueDate = now + dayMs * 5,
                notes = "Split cost for Kaplan Study Kits & CBE Mocks",
                status = DebtStatus.PARTIALLY_PAID
            )
        )
        debtPaymentDao.insertPayment(
            DebtPaymentEntity(
                debtId = debtorId,
                amount = 1000.0,
                timestamp = now - dayMs * 2,
                accountId = upiAcc.id,
                paymentMethod = "Google Pay",
                notes = "Part payment received via GPay"
            )
        )

        addDebt(
            DebtEntity(
                personName = "Anil (Hostel Roommate)",
                phoneNumber = "+91 97455 67890",
                type = DebtType.CREDITOR,
                initialAmount = 600.0,
                paidOrReceivedAmount = 0.0,
                date = now - dayMs * 4,
                dueDate = now + dayMs * 7,
                notes = "Borrowed for bus pass when server was down",
                status = DebtStatus.ACTIVE
            )
        )
    }

    suspend fun exportToCsv(): String = withContext(Dispatchers.IO) {
        val transactions = transactionDao.getAllTransactions()
        val accountsMap = accountDao.getAllAccounts().associateBy { it.id }

        val sb = StringBuilder()
        sb.append("ID,Date,Time,Type,Category,Amount(INR),Account,To_Account,Payment_Method,Notes,ACCA_Related\n")

        for (tx in transactions) {
            val fromAccName = accountsMap[tx.accountId]?.name ?: "Account #${tx.accountId}"
            val toAccName = tx.toAccountId?.let { accountsMap[it]?.name } ?: ""
            val escape = { s: String -> "\"${s.replace("\"", "\"\"")}\"" }

            sb.append("${tx.id},")
            sb.append("${escape(DateFormatter.formatDate(tx.timestamp))},")
            sb.append("${escape(DateFormatter.formatTime(tx.timestamp))},")
            sb.append("${escape(tx.type.name)},")
            sb.append("${escape(tx.category)},")
            sb.append("${String.format(java.util.Locale.ENGLISH, "%.2f", tx.amount)},")
            sb.append("${escape(fromAccName)},")
            sb.append("${escape(toAccName)},")
            sb.append("${escape(tx.paymentMethod)},")
            sb.append("${escape(tx.notes)},")
            sb.append("${if (tx.isAccaRelated) "YES" else "NO"}\n")
        }
        sb.toString()
    }

    suspend fun generateMonthlySummaryText(): String = withContext(Dispatchers.IO) {
        val transactions = transactionDao.getAllTransactions()
        val start = DateFormatter.getStartOfMonth()
        val end = DateFormatter.getEndOfMonth()

        val monthTx = transactions.filter { it.timestamp in start..end }
        val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netCashFlow = income - expense
        val savings = monthTx.filter { it.type == TransactionType.GOAL_SAVING }.sumOf { it.amount }

        val debts = debtDao.getAllDebts()
        val totalReceivable = debts.filter { it.type == DebtType.DEBTOR && it.status != DebtStatus.SETTLED }
            .sumOf { it.remainingBalance }
        val totalPayable = debts.filter { it.type == DebtType.CREDITOR && it.status != DebtStatus.SETTLED }
            .sumOf { it.remainingBalance }
        val netDebtPosition = totalReceivable - totalPayable

        val totalAccountsBalance = accountDao.getAllAccounts().sumOf { it.balance }

        buildString {
            append("📊 MY MONEY MATE - ACCA STUDENT EDITION\n")
            append("Monthly Financial Statement (${DateFormatter.getMonthName()})\n")
            append("-------------------------------------------\n")
            append("💰 Total Available Money: ${CurrencyFormatter.formatInr(totalAccountsBalance)}\n")
            append("📈 Total Income This Month: ${CurrencyFormatter.formatInr(income)}\n")
            append("📉 Total Expenses This Month: ${CurrencyFormatter.formatInr(expense)}\n")
            append("💵 Net Cash Flow: ${CurrencyFormatter.formatInr(netCashFlow)}\n")
            append("🎯 Allocated to Savings: ${CurrencyFormatter.formatInr(savings)}\n")
            append("-------------------------------------------\n")
            append("🤝 DEBTS & CREDITORS POSITION:\n")
            append("• Money Owed to Me (Receivables): ${CurrencyFormatter.formatInr(totalReceivable)}\n")
            append("• Money I Owe (Payables): ${CurrencyFormatter.formatInr(totalPayable)}\n")
            append("• Net Debt Position: ${if (netDebtPosition >= 0) "+" else ""}${CurrencyFormatter.formatInr(netDebtPosition)}\n")
            append("-------------------------------------------\n")
            append("Keep preparing for your ACCA papers with financial discipline! 🎓\n")
        }
    }
}
