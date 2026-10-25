package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER,
    GOAL_SAVING,
    DEBT_COLLECTED, // Debtor paid me back (increases account, not income)
    DEBT_REPAID     // I paid creditor back (decreases account, not expense)
}

enum class AccountType {
    CASH,
    BANK,
    UPI,
    OTHER
}

enum class FeeCategory {
    TUITION,
    EXAM_FEE,
    ANNUAL_SUBSCRIPTION,
    STUDY_MATERIALS
}

enum class DebtType {
    DEBTOR,   // Money owed to me (Receivable)
    CREDITOR  // Money I owe others (Payable)
}

enum class DebtStatus {
    ACTIVE,
    PARTIALLY_PAID,
    SETTLED
}

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val accountDetail: String = "",
    val colorHex: String = "#14B8A6",
    val isDefault: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val timestamp: Long,
    val accountId: Long,
    val toAccountId: Long? = null,
    val goalId: Long? = null,
    val debtId: Long? = null,
    val notes: String = "",
    val paymentMethod: String = "Cash",
    val isAccaRelated: Boolean = false,
    val accaFeeId: Long? = null
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val phoneNumber: String = "",
    val type: DebtType, // DEBTOR (owed to me) or CREDITOR (I owe)
    val initialAmount: Double,
    val paidOrReceivedAmount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val notes: String = "",
    val status: DebtStatus = DebtStatus.ACTIVE,
    val initialAccountId: Long? = null
) {
    val remainingBalance: Double
        get() = (initialAmount - paidOrReceivedAmount).coerceAtLeast(0.0)

    val progressPercent: Float
        get() = if (initialAmount > 0) (paidOrReceivedAmount / initialAmount).toFloat().coerceIn(0f, 1f) else 1f

    val isOverdue: Boolean
        get() = status != DebtStatus.SETTLED && dueDate != null && dueDate < System.currentTimeMillis()
}

@Entity(tableName = "debt_payments")
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val debtId: Long,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val accountId: Long,
    val paymentMethod: String = "Cash",
    val notes: String = ""
)

@Entity(tableName = "acca_fees")
data class AccaFeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val category: FeeCategory,
    val dueDate: Long? = null,
    val status: String = "PENDING", // PENDING, PARTIAL, COMPLETED
    val referenceNote: String = ""
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val targetDate: Long? = null,
    val colorHex: String = "#2DD4BF",
    val notes: String = ""
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "__OVERALL__" or category name
    val monthKey: String, // "YYYY-MM"
    val budgetAmount: Double,
    val carriedForwardAmount: Double = 0.0
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val note: String = "",
    val dueDate: Long,
    val category: String = "ACCA", // ACCA, EXAM, BUDGET, RECURRING, DEBT
    val isEnabled: Boolean = true
)
