package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.AccaFeesScreen
import com.example.ui.screens.AddTransactionDialog
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtsScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Localization

enum class Screen {
    HOME,
    TRANSACTIONS,
    BUDGETS,
    ACCA,
    REPORTS,
    DEBTS,
    ACCOUNTS,
    SAVINGS,
    INSIGHTS,
    REMINDERS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setContent {
            val userPref by viewModel.userPreferences.collectAsState()

            MyApplicationTheme(darkTheme = userPref.darkTheme) {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MainViewModel) {
    val userPref by viewModel.userPreferences.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val accaFees by viewModel.accaFees.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val debtSummary by viewModel.debtSummary.collectAsState()
    val lang = userPref.language

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var isUnlocked by remember { mutableStateOf(!userPref.pinLockEnabled) }

    // Dialog state for adding/editing transactions
    var showAddTxDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var dialogInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }

    if (userPref.pinLockEnabled && !isUnlocked) {
        PinLockScreen(
            correctPin = userPref.userPin,
            onUnlocked = { isUnlocked = true }
        )
        return
    }

    // Back handling for sub-screens
    if (currentScreen != Screen.HOME) {
        BackHandler {
            currentScreen = Screen.HOME
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (currentScreen) {
                                Screen.HOME -> Localization.getString("app_title", lang)
                                Screen.TRANSACTIONS -> Localization.getString("tab_transactions", lang)
                                Screen.BUDGETS -> Localization.getString("tab_budgets", lang)
                                Screen.ACCA -> Localization.getString("tab_acca", lang)
                                Screen.REPORTS -> Localization.getString("tab_reports", lang)
                                Screen.DEBTS -> "People & Debts"
                                Screen.ACCOUNTS -> "Money Accounts"
                                Screen.SAVINGS -> "Savings Goals"
                                Screen.INSIGHTS -> "Financial Insights"
                                Screen.REMINDERS -> "Reminders"
                                Screen.SETTINGS -> "Settings"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (currentScreen == Screen.HOME) {
                            Text(
                                text = Localization.getString("app_subtitle", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (currentScreen != Screen.HOME && currentScreen != Screen.TRANSACTIONS &&
                        currentScreen != Screen.BUDGETS && currentScreen != Screen.ACCA &&
                        currentScreen != Screen.REPORTS
                    ) {
                        IconButton(onClick = { currentScreen = Screen.HOME }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    // Quick Action: People & Debts button with badge
                    IconButton(
                        onClick = { currentScreen = Screen.DEBTS },
                        modifier = Modifier.testTag("nav_debts_top_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (debtSummary.activeDebtorsCount + debtSummary.activeCreditorsCount > 0) {
                                    Badge(containerColor = TealPrimary) {
                                        Text("${debtSummary.activeDebtorsCount + debtSummary.activeCreditorsCount}")
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Group, contentDescription = "People & Debts")
                        }
                    }

                    // Accounts
                    IconButton(
                        onClick = { currentScreen = Screen.ACCOUNTS },
                        modifier = Modifier.testTag("nav_accounts_top_button")
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "Accounts")
                    }

                    // Reminders
                    IconButton(
                        onClick = { currentScreen = Screen.REMINDERS },
                        modifier = Modifier.testTag("nav_reminders_top_button")
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Reminders")
                    }

                    // Settings
                    IconButton(
                        onClick = { currentScreen = Screen.SETTINGS },
                        modifier = Modifier.testTag("nav_settings_top_button")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                val navItems = listOf(
                    Triple(Screen.HOME, Localization.getString("tab_home", lang), Icons.Default.Home),
                    Triple(Screen.TRANSACTIONS, Localization.getString("tab_transactions", lang), Icons.Default.Receipt),
                    Triple(Screen.BUDGETS, Localization.getString("tab_budgets", lang), Icons.Default.PieChart),
                    Triple(Screen.ACCA, Localization.getString("tab_acca", lang), Icons.Default.School),
                    Triple(Screen.REPORTS, Localization.getString("tab_reports", lang), Icons.Default.BarChart)
                )

                navItems.forEach { (screen, label, icon) ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(text = label, fontSize = 11.sp, fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { target ->
                when (target) {
                    Screen.HOME -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { currentScreen = Screen.TRANSACTIONS },
                        onNavigateToAcca = { currentScreen = Screen.ACCA },
                        onNavigateToBudgets = { currentScreen = Screen.BUDGETS },
                        onNavigateToAccounts = { currentScreen = Screen.ACCOUNTS },
                        onNavigateToSavings = { currentScreen = Screen.SAVINGS },
                        onNavigateToInsights = { currentScreen = Screen.INSIGHTS },
                        onQuickAddExpense = {
                            dialogInitialType = TransactionType.EXPENSE
                            editingTransaction = null
                            showAddTxDialog = true
                        },
                        onQuickAddIncome = {
                            dialogInitialType = TransactionType.INCOME
                            editingTransaction = null
                            showAddTxDialog = true
                        },
                        onQuickTransfer = {
                            dialogInitialType = TransactionType.TRANSFER
                            editingTransaction = null
                            showAddTxDialog = true
                        },
                        onTransactionClick = { tx ->
                            editingTransaction = tx
                            showAddTxDialog = true
                        }
                    )
                    Screen.TRANSACTIONS -> TransactionsScreen(
                        viewModel = viewModel,
                        onAddTransactionClick = {
                            dialogInitialType = TransactionType.EXPENSE
                            editingTransaction = null
                            showAddTxDialog = true
                        },
                        onEditTransactionClick = { tx ->
                            editingTransaction = tx
                            showAddTxDialog = true
                        }
                    )
                    Screen.BUDGETS -> BudgetsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                    Screen.ACCA -> AccaFeesScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                    Screen.REPORTS -> ReportsScreen(
                        viewModel = viewModel
                    )
                    Screen.DEBTS -> DebtsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                    Screen.ACCOUNTS -> AccountsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                    Screen.SAVINGS -> SavingsGoalsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                    Screen.INSIGHTS -> InsightsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                    Screen.REMINDERS -> RemindersScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                }
            }
        }
    }

    // Add / Edit Transaction Dialog
    if (showAddTxDialog) {
        AddTransactionDialog(
            initialTransaction = editingTransaction,
            initialType = dialogInitialType,
            accounts = accounts,
            accaFees = accaFees,
            savingsGoals = savingsGoals,
            onDismiss = {
                showAddTxDialog = false
                editingTransaction = null
            },
            onSave = { tx ->
                if (editingTransaction != null) {
                    viewModel.updateTransaction(editingTransaction!!, tx)
                } else {
                    viewModel.addTransaction(tx)
                }
                showAddTxDialog = false
                editingTransaction = null
            }
        )
    }
}
