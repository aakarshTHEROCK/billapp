package com.quickbill.app.data

import android.content.Context

data class BusinessDetails(
    val shopName: String = "Your Store Name",
    val address: String = "",
    val phone: String = "",
    val gstin: String = "",
    val logoPath: String? = null
)

/**
 * Tiny, dependency-free settings store. This intentionally is NOT a full
 * "Settings" screen - per the product spec, business details are the only
 * configuration QuickBill has, reached from a small secondary action rather
 * than a main navigation item.
 */
class Prefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("quickbill_prefs", Context.MODE_PRIVATE)

    fun getBusinessDetails(): BusinessDetails = BusinessDetails(
        shopName = sp.getString(KEY_SHOP_NAME, "Your Store Name") ?: "Your Store Name",
        address = sp.getString(KEY_ADDRESS, "") ?: "",
        phone = sp.getString(KEY_PHONE, "") ?: "",
        gstin = sp.getString(KEY_GSTIN, "") ?: "",
        logoPath = sp.getString(KEY_LOGO, null)
    )

    fun saveBusinessDetails(details: BusinessDetails) {
        sp.edit()
            .putString(KEY_SHOP_NAME, details.shopName.ifBlank { "Your Store Name" })
            .putString(KEY_ADDRESS, details.address)
            .putString(KEY_PHONE, details.phone)
            .putString(KEY_GSTIN, details.gstin)
            .putString(KEY_LOGO, details.logoPath)
            .apply()
    }

    /** Returns the next invoice number, e.g. INV-00001, and persists the counter. */
    fun nextInvoiceNumber(): String {
        val next = sp.getInt(KEY_COUNTER, 0) + 1
        sp.edit().putInt(KEY_COUNTER, next).apply()
        return "INV-" + next.toString().padStart(5, '0')
    }

    companion object {
        private const val KEY_SHOP_NAME = "shop_name"
        private const val KEY_ADDRESS = "address"
        private const val KEY_PHONE = "phone"
        private const val KEY_GSTIN = "gstin"
        private const val KEY_LOGO = "logo_path"
        private const val KEY_COUNTER = "invoice_counter"
    }
}
