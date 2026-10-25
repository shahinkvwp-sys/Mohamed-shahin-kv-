package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.util.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserPreferencesState(
    val userName: String = "Shahin",
    val studentStage: String = "Applied Skills Level",
    val monthlyBudget: Double = 12000.0,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val darkTheme: Boolean = true,
    val businessMode: Boolean = false,
    val pinLockEnabled: Boolean = false,
    val userPin: String = "",
    val customCategories: Set<String> = emptySet(),
    val isDemoLoaded: Boolean = false
)

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("acca_money_mate_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadFromPrefs())
    val state: StateFlow<UserPreferencesState> = _state.asStateFlow()

    private fun loadFromPrefs(): UserPreferencesState {
        val langStr = prefs.getString("language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name
        val language = runCatching { AppLanguage.valueOf(langStr) }.getOrDefault(AppLanguage.ENGLISH)
        return UserPreferencesState(
            userName = prefs.getString("user_name", "Shahin") ?: "Shahin",
            studentStage = prefs.getString("student_stage", "Applied Skills Level") ?: "Applied Skills Level",
            monthlyBudget = prefs.getFloat("monthly_budget", 12000.0f).toDouble(),
            language = language,
            darkTheme = prefs.getBoolean("dark_theme", true),
            businessMode = prefs.getBoolean("business_mode", false),
            pinLockEnabled = prefs.getBoolean("pin_lock_enabled", false),
            userPin = prefs.getString("user_pin", "") ?: "",
            customCategories = prefs.getStringSet("custom_categories", emptySet()) ?: emptySet(),
            isDemoLoaded = prefs.getBoolean("is_demo_loaded", false)
        )
    }

    fun updateUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
        _state.value = _state.value.copy(userName = name)
    }

    fun updateStudentStage(stage: String) {
        prefs.edit().putString("student_stage", stage).apply()
        _state.value = _state.value.copy(studentStage = stage)
    }

    fun updateMonthlyBudget(budget: Double) {
        prefs.edit().putFloat("monthly_budget", budget.toFloat()).apply()
        _state.value = _state.value.copy(monthlyBudget = budget)
    }

    fun updateLanguage(language: AppLanguage) {
        prefs.edit().putString("language", language.name).apply()
        _state.value = _state.value.copy(language = language)
    }

    fun updateDarkTheme(dark: Boolean) {
        prefs.edit().putBoolean("dark_theme", dark).apply()
        _state.value = _state.value.copy(darkTheme = dark)
    }

    fun updateBusinessMode(enabled: Boolean) {
        prefs.edit().putBoolean("business_mode", enabled).apply()
        _state.value = _state.value.copy(businessMode = enabled)
    }

    fun setPin(pin: String, enabled: Boolean) {
        prefs.edit().putString("user_pin", pin).putBoolean("pin_lock_enabled", enabled).apply()
        _state.value = _state.value.copy(userPin = pin, pinLockEnabled = enabled)
    }

    fun addCustomCategory(category: String) {
        val current = _state.value.customCategories.toMutableSet()
        current.add(category)
        prefs.edit().putStringSet("custom_categories", current).apply()
        _state.value = _state.value.copy(customCategories = current)
    }

    fun setDemoLoaded(loaded: Boolean) {
        prefs.edit().putBoolean("is_demo_loaded", loaded).apply()
        _state.value = _state.value.copy(isDemoLoaded = loaded)
    }

    fun clearAllPrefs() {
        prefs.edit().clear().apply()
        _state.value = UserPreferencesState()
    }
}
