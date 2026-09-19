@file:OptIn(ExperimentalMaterial3Api::class)

package com.quickbill.app.ui.screens
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickbill.app.QuickBillApplication
import com.quickbill.app.data.Bill
import com.quickbill.app.data.Money
import com.quickbill.app.ui.components.AppCard
import com.quickbill.app.ui.components.IconBadge
import com.quickbill.app.ui.theme.Background
import com.quickbill.app.ui.theme.Blue
import com.quickbill.app.ui.theme.BlueContainer
import com.quickbill.app.ui.theme.BlueDark
import com.quickbill.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class HistoryFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    WEEK("This Week"),
    MONTH("This Month")
}

private fun cutoffFor(filter: HistoryFilter): Long {
    val cal = Calendar.getInstance()
    return when (filter) {
        HistoryFilter.ALL -> 0L
        HistoryFilter.TODAY -> {
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
        HistoryFilter.WEEK -> {
            cal.add(Calendar.DAY_OF_YEAR, -7)
            cal.timeInMillis
        }
        HistoryFilter.MONTH -> {
            cal.add(Calendar.DAY_OF_YEAR, -30)
            cal.timeInMillis
        }
    }
}

@Composable
fun HistoryScreen(onBack: () -> Unit, onOpenBill: (Long) -> Unit) {
    val app = LocalContext.current.applicationContext as QuickBillApplication
    val allBills by app.database.billDao().observeAll().collectAsState(initial = emptyList())

    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    var searchText by remember { mutableStateOf("") }

    // Bills older than QuickBill's retention window are removed automatically
    // (also enforced on app startup and daily in the background); doing it
    // here too keeps this list correct even in a long-running session.
    LaunchedEffect(Unit) {
        app.database.billDao().deleteOlderThan(QuickBillApplication.retentionCutoffMillis())
    }

    val filteredBills = remember(allBills, filter, searchText) {
        val cutoff = cutoffFor(filter)
        allBills.filter { bill ->
            (cutoff == 0L || bill.dateMillis >= cutoff) &&
                (searchText.isBlank() || bill.customerName.contains(searchText, ignoreCase = true) ||
                    bill.invoiceNumber.contains(searchText, ignoreCase = true))
        }
    }

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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HistoryFilter.values().toList()) { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { filter = option },
                        label = { Text(option.label, fontSize = 15.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Search by customer or invoice #") },
                singleLine = true
            )
            Spacer(Modifier.height(6.dp))

            if (filteredBills.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (allBills.isEmpty()) "No bills yet.\nBills you generate will appear here."
                        else "No bills match this filter.",
                        color = TextSecondary,
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                return@Scaffold
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredBills, key = { it.id }) { bill -> HistoryRow(bill) { onOpenBill(bill.id) } }
            }
        }
    }
}

@Composable
private fun HistoryRow(bill: Bill, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Filled.ReceiptLong, tint = Blue, containerColor = BlueContainer)
                Spacer(Modifier.width(12.dp))
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
            }
            Text(Money.format(bill.total), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BlueDark)
        }
    }
}
