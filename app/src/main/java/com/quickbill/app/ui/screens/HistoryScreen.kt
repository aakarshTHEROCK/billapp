@file:OptIn(ExperimentalMaterial3Api::class)

package com.quickbill.app.ui.screens
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickbill.app.QuickBillApplication
import com.quickbill.app.data.Bill
import com.quickbill.app.data.Money
import com.quickbill.app.ui.theme.Background
import com.quickbill.app.ui.theme.Surface
import com.quickbill.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(onBack: () -> Unit, onOpenBill: (Long) -> Unit) {
    val app = LocalContext.current.applicationContext as QuickBillApplication
    val bills by app.database.billDao().observeAll().collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Bill History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        if (bills.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No bills yet.\nBills you generate will appear here.",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bills, key = { it.id }) { bill -> HistoryRow(bill) { onOpenBill(bill.id) } }
        }
    }
}

@Composable
private fun HistoryRow(bill: Bill, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(bill.invoiceNumber, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            Text(
                bill.customerName.ifBlank { "Walk-in customer" },
                color = TextSecondary,
                fontSize = 14.sp
            )
            Text(
                SimpleDateFormat("dd MMM yyyy", Locale.US).format(java.util.Date(bill.dateMillis)),
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
        Text(Money.format(bill.total), fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
