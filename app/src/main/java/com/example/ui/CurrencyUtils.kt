package com.example.ui

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    /**
     * Formats any amount to Indian Rupees (INR) with لاکھ/کروڑ (lakh/crore) grouping
     * and the '₹' symbol, showing 2 decimal places.
     * Example: 150000.00 -> ₹1,50,000.00
     */
    fun formatInr(amount: Double): String {
        return try {
            val locale = Locale("en", "IN")
            val formatter = NumberFormat.getCurrencyInstance(locale)
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            var result = formatter.format(amount)
            
            // Replace various localized currency symbols/names with ₹
            result = result.replace("INR", "₹")
            result = result.replace("Rs.", "₹")
            result = result.replace("Rs", "₹")
            result = result.replace("RS", "₹")
            result = result.replace("\\s".toRegex(), "") // strip any spaces between symbol and numbers
            
            if (!result.contains("₹")) {
                result = "₹$result"
            }
            result
        } catch (e: Exception) {
            // Fallback manually using US formatting but prefixing ₹ if Locale IN fails
            val formattedVal = String.format(Locale.US, "%,.2f", amount)
            "₹$formattedVal"
        }
    }

    /**
     * Formats any amount to Indian Rupees (INR) without writing the decimal decimals if not desired.
     * Example: 150000 -> ₹1,50,000
     */
    fun formatInrNoDecimal(amount: Double): String {
        return try {
            val locale = Locale("en", "IN")
            val formatter = NumberFormat.getCurrencyInstance(locale)
            formatter.maximumFractionDigits = 0
            var result = formatter.format(amount)
            
            result = result.replace("INR", "₹")
            result = result.replace("Rs.", "₹")
            result = result.replace("Rs", "₹")
            result = result.replace("RS", "₹")
            result = result.replace("\\s".toRegex(), "")
            
            if (!result.contains("₹")) {
                result = "₹$result"
            }
            result
        } catch (e: Exception) {
            val formattedVal = String.format(Locale.US, "%,.0f", amount)
            "₹$formattedVal"
        }
    }
}
