@file:OptIn(ExperimentalMaterial3Api::class)

package com.quickbill.app.ui.screens
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quickbill.app.data.BusinessDetails
import com.quickbill.app.data.Prefs
import com.quickbill.app.ui.components.AppCard
import com.quickbill.app.ui.components.LargeTextField
import com.quickbill.app.ui.components.PrimaryButton
import com.quickbill.app.ui.theme.Background
import com.quickbill.app.ui.theme.WhatsAppGreen

@Composable
fun BusinessDetailsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val saved = remember { prefs.getBusinessDetails() }

    var shopName by remember { mutableStateOf(saved.shopName) }
    var address by remember { mutableStateOf(saved.address) }
    var phone by remember { mutableStateOf(saved.phone) }
    var gstin by remember { mutableStateOf(saved.gstin) }
    var savedMessage by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Business Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                "These details appear on every invoice you generate.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            AppCard {
                Column(modifier = Modifier.padding(18.dp)) {
                    LargeTextField(value = shopName, onValueChange = { shopName = it }, label = "Shop / Business name")
                    Spacer(Modifier.height(14.dp))
                    LargeTextField(value = address, onValueChange = { address = it }, label = "Address")
                    Spacer(Modifier.height(14.dp))
                    LargeTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    )
                    Spacer(Modifier.height(14.dp))
                    LargeTextField(value = gstin, onValueChange = { gstin = it }, label = "GSTIN (optional)")
                }
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(text = "Save") {
                prefs.saveBusinessDetails(
                    BusinessDetails(
                        shopName = shopName,
                        address = address,
                        phone = phone,
                        gstin = gstin,
                        logoPath = saved.logoPath
                    )
                )
                savedMessage = true
            }
            if (savedMessage) {
                Spacer(Modifier.height(12.dp))
                Text("Saved.", color = WhatsAppGreen, fontWeight = FontWeight.Medium)
            }
        }
    }
}
