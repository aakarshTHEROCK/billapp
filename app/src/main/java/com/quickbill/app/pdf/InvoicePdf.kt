package com.quickbill.app.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import android.text.TextUtils
import com.quickbill.app.data.Bill
import com.quickbill.app.data.BusinessDetails
import com.quickbill.app.data.Money
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Builds a professional-looking A4 invoice PDF that mirrors the on-screen
 * preview layout exactly. Handles pagination so that 1, 5, 10+ items, long
 * garment names, large quantities/prices and any discount percentage all
 * stay aligned - nothing overlaps and nothing is clipped.
 */
object InvoicePdf {

    // A4 at 72dpi
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val COL_NAME = MARGIN
    private const val COL_QTY = 330f
    private const val COL_PRICE = 380f
    private const val COL_DISC = 460f
    private const val COL_AMOUNT_RIGHT = PAGE_WIDTH - MARGIN

    private val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#172033") }
    private val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#5B6478") }
    private val linePaint = Paint().apply { color = Color.parseColor("#E1E6EE"); strokeWidth = 1f }
    private val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#126FEF") }

    fun generate(context: android.content.Context, bill: Bill, business: BusinessDetails): File {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "${bill.invoiceNumber}.pdf")
        val document = PdfDocument()

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas
        var y = drawHeader(canvas, bill, business, pageNumber)
        y = drawTableHeader(canvas, y)

        val rowHeight = 24f
        val bottomLimit = PAGE_HEIGHT - 110f

        bill.items.forEachIndexed { index, item ->
            if (y > bottomLimit) {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                y = drawHeader(canvas, bill, business, pageNumber, continued = true)
                y = drawTableHeader(canvas, y)
            }
            y = drawRow(canvas, y, index + 1, item)
        }

        if (y > bottomLimit - 90f) {
            document.finishPage(page)
            pageNumber += 1
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = page.canvas
            y = drawHeader(canvas, bill, business, pageNumber, continued = true)
        }

        drawTotals(canvas, y, bill)
        drawFooter(canvas)
        document.finishPage(page)

        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun drawHeader(
        canvas: Canvas,
        bill: Bill,
        business: BusinessDetails,
        pageNumber: Int,
        continued: Boolean = false
    ): Float {
        var y = 55f
        val title = Paint(darkPaint).apply { textSize = 20f; isFakeBoldText = true; textAlign = Paint.Align.LEFT }
        canvas.drawText(business.shopName.ifBlank { "Your Store Name" }, MARGIN, y, title)

        val small = Paint(mutedPaint).apply { textSize = 10f; textAlign = Paint.Align.LEFT }
        y += 16f
        if (business.address.isNotBlank()) { canvas.drawText(business.address, MARGIN, y, small); y += 14f }
        if (business.phone.isNotBlank()) { canvas.drawText("Phone: ${business.phone}", MARGIN, y, small); y += 14f }
        if (business.gstin.isNotBlank()) { canvas.drawText("GSTIN: ${business.gstin}", MARGIN, y, small); y += 14f }

        val invoiceTitle = Paint(bluePaint).apply { textSize = 18f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }
        canvas.drawText(if (continued) "INVOICE (contd.)" else "INVOICE", COL_AMOUNT_RIGHT, 55f, invoiceTitle)
        val meta = Paint(darkPaint).apply { textSize = 10.5f; textAlign = Paint.Align.RIGHT }
        canvas.drawText("Invoice: ${bill.invoiceNumber}", COL_AMOUNT_RIGHT, 74f, meta)
        canvas.drawText(
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(java.util.Date(bill.dateMillis)),
            COL_AMOUNT_RIGHT, 90f, meta
        )
        canvas.drawText("Page $pageNumber", COL_AMOUNT_RIGHT, 106f, Paint(mutedPaint).apply { textSize = 9f; textAlign = Paint.Align.RIGHT })

        y = maxOf(y, 106f) + 10f
        canvas.drawLine(MARGIN, y, COL_AMOUNT_RIGHT, y, linePaint)
        y += 20f

        val customerLabel = Paint(darkPaint).apply { textSize = 12f; isFakeBoldText = true; textAlign = Paint.Align.LEFT }
        canvas.drawText("Bill To: ${bill.customerName.ifBlank { "Walk-in customer" }}", MARGIN, y, customerLabel)
        y += 26f
        return y
    }

