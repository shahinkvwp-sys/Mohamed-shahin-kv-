package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.AccaFeeEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TransferBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    initialTransaction: TransactionEntity? = null,
    initialType: TransactionType = TransactionType.EXPENSE,
    accounts: List<AccountEntity>,
    accaFees: List<AccaFeeEntity>,
    savingsGoals: List<SavingsGoalEntity>,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    var type by remember { mutableStateOf(initialTransaction?.type ?: initialType) }
    var amountStr by remember {
        mutableStateOf(initialTransaction?.amount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }

    val defaultExpenseCategories = listOf(
        "Food & Mess", "Tea & Bakery", "Bus Fare (KSRTC/Private)", "Petrol & Bike",
        "Transport", "ACCA Course Fees", "ACCA Exam Fees", "Books & Study Materials",
        "Stationery & Printing", "Mobile Recharge & WiFi", "Hostel & Rent",
        "Shopping", "Subscriptions", "Entertainment", "Personal Expenses", "Other"
    )

    val defaultIncomeCategories = listOf(
        "Pocket Money / Allowance", "Accounting Internship", "Part-time Job",
        "Family Gift", "Freelance / Bookkeeping", "Scholarship", "Other Income"
    )

    var category by remember {
        mutableStateOf(
            initialTransaction?.category ?: if (type == TransactionType.INCOME) "Pocket Money / Allowance"
            else if (type == TransactionType.TRANSFER) "Account Transfer"
            else if (type == TransactionType.GOAL_SAVING) "Savings Allocation"
            else "Food & Mess"
        )
    }

    var selectedAccountId by remember {
        mutableStateOf(initialTransaction?.accountId ?: accounts.firstOrNull()?.id ?: 1L)
    }
    var toAccountId by remember {
        mutableStateOf(
            initialTransaction?.toAccountId ?: accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id ?: 1L
        )
    }
    var selectedGoalId by remember {
        mutableStateOf(initialTransaction?.goalId ?: savingsGoals.firstOrNull()?.id)
    }

    var paymentMethod by remember {
        mutableStateOf(initialTransaction?.paymentMethod ?: "Cash")
    }
    val paymentMethods = listOf("Cash", "Google Pay (UPI)", "PhonePe", "Paytm", "Bank Transfer", "Debit Card")

    var notes by remember { mutableStateOf(initialTransaction?.notes ?: "") }
    var isAccaRelated by remember { mutableStateOf(initialTransaction?.isAccaRelated ?: false) }
    var selectedAccaFeeId by remember { mutableStateOf(initialTransaction?.accaFeeId ?: accaFees.firstOrNull()?.id) }

    var customCategoryMode by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTransaction == null) "Record Transaction" else "Edit Transaction",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_transaction_dialog_form"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Switcher: Expense | Income | Transfer | Savings
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = type == TransactionType.EXPENSE,
                            onClick = {
                                type = TransactionType.EXPENSE
                                category = "Food & Mess"
                            },
                            label = { Text("Expense", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = type == TransactionType.INCOME,
                            onClick = {
                                type = TransactionType.INCOME
                                category = "Pocket Money / Allowance"
                            },
                            label = { Text("Income", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = type == TransactionType.TRANSFER,
                            onClick = {
                                type = TransactionType.TRANSFER
                                category = "Account Transfer"
                            },
                            label = { Text("Transfer", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = type == TransactionType.GOAL_SAVING,
                            onClick = {
                                type = TransactionType.GOAL_SAVING
                                category = "Savings Allocation"
                            },
                            label = { Text("Save", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Amount Field with ₹ Prefix
                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount *") },
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = TealPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_amount_field")
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    // Quick amount chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(50, 100, 500, 1000).forEach { amt ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        val current = amountStr.toDoubleOrNull() ?: 0.0
                                        amountStr = (current + amt).toInt().toString()
                                    }
                            ) {
                                Text(
                                    text = "+₹$amt",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Account Selection
                item {
                    Text(
                        text = if (type == TransactionType.TRANSFER) "From Account:" else "Account Used:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        accounts.forEach { acc ->
                            FilterChip(
                                selected = selectedAccountId == acc.id,
                                onClick = { selectedAccountId = acc.id },
                                label = { Text(acc.name, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // If Transfer: Destination Account
                if (type == TransactionType.TRANSFER) {
                    item {
                        Text(
                            text = "To Account:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            accounts.forEach { acc ->
                                FilterChip(
                                    selected = toAccountId == acc.id,
                                    onClick = { toAccountId = acc.id },
                                    label = { Text(acc.name, fontSize = 12.sp) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ℹ️ Internal transfer will not affect income or expenses.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealPrimary
                        )
                    }
                }

                // If Goal Saving: Destination Goal
                if (type == TransactionType.GOAL_SAVING) {
                    item {
                        Text(
                            text = "Select Savings Goal:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            savingsGoals.forEach { goal ->
                                FilterChip(
                                    selected = selectedGoalId == goal.id,
                                    onClick = { selectedGoalId = goal.id },
                                    label = { Text(goal.title, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }

                // Category Selection (For Expense & Income)
                if (type == TransactionType.EXPENSE || type == TransactionType.INCOME) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Category:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            TextButton(onClick = { customCategoryMode = !customCategoryMode }) {
                                Text(
                                    text = if (customCategoryMode) "Preset List" else "+ Custom",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        if (customCategoryMode) {
                            OutlinedTextField(
                                value = customCategoryText,
                                onValueChange = {
                                    customCategoryText = it
                                    category = it
                                },
                                label = { Text("Custom Category Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            val categoryList = if (type == TransactionType.EXPENSE) defaultExpenseCategories else defaultIncomeCategories
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                categoryList.forEach { cat ->
                                    FilterChip(
                                        selected = category.equals(cat, ignoreCase = true),
                                        onClick = { category = cat },
                                        label = { Text(cat, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Payment Method
                item {
                    Text(
                        text = "Payment Method:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        paymentMethods.forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // ACCA Fee Linkage Checkbox
                if (type == TransactionType.EXPENSE && accaFees.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isAccaRelated,
                                onCheckedChange = { isAccaRelated = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Link to ACCA Course / Exam Fee record",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (isAccaRelated) {
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                accaFees.forEach { fee ->
                                    FilterChip(
                                        selected = selectedAccaFeeId == fee.id,
                                        onClick = { selectedAccaFeeId = fee.id },
                                        label = { Text(fee.title, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Description (Optional)") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMessage.isNotBlank()) {
                    item {
                        Text(
                            text = errorMessage,
                            color = ExpenseRed,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        errorMessage = "Please enter a valid amount greater than 0"
                        return@Button
                    }
                    if (type == TransactionType.TRANSFER && selectedAccountId == toAccountId) {
                        errorMessage = "From and To accounts cannot be the same"
                        return@Button
                    }

                    val finalCategory = if (customCategoryMode && customCategoryText.isNotBlank()) {
                        customCategoryText.trim()
                    } else category

                    val tx = TransactionEntity(
                        id = initialTransaction?.id ?: 0L,
                        amount = amount,
                        type = type,
                        category = finalCategory,
                        timestamp = initialTransaction?.timestamp ?: System.currentTimeMillis(),
                        accountId = selectedAccountId,
                        toAccountId = if (type == TransactionType.TRANSFER) toAccountId else null,
                        goalId = if (type == TransactionType.GOAL_SAVING) selectedGoalId else null,
                        notes = notes.trim(),
                        paymentMethod = paymentMethod,
                        isAccaRelated = isAccaRelated,
                        accaFeeId = if (isAccaRelated) selectedAccaFeeId else null
                    )

                    onSave(tx)
                },
                modifier = Modifier.testTag("save_transaction_button")
            ) {
                Text(if (initialTransaction == null) "Add Transaction" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
