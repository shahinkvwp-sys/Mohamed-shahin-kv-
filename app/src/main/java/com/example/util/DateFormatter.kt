package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val displayTimeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    private val displayDateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
    private val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.ENGLISH)
    private val monthNameFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.ENGLISH)

    fun formatDate(timestamp: Long): String {
        return displayDateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return displayTimeFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return displayDateTimeFormat.format(Date(timestamp))
    }

    fun getMonthKey(timestamp: Long = System.currentTimeMillis()): String {
        return monthKeyFormat.format(Date(timestamp))
    }

    fun getMonthName(timestamp: Long = System.currentTimeMillis()): String {
        return monthNameFormat.format(Date(timestamp))
    }

    fun getDayOfWeek(timestamp: Long): String {
        return dayOfWeekFormat.format(Date(timestamp))
    }

    fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartToday(): Long = getStartOfToday()

    fun getStartOfMonth(monthOffset: Int = 0): Long {
        val cal = Calendar.getInstance()
        if (monthOffset != 0) {
            cal.add(Calendar.MONTH, monthOffset)
        }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfMonth(monthOffset: Int = 0): Long {
        val cal = Calendar.getInstance()
        if (monthOffset != 0) {
            cal.add(Calendar.MONTH, monthOffset)
        }
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getDaysAgo(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
