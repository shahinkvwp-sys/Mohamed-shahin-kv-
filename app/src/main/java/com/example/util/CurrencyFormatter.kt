package com.example.util

import java.util.Locale

object CurrencyFormatter {

    /**
     * Formats an amount with the Indian Rupee symbol (₹) and Indian numbering system (Lakhs/Crores).
     * Example: 150000 -> "₹1,50,000"
     */
    fun formatInr(amount: Double, showDecimals: Boolean = false): String {
        val safeAmount = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        val isNeg = safeAmount < 0
        val absAmt = kotlin.math.abs(safeAmount)
        val integerPart = absAmt.toLong()
        val decimalPart = String.format(Locale.ENGLISH, "%.2f", absAmt - integerPart).substringAfter(".")

        val str = integerPart.toString()
        val formatted = if (str.length <= 3) {
            str
        } else {
            val last3 = str.takeLast(3)
            val remaining = str.dropLast(3)
            val groupedRemaining = remaining.reversed().chunked(2).joinToString(",").reversed()
            "$groupedRemaining,$last3"
        }

        val result = if (showDecimals || (absAmt % 1.0 != 0.0 && decimalPart != "00")) {
            "$formatted.$decimalPart"
        } else {
            formatted
        }

        return if (isNeg) "-₹$result" else "₹$result"
    }

    /**
     * Compact Indian Rupee format for small card badges (e.g. ₹1.5L, ₹45K)
     */
    fun formatCompact(amount: Double): String {
        val safeAmount = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        return when {
            kotlin.math.abs(safeAmount) >= 1_00_00_000 -> String.format(Locale.ENGLISH, "₹%.1fCr", safeAmount / 1_00_00_000.0)
            kotlin.math.abs(safeAmount) >= 1_00_000 -> String.format(Locale.ENGLISH, "₹%.1fL", safeAmount / 1_00_000.0)
            kotlin.math.abs(safeAmount) >= 1_000 -> String.format(Locale.ENGLISH, "₹%.1fK", safeAmount / 1_000.0)
            else -> formatInr(safeAmount)
        }
    }
}
