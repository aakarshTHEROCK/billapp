package com.quickbill.app.whatsapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

sealed class WhatsAppResult {
    /** WhatsApp opened the right chat; the PDF was handed to WhatsApp for the user to send. */
    object ChatOpened : WhatsAppResult()
    object InvalidNumber : WhatsAppResult()
    object NotInstalled : WhatsAppResult()
    data class Failed(val message: String) : WhatsAppResult()
}

/**
 * QuickBill's WhatsApp handoff.
 *
 * The real constraint, stated plainly: no third-party app can open a specific
 * WhatsApp chat for an arbitrary, unsaved number AND have a file already
 * attached, in one step that WhatsApp is guaranteed to honor - and no app can
 * make WhatsApp send a message without the user pressing Send themselves.
 *
 * An earlier build tried an undocumented "jid" extra on ACTION_SEND to do
 * both at once. On many WhatsApp versions that silently fails, and WhatsApp
 * falls back to showing its own full contact list instead of the intended
 * chat - which is exactly the confusing behaviour reported for numbers that
 * aren't saved as contacts.
 *
 * This version uses WhatsApp's own documented "click to chat" link
 * (api.whatsapp.com/send?phone=...) instead, which reliably opens the exact
 * chat for any number, saved or not - WhatsApp creates the chat if it
 * doesn't exist yet. Right after that, the PDF is handed to WhatsApp only
 * (never a generic Email/SMS chooser). Because the chat just opened is now
 * WhatsApp's most recently used conversation, it appears as the top option
 * in the short "send to" list WhatsApp shows - so the user taps the top
 * item, then Send, inside WhatsApp. That one extra tap is an Android/WhatsApp
 * limitation, not something QuickBill can skip.
 */
object WhatsAppShare {

    private const val WHATSAPP_PACKAGE = "com.whatsapp"
    private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"

    /** Returns a clean E.164-style number (country code + digits, no symbols) or null if invalid. */
    fun normalizeNumber(rawInput: String): String? {
        val digits = rawInput.filter { it.isDigit() }
        return when {
            digits.length == 10 -> "91$digits"
            digits.length == 12 && digits.startsWith("91") -> digits
            digits.length in 11..15 -> digits
            else -> null
        }
    }

    fun send(context: Context, rawNumber: String, pdfFile: File): WhatsAppResult {
        val number = normalizeNumber(rawNumber) ?: return WhatsAppResult.InvalidNumber

        val installedPackage = when {
            isInstalled(context, WHATSAPP_PACKAGE) -> WHATSAPP_PACKAGE
            isInstalled(context, WHATSAPP_BUSINESS_PACKAGE) -> WHATSAPP_BUSINESS_PACKAGE
            else -> return WhatsAppResult.NotInstalled
        }

        val uri: Uri = try {
            FileProvider.getUriForFile(context, "com.quickbill.app.fileprovider", pdfFile)
        } catch (e: Exception) {
            return WhatsAppResult.Failed("Could not attach the invoice PDF.")
        }

        return try {
            // Step 1: open the exact chat for this number - works for saved
            // and unsaved numbers alike, and never shows a contact list.
            val chatIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://api.whatsapp.com/send?phone=$number")
            ).apply {
                setPackage(installedPackage)
            }
            context.startActivity(chatIntent)

            // Step 2: hand the PDF to WhatsApp only. The chat opened above is
            // now WhatsApp's most recent conversation, so it appears first in
            // the short list WhatsApp shows here.
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage(installedPackage)
            }
            context.startActivity(shareIntent)
            WhatsAppResult.ChatOpened
        } catch (e: ActivityNotFoundException) {
            WhatsAppResult.NotInstalled
        } catch (e: Exception) {
            WhatsAppResult.Failed("Could not open WhatsApp. Please try again.")
        }
    }

    fun shareGeneric(context: Context, pdfFile: File): Boolean {
        return try {
            val uri = FileProvider.getUriForFile(context, "com.quickbill.app.fileprovider", pdfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share invoice PDF"))
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
