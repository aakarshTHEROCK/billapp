@file:OptIn(ExperimentalMaterial3Api::class)

package com.quickbill.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickbill.app.QuickBillApplication
import com.quickbill.app.data.Bill
import com.quickbill.app.data.BillItem
import com.quickbill.app.data.Money
import com.quickbill.app.data.Prefs
import com.quickbill.app.ui.components.AppCard
import com.quickbill.app.ui.components.ConfirmDialog
import com.quickbill.app.ui.components.IconBadge
import com.quickbill.app.ui.components.LargeTextField
import com.quickbill.app.ui.components.PrimaryButton
import com.quickbill.app.ui.components.SecondaryButton
import com.quickbill.app.ui.theme.Background
import com.quickbill.app.ui.theme.Blue
import com.quickbill.app.ui.theme.BlueContainer
import com.quickbill.app.ui.theme.ErrorRed
import com.quickbill.app.ui.theme.Line
import com.quickbill.app.ui.theme.Surface
import kotlinx.coroutines.launch

/**
 * A single garment row's editable state. Fields are Compose snapshot state
 * (`by mutableStateOf`) rather than plain vars, so editing a field inside one
 * card correctly recomposes the running totals up in [CreateBillScreen] even
 * though the object lives inside a [mutableStateListOf].
 */
private class ItemDraft {
    var name by mutableStateOf("")
    var qtyText by mutableStateOf("1")
    var priceText by mutableStateOf("")
    var discount by mutableStateOf(0)

    val quantity: Int get() = Money.parseQuantity(qtyText)
    val price: Double get() = Money.parseAmount(priceText)
    val originalAmount: Double get() = quantity * price
    val discountAmount: Double get() = originalAmount * discount / 100.0
    val finalAmount: Double get() = originalAmount - discountAmount
}

private val discountOptions = (0..50 step 5).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBillScreen(onBack: () -> Unit, onBillGenerated: (Long) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as QuickBillApplication
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf<ItemDraft>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var itemPendingDelete by remember { mutableStateOf(-1) }

    val subtotal = items.sumOf { it.originalAmount }
    val discountTotal = items.sumOf { it.discountAmount }
    val total = subtotal - discountTotal

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Create New Bill", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Text("Customer details", style = MaterialTheme.typography.titleMedium) }
                item {
                    LargeTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = "Customer name (optional)"
                    )
                }
                item {
                    LargeTextField(
                        value = phone,
                        onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                        label = "WhatsApp number (optional)",
                        prefix = "+91",
                        keyboardType = KeyboardType.Phone
                    )
                }
                item { Spacer(Modifier.height(6.dp)) }
                item { Text("Items", style = MaterialTheme.typography.titleMedium) }

                itemsIndexed(items, key = { _, draft -> System.identityHashCode(draft) }) { index, draft ->
                    ItemCard(
                        draft = draft,
                        onErrorCleared = { errorMessage = null },
                        onDelete = { itemPendingDelete = index }
                    )
                }

                item {
                    SecondaryButton(
                        text = "Add Garment",
                        icon = Icons.Filled.Add,
                        onClick = { items.add(ItemDraft()) }
                    )
                }
                item { SummaryCard(subtotal = subtotal, discountTotal = discountTotal, total = total) }
                if (errorMessage != null) {
                    item {
                        Text(errorMessage!!, color = ErrorRed, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                PrimaryButton(text = "Generate Bill", height = 64.dp) {
                    when {
                        items.isEmpty() -> errorMessage = "Please add at least one garment."
                        items.any { it.priceText.isNotBlank() && it.priceText.toDoubleOrNull() == null } ->
                            errorMessage = "Please enter a valid price."
                        else -> {
                            errorMessage = null
                            val billItems = items.map {
                                BillItem(
                                    name = it.name.ifBlank { "Garment" },
                                    quantity = it.quantity,
                                    price = it.price,
                                    discountPercent = it.discount
                                )
                            }
                            val bill = Bill(
                                invoiceNumber = prefs.nextInvoiceNumber(),
                                dateMillis = System.currentTimeMillis(),
                                customerName = customerName.trim(),
                                phoneNumber = phone.trim(),
                                items = billItems,
                                subtotal = subtotal,
                                discountTotal = discountTotal,
                                total = total
                            )
                            scope.launch {
                                val id = app.database.billDao().insert(bill)
                                onBillGenerated(id)
                            }
                        }
                    }
                }
            }
        }
    }

    if (itemPendingDelete >= 0) {
        ConfirmDialog(
            title = "Remove this item?",
            message = "This garment will be removed from the bill.",
            confirmLabel = "Remove",
            onConfirm = {
                items.removeAt(itemPendingDelete)
                itemPendingDelete = -1
            },
            onDismiss = { itemPendingDelete = -1 }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemCard(draft: ItemDraft, onErrorCleared: () -> Unit, onDelete: () -> Unit) {
    var discountExpanded by remember { mutableStateOf(false) }

    AppCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Filled.Checkroom, tint = Blue, containerColor = BlueContainer, size = 36.dp, contentDescription = "Garment")
                Spacer(Modifier.width(10.dp))
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft.name = it; onErrorCleared() },
                    label = { Text("Garment name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete item", tint = ErrorRed)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityStepper(
                    quantity = draft.quantity,
                    onChange = { newQty -> draft.qtyText = newQty.toString(); onErrorCleared() },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = draft.priceText,
                    onValueChange = { draft.priceText = it.filter { c -> c.isDigit() || c == '.' }; onErrorCleared() },
                    label = { Text("Price") },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(12.dp))
            DiscountSelector(
                selected = draft.discount,
                expanded = discountExpanded,
                onExpandedChange = { discountExpanded = it },
                onSelect = { option -> draft.discount = option; onErrorCleared() }
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${draft.quantity} × ${Money.format(draft.price)}  −  ${draft.discount}% (${Money.format(draft.discountAmount)})",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(Money.format(draft.finalAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

/**
 * A plain Box + DropdownMenu implementation rather than Material3's
 * ExposedDropdownMenuBox, whose anchor API (menuAnchor) has changed shape
 * across recent material3 releases. This uses only the long-stable
 * DropdownMenu/DropdownMenuItem APIs.
 */
@Composable
private fun DiscountSelector(
    selected: Int,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (Int) -> Unit
) {
    Column {
        Text(
            "Discount",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, Line, RoundedCornerShape(12.dp))
                    .clickable { onExpandedChange(true) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$selected% off", fontSize = 17.sp)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose discount")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                discountOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text("$option% off", fontSize = 16.sp) },
                        onClick = {
                            onSelect(option)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantityStepper(quantity: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Background, RoundedCornerShape(12.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { if (quantity > 1) onChange(quantity - 1) }, modifier = Modifier.size(44.dp)) {
            Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Text(quantity.toString(), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = { onChange(quantity + 1) }, modifier = Modifier.size(44.dp)) {
            Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryCard(subtotal: Double, discountTotal: Double, total: Double) {
    AppCard {
        Column(modifier = Modifier.padding(18.dp)) {
            SummaryRow("Subtotal", Money.format(subtotal))
            Spacer(Modifier.height(6.dp))
            SummaryRow("Total Discount", "- ${Money.format(discountTotal)}")
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Line)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Final Amount", style = MaterialTheme.typography.titleMedium)
                Text(
                    Money.format(total),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
