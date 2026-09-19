package com.quickbill.app.whatsapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

sealed class WhatsAppResult {
    object Sent : WhatsAppResult()
    object InvalidNumber : WhatsAppResult()
    object NotInstalled : WhatsAppResult()
    data class Failed(val message: String) : WhatsAppResult()
}

/**
 * QuickBill's WhatsApp handoff.
 *
 * Android/WhatsApp do not give a third-party app a way to both (a) target an
 * arbitrary, unsaved phone number AND (b) attach a specific file, in a single
 * intent that is guaranteed to work across every WhatsApp version - and no
 * app can make WhatsApp send a message without the user pressing Send
 * themselves. What we do instead, and what this class implements:
 *
 *  1. Normalize the entered number to a WhatsApp "jid" and try
 *     ACTION_SEND with the PDF attached, targeted at that jid via WhatsApp's
 *     own package. On current WhatsApp this opens the correct chat with the
 *     PDF already attached, ready for the user to press Send.
 *  2. If that is not resolvable on this device (older/newer WhatsApp
 *     builds, or WhatsApp Business), fall back to opening the exact chat for
 *     that number via the documented wa.me deep link, and separately hand
 *     the PDF to WhatsApp through a share intent scoped only to WhatsApp
 *     (never a generic Email/SMS chooser) so the user can attach and send it
 *     with one more tap inside WhatsApp.
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

        // Attempt 1: direct-to-chat with the PDF attached.
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra("jid", "$number@s.whatsapp.net")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage(installedPackage)
            }
            context.startActivity(intent)
            return WhatsAppResult.Sent
        } catch (e: ActivityNotFoundException) {
            // fall through to the fallback below
        } catch (e: Exception) {
            // fall through to the fallback below
        }

        // Fallback: open the exact chat, then hand off the PDF to WhatsApp only.
        return try {
            val chatIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number"))
            context.startActivity(chatIntent)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage(installedPackage)
            }
            context.startActivity(shareIntent)
            WhatsAppResult.Sent
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
