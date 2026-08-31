package com.siraj.app.core.utils

import java.text.NumberFormat
import java.util.Locale
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

object LocalizationUtils {
    fun formatNumber(number: Number): String {
        val format = NumberFormat.getInstance(Locale.getDefault())
        return format.format(number)
    }

    fun formatCurrency(
        amount: Double,
        currencyCode: String = "USD",
    ): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        format.currency = java.util.Currency.getInstance(currencyCode)
        return format.format(amount)
    }

    fun formatDate(
        temporal: TemporalAccessor,
        pattern: String,
    ): String {
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        return formatter.format(temporal)
    }
}
