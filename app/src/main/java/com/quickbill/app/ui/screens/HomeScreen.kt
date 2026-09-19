@file:OptIn(ExperimentalMaterial3Api::class)

package com.quickbill.app.ui.screens
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickbill.app.QuickBillApplication
import com.quickbill.app.data.Bill
import com.quickbill.app.data.Money
import com.quickbill.app.ui.components.AppCard
import com.quickbill.app.ui.components.IconBadge
import com.quickbill.app.ui.components.PrimaryButton
import com.quickbill.app.ui.components.SecondaryButton
import com.quickbill.app.ui.theme.Background
import com.quickbill.app.ui.theme.Blue
import com.quickbill.app.ui.theme.BlueContainer
import com.quickbill.app.ui.theme.BlueDark
import com.quickbill.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    onCreateBill: () -> Unit,
    onBillHistory: () -> Unit,
    onBusinessDetails: () -> Unit,
    onOpenBill: (Long) -> Unit
) {
    val app = LocalContext.current.applicationContext as QuickBillApplication
    val recentBills by app.database.billDao().observeRecent(5).collectAsState(initial = emptyList())

    Scaffold(containerColor = Background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Hero header - a solid block of color rather than a flat white
            // top, so the app reads as designed rather than a bare form.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF126FEF),
                        RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "QuickBill",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Create and share bills easily.",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                IconButton(
                    onClick = onBusinessDetails,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.18f), CircleShape)
                ) {
                    Icon(Icons.Filled.Storefront, contentDescription = "Business details", tint = Color.White)
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Spacer(Modifier.height(4.dp))

                PrimaryButton(
                    text = "Create New Bill",
                    icon = Icons.Filled.Add,
                    height = 68.dp,
                    onClick = onCreateBill
                )

                Spacer(Modifier.height(14.dp))

                SecondaryButton(
                    text = "Bill History",
                    icon = Icons.Filled.History,
                    onClick = onBillHistory
                )

                if (recentBills.isNotEmpty()) {
                    Spacer(Modifier.height(32.dp))
                    Text("Recent Bills", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(recentBills) { bill -> RecentBillRow(bill) { onOpenBill(bill.id) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentBillRow(bill: Bill, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Filled.ReceiptLong,
                    tint = Blue,
                    containerColor = BlueContainer,
                    size = 40.dp
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(bill.invoiceNumber, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        bill.customerName.ifBlank { "Walk-in customer" },
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
            Text(Money.format(bill.total), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BlueDark)
        }
    }
}