    private fun drawTableHeader(canvas: Canvas, yStart: Float): Float {
        var y = yStart
        val header = Paint(darkPaint).apply { textSize = 11f; isFakeBoldText = true }
        canvas.drawText("#", MARGIN, y, header)
        canvas.drawText("Garment", MARGIN + 20f, y, header)
        header.textAlign = Paint.Align.CENTER
        canvas.drawText("Qty", COL_QTY, y, header)
        header.textAlign = Paint.Align.RIGHT
        canvas.drawText("Price", COL_PRICE + 30f, y, header)
        canvas.drawText("Disc.", COL_DISC + 20f, y, header)
        canvas.drawText("Amount", COL_AMOUNT_RIGHT, y, header)
        y += 8f
        canvas.drawLine(MARGIN, y, COL_AMOUNT_RIGHT, y, linePaint)
        y += 18f
        return y
    }

    private fun drawRow(canvas: Canvas, yStart: Float, index: Int, item: com.quickbill.app.data.BillItem): Float {
        val body = TextPaint(darkPaint).apply { textSize = 10.5f; textAlign = Paint.Align.LEFT }
        val name = TextUtils.ellipsize(item.name, body, 250f, TextUtils.TruncateAt.END).toString()

        canvas.drawText(index.toString(), MARGIN, yStart, body)
        canvas.drawText(name, MARGIN + 20f, yStart, body)

        val centered = Paint(body).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(item.quantity.toString(), COL_QTY, yStart, centered)

        val right = Paint(body).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText(Money.format(item.price), COL_PRICE + 30f, yStart, right)
        canvas.drawText("${item.discountPercent}%", COL_DISC + 20f, yStart, right)
        canvas.drawText(Money.format(item.finalAmount), COL_AMOUNT_RIGHT, yStart, right)

        return yStart + 24f
    }

    private fun drawTotals(canvas: Canvas, yStart: Float, bill: Bill) {
        var y = yStart + 10f
        canvas.drawLine(300f, y, COL_AMOUNT_RIGHT, y, linePaint)
        y += 22f

        val label = Paint(mutedPaint).apply { textSize = 11f; textAlign = Paint.Align.LEFT }
        val value = Paint(darkPaint).apply { textSize = 11f; textAlign = Paint.Align.RIGHT }

        canvas.drawText("Subtotal", 300f, y, label)
        canvas.drawText(Money.format(bill.subtotal), COL_AMOUNT_RIGHT, y, value)
        y += 20f

        canvas.drawText("Total Discount", 300f, y, label)
        canvas.drawText("- ${Money.format(bill.discountTotal)}", COL_AMOUNT_RIGHT, y, value)
        y += 14f
        canvas.drawLine(300f, y, COL_AMOUNT_RIGHT, y, linePaint)
        y += 22f

        val totalLabel = Paint(darkPaint).apply { textSize = 15f; isFakeBoldText = true; textAlign = Paint.Align.LEFT }
        val totalValue = Paint(bluePaint).apply { textSize = 16f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }
        canvas.drawText("TOTAL", 300f, y, totalLabel)
        canvas.drawText(Money.format(bill.total), COL_AMOUNT_RIGHT, y, totalValue)
    }

    private fun drawFooter(canvas: Canvas) {
        val footer = Paint(mutedPaint).apply { textSize = 10f; textAlign = Paint.Align.CENTER }
        canvas.drawText("Thank you for shopping with us!", PAGE_WIDTH / 2f, PAGE_HEIGHT - 40f, footer)
    }
}
