package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.model.FeeCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CurrencyFormatter
import com.example.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccaFeesScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accaFees by viewModel.accaFees.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    val totalFees = accaFees.sumOf { it.totalAmount }
    val paidFees = accaFees.sumOf { it.paidAmount }
    val remainingFees = (totalFees - paidFees).coerceAtLeast(0.0)
    val overallProgress = if (totalFees > 0) (paidFees / totalFees).toFloat() else 0f

    var showAddFeeDialog by remember { mutableStateOf(false) }
    var feeToEdit by remember { mutableStateOf<AccaFeeEntity?>(null) }
    var feeForInstallment by remember { mutableStateOf<AccaFeeEntity?>(null) }
    var feeToDelete by remember { mutableStateOf<AccaFeeEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("ACCA Fees Manager", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFeeDialog = true },
                containerColor = TealPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_acca_fee_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expected Fee")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("acca_fees_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ACCA Course Overview Progress Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().testTag("acca_summary_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACCA Financial Progress",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(
                                text = "${(overallProgress * 100).toInt()}% COMPLETED",
                                backgroundColor = TealPrimary.copy(alpha = 0.2f),
                                contentColor = TealPrimaryLight
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { overallProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = TealPrimary,
                            trackColor = TealPrimary.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Total Expected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = CurrencyFormatter.formatInr(totalFees),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Total Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = CurrencyFormatter.formatInr(paidFees),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = IncomeGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Remaining Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = CurrencyFormatter.formatInr(remainingFees),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (remainingFees > 0) AccentAmber else IncomeGreen
                                )
                            }
                        }
                    }
                }
            }

            // Fee Items List
            item {
                Text(
                    text = "Fees Breakdown & Installments",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(accaFees, key = { it.id }) { fee ->
                AccaFeeItemCard(
                    fee = fee,
                    onPayInstallment = { feeForInstallment = fee },
                    onEdit = { feeToEdit = fee },
                    onDelete = { feeToDelete = fee }
                )
            }
        }
    }

    // Add / Edit Fee Dialog
    if (showAddFeeDialog || feeToEdit != null) {
        val editing = feeToEdit
        AddEditAccaFeeDialog(
            fee = editing,
            onDismiss = {
                showAddFeeDialog = false
                feeToEdit = null
            },
            onSave = { newFee ->
                if (editing != null) {
                    viewModel.updateAccaFee(newFee)
                } else {
                    viewModel.addAccaFee(newFee)
                }
                showAddFeeDialog = false
                feeToEdit = null
            }
        )
    }

    // Record Fee Installment Dialog
    feeForInstallment?.let { fee ->
        RecordFeeInstallmentDialog(
            fee = fee,
            accounts = accounts,
            onDismiss = { feeForInstallment = null },
            onConfirm = { paymentTx ->
                viewModel.addTransaction(paymentTx)
                feeForInstallment = null
            }
        )
    }

    // Delete confirmation
    feeToDelete?.let { fee ->
        AlertDialog(
            onDismissRequest = { feeToDelete = null },
            title = { Text("Delete Fee Item?") },
            text = { Text("Are you sure you want to delete '${fee.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccaFee(fee)
                        feeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { feeToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AccaFeeItemCard(
    fee: AccaFeeEntity,
    onPayInstallment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remaining = (fee.totalAmount - fee.paidAmount).coerceAtLeast(0.0)
    val pct = if (fee.totalAmount > 0) (fee.paidAmount / fee.totalAmount).toFloat() else 0f

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fee.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = fee.category.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = TealPrimary
                    )
                }

                StatusBadge(
                    text = if (remaining <= 0) "FULLY PAID" else "${(pct * 100).toInt()}% PAID",
                    backgroundColor = if (remaining <= 0) IncomeGreen.copy(alpha = 0.2f) else AccentAmber.copy(alpha = 0.2f),
                    contentColor = if (remaining <= 0) IncomeGreen else AccentAmber
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { pct.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (remaining <= 0) IncomeGreen else TealPrimary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Paid: ${CurrencyFormatter.formatInr(fee.paidAmount)} / ${CurrencyFormatter.formatInr(fee.totalAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Remaining: ${CurrencyFormatter.formatInr(remaining)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (remaining > 0) AccentAmber else IncomeGreen
                )
            }

            if (fee.referenceNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ref: ${fee.referenceNote}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            if (fee.dueDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: ${DateFormatter.formatDate(fee.dueDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (remaining > 0) {
                    Button(
                        onClick = onPayInstallment,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pay Installment", fontSize = 12.sp)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Settled in Full", style = MaterialTheme.typography.labelSmall, color = IncomeGreen)
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditAccaFeeDialog(
    fee: AccaFeeEntity?,
    onDismiss: () -> Unit,
    onSave: (AccaFeeEntity) -> Unit
) {
    var title by remember { mutableStateOf(fee?.title ?: "") }
    var totalAmountStr by remember { mutableStateOf(fee?.totalAmount?.toInt()?.toString() ?: "") }
    var category by remember { mutableStateOf(fee?.category ?: FeeCategory.TUITION) }
    var referenceNote by remember { mutableStateOf(fee?.referenceNote ?: "") }
    var daysToDue by remember { mutableStateOf("30") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (fee == null) "Add ACCA Fee Item" else "Edit Fee Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Fee Title (e.g. FR Coaching, Exam Entry)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Fee Category:", style = MaterialTheme.typography.labelSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FeeCategory.values().forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.name.substringBefore("_"), fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = totalAmountStr,
                    onValueChange = { totalAmountStr = it },
                    label = { Text("Total Expected Amount in ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = referenceNote,
                    onValueChange = { referenceNote = it },
                    label = { Text("Receipt Ref / Institute Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = daysToDue,
                    onValueChange = { daysToDue = it },
                    label = { Text("Due In (Days from today)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotBlank()) {
                    Text(text = error, color = ExpenseRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val total = totalAmountStr.toDoubleOrNull() ?: 0.0
                    if (title.isBlank() || total <= 0.0) {
                        error = "Please enter a valid title and total amount"
                        return@Button
                    }
                    val days = daysToDue.toIntOrNull() ?: 30
                    val dueTime = System.currentTimeMillis() + days * 24L * 60 * 60 * 1000

                    val newFee = fee?.copy(
                        title = title.trim(),
                        totalAmount = total,
                        category = category,
                        referenceNote = referenceNote.trim(),
                        dueDate = dueTime
                    ) ?: AccaFeeEntity(
                        title = title.trim(),
                        totalAmount = total,
                        paidAmount = 0.0,
                        category = category,
                        referenceNote = referenceNote.trim(),
                        dueDate = dueTime,
                        status = "PENDING"
                    )
                    onSave(newFee)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RecordFeeInstallmentDialog(
    fee: AccaFeeEntity,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit
) {
    val remaining = (fee.totalAmount - fee.paidAmount).coerceAtLeast(0.0)
    var amountStr by remember { mutableStateOf(remaining.toInt().toString()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var paymentMethod by remember { mutableStateOf("Google Pay (UPI)") }
    var refNote by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pay Installment: ${fee.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Remaining to pay: ${CurrencyFormatter.formatInr(remaining)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = AccentAmber
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Installment Amount in ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Pay from Account:", style = MaterialTheme.typography.labelSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    accounts.take(3).forEach { acc ->
                        FilterChip(
                            selected = selectedAccountId == acc.id,
                            onClick = { selectedAccountId = acc.id },
                            label = { Text(acc.name, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = refNote,
                    onValueChange = { refNote = it },
                    label = { Text("Transaction Reference / Receipt No.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotBlank()) {
                    Text(text = error, color = ExpenseRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt <= 0.0) {
                        error = "Enter a valid amount"
                        return@Button
                    }
                    if (amt > remaining + 0.01) {
                        error = "Amount cannot exceed remaining fee (${CurrencyFormatter.formatInr(remaining)})"
                        return@Button
                    }
                    val tx = TransactionEntity(
                        amount = amt,
                        type = TransactionType.EXPENSE,
                        category = fee.title,
                        timestamp = System.currentTimeMillis(),
                        accountId = selectedAccountId,
                        paymentMethod = paymentMethod,
                        notes = "ACCA Fee Installment. Ref: $refNote",
                        isAccaRelated = true,
                        accaFeeId = fee.id
                    )
                    onConfirm(tx)
                }
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
