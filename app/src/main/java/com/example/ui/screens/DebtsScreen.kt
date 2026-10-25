package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.DebtEntity
import com.example.data.model.DebtPaymentEntity
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
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
fun DebtsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val debts by viewModel.debts.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val debtSummary by viewModel.debtSummary.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Owed to Me (Debtors), 1: I Owe (Creditors), 2: Settled
    var searchQuery by remember { mutableStateOf("") }
    var filterOverdueOnly by remember { mutableStateOf(false) }

    // Dialog states
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var debtToEdit by remember { mutableStateOf<DebtEntity?>(null) }
    var debtToDelete by remember { mutableStateOf<DebtEntity?>(null) }
    var debtForPayment by remember { mutableStateOf<DebtEntity?>(null) }
    var debtForHistory by remember { mutableStateOf<DebtEntity?>(null) }

    val filteredList = debts.filter { debt ->
        val matchesTab = when (selectedTab) {
            0 -> debt.type == DebtType.DEBTOR && debt.status != DebtStatus.SETTLED
            1 -> debt.type == DebtType.CREDITOR && debt.status != DebtStatus.SETTLED
            else -> debt.status == DebtStatus.SETTLED
        }

        val matchesSearch = searchQuery.isBlank() ||
            debt.personName.contains(searchQuery, ignoreCase = true) ||
            debt.phoneNumber.contains(searchQuery) ||
            debt.notes.contains(searchQuery, ignoreCase = true)

        val matchesOverdue = !filterOverdueOnly || debt.isOverdue

        matchesTab && matchesSearch && matchesOverdue
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "People & Debts",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDebtDialog = true },
                containerColor = TealPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_debt_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Person / Debt")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Debt Position Summary Header
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().testTag("debt_summary_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Debt & Credit Position",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (debtSummary.overdueCount > 0) {
                                StatusBadge(
                                    text = "${debtSummary.overdueCount} Overdue",
                                    backgroundColor = ExpenseRed.copy(alpha = 0.2f),
                                    contentColor = ExpenseRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Money Owed To Me (Receivables)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Owed to Me (Receivables)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatInr(debtSummary.totalReceivable),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = IncomeGreen
                                )
                                Text(
                                    text = "${debtSummary.activeDebtorsCount} people",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Money I Owe (Payables)
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "I Owe (Payables)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatInr(debtSummary.totalPayable),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ExpenseRed
                                )
                                Text(
                                    text = "${debtSummary.activeCreditorsCount} people",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Net Position:",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val isPositive = debtSummary.netDebtPosition >= 0
                                Text(
                                    text = "${if (isPositive) "+" else ""}${CurrencyFormatter.formatInr(debtSummary.netDebtPosition)} ${if (isPositive) "(Net to Receive)" else "(Net to Pay)"}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isPositive) IncomeGreen else AccentAmber
                                )
                            }
                        }
                    }
                }
            }

            // 2. Tabs: Owed To Me | I Owe Others | Settled
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = TealPrimary,
                    edgePadding = 0.dp
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Owed to Me (${debts.count { it.type == DebtType.DEBTOR && it.status != DebtStatus.SETTLED }})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("I Owe Others (${debts.count { it.type == DebtType.CREDITOR && it.status != DebtStatus.SETTLED }})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Settled (${debts.count { it.status == DebtStatus.SETTLED }})", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // 3. Search Bar and Filter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, phone or notes...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("debts_search_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterChip(
                        selected = filterOverdueOnly,
                        onClick = { filterOverdueOnly = !filterOverdueOnly },
                        label = { Text("Overdue") },
                        leadingIcon = if (filterOverdueOnly) {
                            { Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // 4. Debt List
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (selectedTab == 0) "No active receivables! Tap + to record money you lent."
                                else if (selectedTab == 1) "No active payables! You don't owe anyone right now."
                                else "No settled records matching your search.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { debt ->
                    DebtCardItem(
                        debt = debt,
                        onRecordPayment = { debtForPayment = debt },
                        onViewHistory = { debtForHistory = debt },
                        onEdit = { debtToEdit = debt },
                        onDelete = { debtToDelete = debt },
                        onCall = {
                            if (debt.phoneNumber.isNotBlank()) {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${debt.phoneNumber.replace(" ", "")}"))
                                    context.startActivity(intent)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // ADD / EDIT DEBT DIALOG
    if (showAddDebtDialog || debtToEdit != null) {
        val editing = debtToEdit
        AddEditDebtDialog(
            debt = editing,
            accounts = accounts,
            initialType = if (selectedTab == 1) DebtType.CREDITOR else DebtType.DEBTOR,
            onDismiss = {
                showAddDebtDialog = false
                debtToEdit = null
            },
            onSave = { newDebt, affectAccount ->
                if (editing != null) {
                    viewModel.updateDebt(newDebt)
                } else {
                    viewModel.addDebt(newDebt, affectAccount)
                }
                showAddDebtDialog = false
                debtToEdit = null
            }
        )
    }

    // RECORD PAYMENT DIALOG
    debtForPayment?.let { debt ->
        RecordDebtPaymentDialog(
            debt = debt,
            accounts = accounts,
            onDismiss = { debtForPayment = null },
            onConfirm = { payment ->
                viewModel.recordDebtPayment(payment, debt)
                debtForPayment = null
            }
        )
    }

    // PAYMENT HISTORY DIALOG
    debtForHistory?.let { debt ->
        DebtPaymentHistoryDialog(
            debt = debt,
            accounts = accounts,
            viewModel = viewModel,
            onDismiss = { debtForHistory = null }
        )
    }

    // DELETE CONFIRMATION DIALOG
    debtToDelete?.let { debt ->
        AlertDialog(
            onDismissRequest = { debtToDelete = null },
            title = { Text("Delete Record?") },
            text = { Text("Are you sure you want to delete ${debt.personName}'s debt record? Payment history will also be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDebt(debt)
                        debtToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DebtCardItem(
    debt: DebtEntity,
    onRecordPayment: () -> Unit,
    onViewHistory: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("debt_card_${debt.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name, Phone & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (debt.type == DebtType.DEBTOR) IncomeGreen.copy(alpha = 0.15f)
                                else ExpenseRed.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (debt.type == DebtType.DEBTOR) IncomeGreen else ExpenseRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = debt.personName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (debt.phoneNumber.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(onClick = onCall)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = debt.phoneNumber,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (debt.status == DebtStatus.SETTLED) {
                        StatusBadge(
                            text = "SETTLED",
                            backgroundColor = IncomeGreen.copy(alpha = 0.2f),
                            contentColor = IncomeGreen
                        )
                    } else if (debt.isOverdue) {
                        StatusBadge(
                            text = "OVERDUE",
                            backgroundColor = ExpenseRed.copy(alpha = 0.2f),
                            contentColor = ExpenseRed
                        )
                    } else {
                        StatusBadge(
                            text = if (debt.type == DebtType.DEBTOR) "RECEIVABLE" else "PAYABLE",
                            backgroundColor = if (debt.type == DebtType.DEBTOR) TealPrimary.copy(alpha = 0.2f) else AccentAmber.copy(alpha = 0.2f),
                            contentColor = if (debt.type == DebtType.DEBTOR) TealPrimaryLight else AccentAmber
                        )
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount row: Original | Paid | Outstanding Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Original", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = CurrencyFormatter.formatInr(debt.initialAmount),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (debt.type == DebtType.DEBTOR) "Received" else "Repaid",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatInr(debt.paidOrReceivedAmount),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = IncomeGreen
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Balance Due",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatInr(debt.remainingBalance),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (debt.type == DebtType.DEBTOR) IncomeGreen else ExpenseRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { debt.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (debt.type == DebtType.DEBTOR) IncomeGreen else TealPrimary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Notes and Due Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (debt.notes.isNotBlank()) debt.notes else "No notes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                if (debt.dueDate != null) {
                    Text(
                        text = "Due: ${DateFormatter.formatDate(debt.dueDate)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = if (debt.isOverdue) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Record Payment & History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (debt.status != DebtStatus.SETTLED) {
                    Button(
                        onClick = onRecordPayment,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (debt.type == DebtType.DEBTOR) IncomeGreen else TealPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (debt.type == DebtType.DEBTOR) "Collect Money" else "Pay Back",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onViewHistory,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "History", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AddEditDebtDialog(
    debt: DebtEntity?,
    accounts: List<AccountEntity>,
    initialType: DebtType,
    onDismiss: () -> Unit,
    onSave: (DebtEntity, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(debt?.personName ?: "") }
    var phone by remember { mutableStateOf(debt?.phoneNumber ?: "") }
    var type by remember { mutableStateOf(debt?.type ?: initialType) }
    var amountStr by remember { mutableStateOf(debt?.initialAmount?.toInt()?.toString() ?: "") }
    var notes by remember { mutableStateOf(debt?.notes ?: "") }
    var daysToDue by remember { mutableStateOf("15") }
    var affectAccount by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }

    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (debt == null) "Add Person & Debt" else "Edit Debt Record")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == DebtType.DEBTOR,
                        onClick = { type = DebtType.DEBTOR },
                        label = { Text("Owed to Me") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == DebtType.CREDITOR,
                        onClick = { type = DebtType.CREDITOR },
                        label = { Text("I Owe Others") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Person's Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount in ₹ *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = daysToDue,
                    onValueChange = { daysToDue = it },
                    label = { Text("Due in (Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Purpose (e.g. Split exam fees)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (debt == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = affectAccount,
                            onCheckedChange = { affectAccount = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (type == DebtType.DEBTOR) "Deduct from my account now" else "Add money to my account now",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (affectAccount && accounts.isNotEmpty()) {
                        Text(text = "Select Account:", style = MaterialTheme.typography.labelSmall)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            accounts.take(3).forEach { acc ->
                                FilterChip(
                                    selected = selectedAccountId == acc.id,
                                    onClick = { selectedAccountId = acc.id },
                                    label = { Text(acc.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                if (error.isNotBlank()) {
                    Text(text = error, color = ExpenseRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (name.isBlank()) {
                        error = "Please enter person's name"
                        return@Button
                    }
                    if (amount <= 0.0) {
                        error = "Please enter a valid amount"
                        return@Button
                    }
                    val days = daysToDue.toIntOrNull() ?: 15
                    val dueTimestamp = System.currentTimeMillis() + days * 24L * 60 * 60 * 1000

                    val newRecord = debt?.copy(
                        personName = name.trim(),
                        phoneNumber = phone.trim(),
                        type = type,
                        initialAmount = amount,
                        dueDate = dueTimestamp,
                        notes = notes.trim()
                    ) ?: DebtEntity(
                        personName = name.trim(),
                        phoneNumber = phone.trim(),
                        type = type,
                        initialAmount = amount,
                        paidOrReceivedAmount = 0.0,
                        dueDate = dueTimestamp,
                        notes = notes.trim(),
                        initialAccountId = if (affectAccount) selectedAccountId else null
                    )

                    onSave(newRecord, affectAccount)
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
fun RecordDebtPaymentDialog(
    debt: DebtEntity,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (DebtPaymentEntity) -> Unit
) {
    val remaining = debt.remainingBalance
    var amountStr by remember { mutableStateOf(remaining.toInt().toString()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var paymentMethod by remember { mutableStateOf("Google Pay (UPI)") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (debt.type == DebtType.DEBTOR) "Collect Money from ${debt.personName}"
                else "Repay Money to ${debt.personName}"
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Outstanding Balance: ${CurrencyFormatter.formatInr(remaining)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (debt.type == DebtType.DEBTOR) IncomeGreen else ExpenseRed
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Payment Amount in ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { amountStr = remaining.toInt().toString() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Full Balance", fontSize = 11.sp)
                    }
                    if (remaining >= 500) {
                        OutlinedButton(
                            onClick = { amountStr = (remaining / 2).toInt().toString() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("50% Partial", fontSize = 11.sp)
                        }
                    }
                }

                Text(
                    text = if (debt.type == DebtType.DEBTOR) "Deposit into Account:" else "Pay from Account:",
                    style = MaterialTheme.typography.labelSmall
                )

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
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Payment Reference / Note") },
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealPrimary.copy(alpha = 0.1f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "ℹ️ ACCA Accounting Rule: Debt repayments adjust your balance sheet and bank balance directly, and are never counted as ordinary income or expenses.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (error.isNotBlank()) {
                    Text(text = error, color = ExpenseRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        error = "Enter a valid payment amount"
                        return@Button
                    }
                    if (amount > remaining + 0.01) {
                        error = "Amount cannot exceed outstanding balance (${CurrencyFormatter.formatInr(remaining)})"
                        return@Button
                    }

                    val payment = DebtPaymentEntity(
                        debtId = debt.id,
                        amount = amount,
                        timestamp = System.currentTimeMillis(),
                        accountId = selectedAccountId,
                        paymentMethod = paymentMethod,
                        notes = note.trim()
                    )
                    onConfirm(payment)
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

@Composable
fun DebtPaymentHistoryDialog(
    debt: DebtEntity,
    accounts: List<AccountEntity>,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val payments by viewModel.getPaymentsForDebtFlow(debt.id).collectAsState(initial = emptyList())
    val accountsMap = remember(accounts) { accounts.associateBy { it.id } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("${debt.personName} - Payment History")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total: ${CurrencyFormatter.formatInr(debt.initialAmount)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Remaining: ${CurrencyFormatter.formatInr(debt.remainingBalance)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (debt.remainingBalance > 0) ExpenseRed else IncomeGreen
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (payments.isEmpty()) {
                    Text(
                        text = "No payments recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(payments, key = { it.id }) { p ->
                            val accName = accountsMap[p.accountId]?.name ?: "Account"
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = CurrencyFormatter.formatInr(p.amount),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = IncomeGreen
                                        )
                                        Text(
                                            text = "$accName • ${DateFormatter.formatDateTime(p.timestamp)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (p.notes.isNotBlank()) {
                                            Text(
                                                text = p.notes,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteDebtPayment(p, debt) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
