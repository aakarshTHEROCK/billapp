package com.quickbill.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.json.JSONArray
import org.json.JSONObject

/** One garment/item line inside a bill. */
data class BillItem(
    val name: String,
    val quantity: Int,
    val price: Double,
    val discountPercent: Int
) {
    val originalAmount: Double get() = quantity * price
    val discountAmount: Double get() = originalAmount * discountPercent / 100.0
    val finalAmount: Double get() = originalAmount - discountAmount
}

class BillItemListConverter {
    @TypeConverter
    fun fromList(items: List<BillItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("quantity", item.quantity)
            obj.put("price", item.price)
            obj.put("discountPercent", item.discountPercent)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toList(raw: String): List<BillItem> {
        if (raw.isBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                BillItem(
                    name = obj.optString("name", "Garment"),
                    quantity = obj.optInt("quantity", 1),
                    price = obj.optDouble("price", 0.0),
                    discountPercent = obj.optInt("discountPercent", 0)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@Entity(tableName = "bills")
@TypeConverters(BillItemListConverter::class)
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val dateMillis: Long,
    val customerName: String,
    val phoneNumber: String,
    val items: List<BillItem>,
    val subtotal: Double,
    val discountTotal: Double,
    val total: Double
)
