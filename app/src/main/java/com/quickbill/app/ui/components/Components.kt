@file:OptIn(ExperimentalMaterial3Api::class)

package com.quickbill.app.ui.components
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickbill.app.ui.theme.Blue
import com.quickbill.app.ui.theme.ErrorRed
import com.quickbill.app.ui.theme.WhatsAppGreen

/** The largest, most visually dominant action on a screen. */
@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: androidx.compose.ui.unit.Dp = 60.dp,
    containerColor: androidx.compose.ui.graphics.Color = Blue,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(height),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
        }
        Text(text, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
        }
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun WhatsAppButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    PrimaryButton(text = text, modifier = modifier, containerColor = WhatsAppGreen, onClick = onClick)
}

@Composable
fun LargeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    supportingText: String? = null,
    prefix: String? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 15.sp) },
        modifier = modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 17.sp),
        singleLine = singleLine,
        isError = isError,
        prefix = if (prefix != null) { { Text(prefix, fontSize = 17.sp) } } else null,
        supportingText = if (supportingText != null) {
            { Text(supportingText, color = if (isError) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors()
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold) },
        text = { Text(message, fontSize = 16.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel, fontSize = 16.sp, color = ErrorRed) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", fontSize = 16.sp) }
        }
    )
}
