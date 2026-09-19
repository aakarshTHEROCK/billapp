package com.quickbill.app.data

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * All currency math and formatting for QuickBill lives here so that "no NaN,
 * no undefined, no broken formatting" only has to be guaranteed in one place.
 */
object Money {

    private val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' }

    // Indian digit grouping (e.g. 1,25,000) built by hand rather than relying on
    // the platform's en-IN locale data, which is not guaranteed identical across
    // every Android build.
    private fun groupIndian(intPart: String): String {
        if (intPart.length <= 3) return intPart
        val last3 = intPart.substring(intPart.length - 3)
        var remaining = intPart.substring(0, intPart.length - 3)
        val sb = StringBuilder()
        while (remaining.length > 2) {
            sb.insert(0, "," + remaining.substring(remaining.length - 2))
            remaining = remaining.substring(0, remaining.length - 2)
        }
        if (remaining.isNotEmpty()) sb.insert(0, remaining)
        return sb.toString() + "," + last3
    }

    /** Safe parse: blank or unparsable text is treated as zero, never NaN. */
    fun parseAmount(text: String?): Double {
        if (text.isNullOrBlank()) return 0.0
        val cleaned = text.trim().replace(",", "")
        val value = cleaned.toDoubleOrNull() ?: return 0.0
        return if (value.isFinite() && value >= 0.0) value else 0.0
    }

    fun parseQuantity(text: String?): Int {
        if (text.isNullOrBlank()) return 1
        val value = text.trim().toIntOrNull() ?: return 1
        return if (value > 0) value else 1
    }

    /** Formats a rupee amount as ₹1,250 or ₹1,250.50 - never NaN/undefined/null. */
    fun format(amount: Double): String {
        val safe = if (amount.isFinite()) amount else 0.0
        val rounded = Math.round(safe * 100) / 100.0
        val isWhole = rounded == Math.floor(rounded)
        val df = if (isWhole) DecimalFormat("0", symbols) else DecimalFormat("0.00", symbols)
        val formatted = df.format(rounded)
        val parts = formatted.split(".")
        val grouped = groupIndian(parts[0])
        return if (parts.size > 1) "₹$grouped.${parts[1]}" else "₹$grouped"
    }
}
