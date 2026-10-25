package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryLight
import com.example.ui.theme.TransferBlue
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ReportPeriod
import com.example.util.CurrencyFormatter
import com.example.util.DateFormatter
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val summary by viewModel.dashboardSummary.collectAsState()
    val reportPeriod by viewModel.reportPeriod.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val userPref by viewModel.userPreferences.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Calculate time range based on reportPeriod
    val now = System.currentTimeMillis()
    val rangeStart = when (reportPeriod) {
        ReportPeriod.THIS_WEEK -> DateFormatter.getDaysAgo(7)
        ReportPeriod.THIS_MONTH -> DateFormatter.getStartOfMonth()
        ReportPeriod.THIS_YEAR -> {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.timeInMillis
        }
        ReportPeriod.ALL_TIME -> 0L
    }

    val periodTransactions = transactions.filter { it.timestamp >= rangeStart }
    val totalIncome = periodTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = periodTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val netCashFlow = totalIncome - totalExpense

    val categorySpending = periodTransactions.filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        .toList()
        .sortedByDescending { it.second }

    // Daily spending data for chart (last 7 days)
    val dailyBuckets = (6 downTo 0).map { daysAgo ->
        val dayStart = DateFormatter.getDaysAgo(daysAgo)
        val dayEnd = dayStart + 24L * 60 * 60 * 1000 - 1
        val amount = transactions.filter { it.type == TransactionType.EXPENSE && it.timestamp in dayStart..dayEnd }
            .sumOf { it.amount }
        val label = DateFormatter.getDayOfWeek(dayStart)
        label to amount
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period Selector: Week | Month | Year | All
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportPeriod.values().forEach { period ->
                    val label = when (period) {
                        ReportPeriod.THIS_WEEK -> "This Week"
                        ReportPeriod.THIS_MONTH -> "This Month"
                        ReportPeriod.THIS_YEAR -> "This Year"
                        ReportPeriod.ALL_TIME -> "All Time"
                    }
                    FilterChip(
                        selected = reportPeriod == period,
                        onClick = { viewModel.setReportPeriod(period) },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Cash Flow Comparison Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().testTag("cash_flow_report_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Cash Flow Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = CurrencyFormatter.formatInr(totalIncome),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = IncomeGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = CurrencyFormatter.formatInr(totalExpense),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExpenseRed
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val flowLabel = if (userPref.businessMode) "Business Profit" else "Net Cash Flow"
                            Text(text = flowLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = CurrencyFormatter.formatInr(netCashFlow),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (netCashFlow >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        // Daily Spending Interactive Bar Chart
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().testTag("daily_spending_chart_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Daily Spending Trend (Last 7 Days)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    DailySpendingBarChart(
                        dailyData = dailyBuckets,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }
        }

        // Category-wise Breakdown with Percentages
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Category Spending Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (categorySpending.isEmpty()) {
                        Text(
                            text = "No expenses recorded in this period.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        categorySpending.take(8).forEach { (category, amt) ->
                            val pct = if (totalExpense > 0) (amt / totalExpense).toFloat() else 0f
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${CurrencyFormatter.formatInr(amt)} (${(pct * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = TealPrimary,
                                    trackColor = TealPrimary.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Export and Share Statement Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val csvData = viewModel.repository.exportToCsv()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csvData)
                                type = "text/csv"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Transactions CSV"))
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    modifier = Modifier.weight(1f).testTag("export_csv_button")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val summaryText = viewModel.repository.generateMonthlySummaryText()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, summaryText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Monthly Statement"))
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("share_summary_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Monthly Summary", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DailySpendingBarChart(
    dailyData: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val maxSpend = dailyData.maxOfOrNull { it.second }?.coerceAtLeast(100.0) ?: 100.0
    val tealColor = TealPrimary
    val emptyColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val barCount = dailyData.size
            val spaceBetween = 16.dp.toPx()
            val totalSpacing = spaceBetween * (barCount + 1)
            val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(12.dp.toPx())

            dailyData.forEachIndexed { index, (_, amount) ->
                val x = spaceBetween + index * (barWidth + spaceBetween)
                val barHeight = ((amount / maxSpend) * size.height).toFloat().coerceIn(4f, size.height)
                val y = size.height - barHeight

                // Draw background bar track
                drawRoundRect(
                    color = emptyColor,
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Draw active spend bar
                if (amount > 0) {
                    drawRoundRect(
                        color = tealColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dailyData.forEach { (label, amount) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatCompact(amount),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = if (amount > 0) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
