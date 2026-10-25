package com.example.util

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    MALAYALAM("ml", "മലയാളം (Malayalam)")
}

object Localization {
    private val englishMap = mapOf(
        "app_title" to "My Money Mate",
        "app_subtitle" to "ACCA Student Edition • Kerala",
        "tab_home" to "Home",
        "tab_transactions" to "Transactions",
        "tab_budgets" to "Budgets",
        "tab_acca" to "ACCA Fees",
        "tab_reports" to "Reports",
        "total_available" to "Total Available Money",
        "cash_in_hand" to "Cash in Hand",
        "bank_balance" to "Bank Balance",
        "upi_digital" to "UPI / Digital Balance",
        "income_this_month" to "Income This Month",
        "expenses_today" to "Expenses Today",
        "expenses_this_month" to "Expenses This Month",
        "total_savings" to "Total Savings",
        "remaining_budget" to "Remaining Budget",
        "upcoming_payments" to "Upcoming Payments",
        "acca_progress" to "ACCA Fees Progress",
        "fees_paid" to "Fees Paid",
        "fees_remaining" to "Fees Remaining",
        "quick_add" to "Quick Add",
        "add_expense" to "Add Expense",
        "add_income" to "Add Income",
        "transfer_money" to "Transfer Money",
        "add_goal" to "Add Goal",
        "recent_transactions" to "Recent Transactions",
        "view_all" to "View All",
        "no_transactions" to "No transactions recorded yet. Tap + to add one!",
        "net_cash_flow" to "Net Cash Flow",
        "business_profit" to "Business Profit",
        "savings_contribution" to "Savings Contribution",
        "monthly_surplus" to "Monthly Surplus",
        "monthly_deficit" to "Monthly Deficit",
        "budget_warning_80" to "Warning: Budget 80% reached!",
        "budget_exceeded" to "Alert: Budget exceeded!",
        "carry_forward" to "Carry Forward Budget",
        "export_csv" to "Export CSV",
        "generate_summary" to "Monthly Statement",
        "demo_mode" to "Demo Mode",
        "load_sample" to "Load Sample Student Data",
        "clear_data" to "Clear All Data",
        "settings" to "Settings",
        "insights" to "Financial Insights",
        "reminders" to "Reminders",
        "accounts" to "Accounts"
    )

    private val malayalamMap = mapOf(
        "app_title" to "മൈ മണി മേറ്റ്",
        "app_subtitle" to "ACCA സ്റ്റുഡന്റ് എഡിഷൻ • കേരളം",
        "tab_home" to "ഹോം",
        "tab_transactions" to "ഇടപാടുകൾ",
        "tab_budgets" to "ബജറ്റ്",
        "tab_acca" to "ACCA ഫീസ്",
        "tab_reports" to "റിപ്പോർട്ടുകൾ",
        "total_available" to "ആകെ ലഭ്യമായ പണം",
        "cash_in_hand" to "കൈവശമുള്ള പണം (Cash)",
        "bank_balance" to "ബാങ്ക് ബാലൻസ്",
        "upi_digital" to "UPI / ഡിജിറ്റൽ ബാലൻസ്",
        "income_this_month" to "ഈ മാസത്തെ വരുമാനം",
        "expenses_today" to "ഇന്നത്തെ ചെലവ്",
        "expenses_this_month" to "ഈ മാസത്തെ ആകെ ചെലവ്",
        "total_savings" to "ആകെ സമ്പാദ്യം",
        "remaining_budget" to "ബാക്കി ബജറ്റ്",
        "upcoming_payments" to "വരാനിരിക്കുന്ന പേയ്‌മെന്റുകൾ",
        "acca_progress" to "ACCA ഫീസ് പുരോഗതി",
        "fees_paid" to "അടച്ച ഫീസ്",
        "fees_remaining" to "ബാക്കി അടയ്ക്കാനുള്ളത്",
        "quick_add" to "വേഗത്തിൽ ചേർക്കുക",
        "add_expense" to "ചെലവ് ചേർക്കുക",
        "add_income" to "വരുമാനം ചേർക്കുക",
        "transfer_money" to "പണം കൈമാറ്റം",
        "add_goal" to "ലക്ഷ്യം ചേർക്കുക",
        "recent_transactions" to "അടുത്തിടെയുള്ള ഇടപാടുകൾ",
        "view_all" to "എല്ലാം കാണുക",
        "no_transactions" to "ഇടപാടുകളൊന്നും രേഖപ്പെടുത്തിയിട്ടില്ല. + ബട്ടൺ അമർത്തുക!",
        "net_cash_flow" to "നെറ്റ് ക്യാഷ് ഫ്ലോ",
        "business_profit" to "ബിസിനസ് ലാഭം",
        "savings_contribution" to "സമ്പാദ്യത്തിലേക്ക് മാറ്റിയത്",
        "monthly_surplus" to "ഈ മാസത്തെ മിച്ചം",
        "monthly_deficit" to "ഈ മാസത്തെ കമ്മി",
        "budget_warning_80" to "ശ്രദ്ധിക്കുക: ബജറ്റ് 80% കഴിഞ്ഞു!",
        "budget_exceeded" to "മുന്നറിയിപ്പ്: ബജറ്റ് പരിധി കഴിഞ്ഞു!",
        "carry_forward" to "ബാക്കി തുക അടുത്ത മാസത്തേക്ക് മാറ്റുക",
        "export_csv" to "CSV എക്സ്പോർട്ട്",
        "generate_summary" to "പ്രതിമാസ സ്റ്റേറ്റ്മെന്റ്",
        "demo_mode" to "ഡെമോ മോഡ്",
        "load_sample" to "മാതൃകാ ഡാറ്റ ചേർക്കുക",
        "clear_data" to "എല്ലാ വിവരങ്ങളും മായ്ക്കുക",
        "settings" to "ക്രമീകരണങ്ങൾ",
        "insights" to "ധനകാര്യ വിവരങ്ങൾ",
        "reminders" to "ഓർമ്മപ്പെടുത്തലുകൾ",
        "accounts" to "അക്കൗണ്ടുകൾ"
    )

    fun getString(key: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.MALAYALAM -> malayalamMap[key] ?: englishMap[key] ?: key
            AppLanguage.ENGLISH -> englishMap[key] ?: key
        }
    }
}
