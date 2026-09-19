@file:OptIn(ExperimentalMaterial3Api::class)

package com.quickbill.app.ui.components
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickbill.app.ui.theme.Blue
import com.quickbill.app.ui.theme.ErrorRed
import com.quickbill.app.ui.theme.Line
import com.quickbill.app.ui.theme.Surface
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
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
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
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Blue),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue)
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

/**
 * The one card style used everywhere in the app (item rows, summaries,
 * history rows, the invoice preview). A soft shadow plus a hairline border
 * is what actually reads as "a raised card" instead of a flat rectangle -
 * a plain white Card on a near-white background is where most of the
 * "looks dull" feedback comes from.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    var cardModifier = modifier.fillMaxWidth()
    if (onClick != null) {
        cardModifier = cardModifier.clip(shape).clickable(onClick = onClick)
    }

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

/** A small icon on a tinted circular background - used for consistent, non-dull iconography. */
@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    containerColor: Color,
    size: Dp = 40.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size * 0.55f))
    }
}
