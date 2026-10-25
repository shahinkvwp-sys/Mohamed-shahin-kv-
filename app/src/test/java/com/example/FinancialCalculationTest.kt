package com.example

import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.AccaFeeEntity
import com.example.data.model.DebtEntity
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.FeeCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialCalculationTest {

    @Test
    fun testCurrencyFormatting() {
        assertEquals("₹1,50,000", CurrencyFormatter.formatInr(150000.0))
        assertEquals("₹450", CurrencyFormatter.formatInr(450.0))
        assertEquals("₹1,250.50", CurrencyFormatter.formatInr(1250.50))
    }

    @Test
    fun testIncomeAndExpenseCalculation() {
        val transactions = listOf(
            TransactionEntity(id = 1, amount = 12000.0, type = TransactionType.INCOME, category = "Pocket Money", timestamp = 0, accountId = 1),
            TransactionEntity(id = 2, amount = 8000.0, type = TransactionType.INCOME, category = "Internship", timestamp = 0, accountId = 1),
            TransactionEntity(id = 3, amount = 1800.0, type = TransactionType.EXPENSE, category = "Books", timestamp = 0, accountId = 1),
            TransactionEntity(id = 4, amount = 350.0, type = TransactionType.EXPENSE, category = "Petrol", timestamp = 0, accountId = 1),
            // Transfers and Debt settlements must NOT count towards P&L income/expenses
            TransactionEntity(id = 5, amount = 2000.0, type = TransactionType.TRANSFER, category = "Transfer", timestamp = 0, accountId = 1, toAccountId = 2),
            TransactionEntity(id = 6, amount = 500.0, type = TransactionType.DEBT_COLLECTED, category = "Debt Repayment", timestamp = 0, accountId = 1),
            TransactionEntity(id = 7, amount = 300.0, type = TransactionType.DEBT_REPAID, category = "Debt Repayment", timestamp = 0, accountId = 1)
        )

        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netCashFlow = totalIncome - totalExpense

        assertEquals(20000.0, totalIncome, 0.001)
        assertEquals(2150.0, totalExpense, 0.001)
        assertEquals(17850.0, netCashFlow, 0.001)
    }

    @Test
    fun testAccountTransferIntegrity() {
        var bankBalance = 10000.0
        var upiBalance = 2000.0
        val transferAmount = 3000.0

        val initialTotal = bankBalance + upiBalance

        // Execute transfer
        bankBalance -= transferAmount
        upiBalance += transferAmount

        val finalTotal = bankBalance + upiBalance

        // Total wealth must be conserved
        assertEquals(initialTotal, finalTotal, 0.001)
        assertEquals(7000.0, bankBalance, 0.001)
        assertEquals(5000.0, upiBalance, 0.001)
    }

    @Test
    fun testAccaFeeRemainingAndProgress() {
        val totalTuition = 65000.0
        val installment1 = 20000.0
        val installment2 = 15000.0

        val paidSoFar = installment1 + installment2
        val remaining = totalTuition - paidSoFar
        val progress = (paidSoFar / totalTuition).toFloat()

        assertEquals(35000.0, paidSoFar, 0.001)
        assertEquals(30000.0, remaining, 0.001)
        assertEquals(0.538f, progress, 0.01f)
    }

    @Test
    fun testDebtorRepaymentLogic() {
        val debt = DebtEntity(
            id = 1,
            personName = "Rahul",
            type = DebtType.DEBTOR,
            initialAmount = 1500.0,
            paidOrReceivedAmount = 500.0
        )

        assertEquals(1000.0, debt.remainingBalance, 0.001)
        assertFalse(debt.status == DebtStatus.SETTLED)

        // Settle remaining 1000
        val settledDebt = debt.copy(paidOrReceivedAmount = debt.paidOrReceivedAmount + 1000.0, status = DebtStatus.SETTLED)
        assertEquals(0.0, settledDebt.remainingBalance, 0.001)
        assertTrue(settledDebt.status == DebtStatus.SETTLED)
    }

    @Test
    fun testBudgetThresholdWarnings() {
        val monthlyBudget = 10000.0
        val spend7500 = 7500.0
        val spend8500 = 8500.0
        val spend10500 = 10500.0

        val usage75 = (spend7500 / monthlyBudget).toFloat()
        val usage85 = (spend8500 / monthlyBudget).toFloat()
        val usage105 = (spend10500 / monthlyBudget).toFloat()

        assertTrue(usage75 < 0.8f)
        assertTrue(usage85 >= 0.8f && usage85 < 1.0f)
        assertTrue(usage105 >= 1.0f)
    }
}
