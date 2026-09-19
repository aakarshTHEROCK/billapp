@file:OptIn(ExperimentalMaterial3Api::class)

package com.quickbill.app.ui.screens
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickbill.app.QuickBillApplication
import com.quickbill.app.data.Bill
import com.quickbill.app.data.BillItem
import com.quickbill.app.data.Money
import com.quickbill.app.data.Prefs
import com.quickbill.app.pdf.InvoicePdf
import com.quickbill.app.ui.components.LargeTextField
import com.quickbill.app.ui.components.PrimaryButton
import com.quickbill.app.ui.components.SecondaryButton
import com.quickbill.app.ui.components.WhatsAppButton
import com.quickbill.app.ui.theme.Background
import com.quickbill.app.ui.theme.Line
import com.quickbill.app.ui.theme.Surface
import com.quickbill.app.whatsapp.WhatsAppResult
import com.quickbill.app.whatsapp.WhatsAppShare
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PreviewScreen(billId: Long, onBack: () -> Unit, onDone: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as QuickBillApplication
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    var bill by remember { mutableStateOf<Bill?>(null) }
    var showWhatsAppDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(billId) {
        bill = app.database.billDao().getById(billId)
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Bill Preview", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        val currentBill = bill
        if (currentBill == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                item { InvoiceCard(currentBill, prefs.getBusinessDetails()) }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                if (toastMessage != null) {
                    Text(
                        toastMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                WhatsAppButton(text = "Share on WhatsApp", modifier = Modifier.padding(bottom = 10.dp)) {
                    showWhatsAppDialog = true
                }
                SecondaryButton(text = "Save / Share PDF", icon = Icons.Filled.Share) {
                    scope.launch {
                        try {
                            val file = InvoicePdf.generate(context, currentBill, prefs.getBusinessDetails())
                            if (!WhatsAppShare.shareGeneric(context, file)) {
                                toastMessage = "Could not create the PDF. Please try again."
                            }
                        } catch (e: Exception) {
                            toastMessage = "Could not create the PDF. Please try again."
                        }
                    }
                }
            }
        }
    }

    if (showWhatsAppDialog && bill != null) {
        WhatsAppNumberDialog(
            initialNumber = bill!!.phoneNumber,
            onDismiss = { showWhatsAppDialog = false },
            onSend = { number ->
                scope.launch {
                    try {
                        val file = InvoicePdf.generate(context, bill!!, prefs.getBusinessDetails())
                        when (WhatsAppShare.send(context, number, file)) {
                            is WhatsAppResult.Sent -> {
                                showWhatsAppDialog = false
                                toastMessage = null
                            }
                            is WhatsAppResult.InvalidNumber -> toastMessage = "Please enter a valid WhatsApp number."
                            is WhatsAppResult.NotInstalled -> {
                                showWhatsAppDialog = false
                                toastMessage = "WhatsApp is not installed on this phone."
                            }
                            is WhatsAppResult.Failed -> {
                                showWhatsAppDialog = false
                                toastMessage = "Could not open WhatsApp. Please try again."
                            }
                        }
                    } catch (e: Exception) {
                        toastMessage = "Could not create the PDF. Please try again."
                    }
                }
            }
        )
    }
}

@Composable
private fun WhatsAppNumberDialog(initialNumber: String, onDismiss: () -> Unit, onSend: (String) -> Unit) {
    var number by remember { mutableStateOf(initialNumber.filter { it.isDigit() }.takeLast(10)) }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send Bill on WhatsApp", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter customer's WhatsApp number.", fontSize = 16.sp)
                Spacer(Modifier.height(14.dp))
                LargeTextField(
                    value = number,
                    onValueChange = { number = it.filter { c -> c.isDigit() }.take(10); error = false },
                    label = "WhatsApp number",
                    prefix = "+91",
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                    isError = error,
                    supportingText = if (error) "Please enter a valid WhatsApp number." else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (number.length == 10) onSend(number) else error = true
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = com.quickbill.app.ui.theme.WhatsAppGreen
                )
            ) { Text("SEND ON WHATSAPP", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}

@Composable
private fun InvoiceCard(bill: Bill, business: com.quickbill.app.data.BusinessDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(business.shopName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (business.address.isNotBlank()) Text(business.address, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (business.phone.isNotBlank()) Text("Phone: ${business.phone}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            Text("INVOICE", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text("Invoice: ${bill.invoiceNumber}", fontSize = 13.sp)
            Text(
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(java.util.Date(bill.dateMillis)),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(6.dp))
            Text("Customer: ${bill.customerName.ifBlank { "Walk-in customer" }}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Line)
            Spacer(Modifier.height(10.dp))

            bill.items.forEachIndexed { index, item ->
                InvoiceItemRow(index + 1, item)
                Spacer(Modifier.height(10.dp))
            }

            HorizontalDivider(color = Line)
            Spacer(Modifier.height(12.dp))
            TotalsRow("Subtotal", Money.format(bill.subtotal))
            Spacer(Modifier.height(6.dp))
            TotalsRow("Total Discount", "- ${Money.format(bill.discountTotal)}")
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Line)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TOTAL", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    Money.format(bill.total),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Thank you for shopping with us!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun InvoiceItemRow(index: Int, item: BillItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$index. ${item.name}", fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(Money.format(item.finalAmount), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "${item.quantity} × ${Money.format(item.price)}   ·   ${item.discountPercent}% off",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TotalsRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}
