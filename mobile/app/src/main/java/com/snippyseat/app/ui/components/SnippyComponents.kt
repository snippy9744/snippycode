package com.snippyseat.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.snippyseat.app.core.format.formatPrice
import com.snippyseat.app.ui.theme.Error
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary
import com.snippyseat.app.ui.theme.Warning

enum class SnippyButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINED,
}

enum class SnippyStatus {
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    PENDING,
    ACTIVE,
    ERROR,
}

@Composable
fun SnippyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SnippyButtonVariant = SnippyButtonVariant.PRIMARY,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(8.dp)
    val content: @Composable () -> Unit = {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
        } else {
            Text(text)
        }
    }
    when (variant) {
        SnippyButtonVariant.PRIMARY -> Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = modifier.height(52.dp),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            content = { content() },
        )

        SnippyButtonVariant.SECONDARY -> Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = modifier.height(52.dp),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = LightRed, contentColor = Primary),
            content = { content() },
        )

        SnippyButtonVariant.OUTLINED -> OutlinedButton(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = modifier.height(52.dp),
            shape = shape,
            content = { content() },
        )
    }
}

@Composable
fun SnippyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    error: String? = null,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null) } },
        isError = error != null,
        supportingText = error?.let { message -> { Text(message) } },
        minLines = minLines,
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Primary,
            focusedLabelColor = Primary,
        ),
    )
}

@Composable
fun SnippyTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
            }
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        actions()
    }
}

@Composable
fun RatingBar(
    rating: Int,
    modifier: Modifier = Modifier,
    max: Int = 5,
    interactive: Boolean = false,
    onRating: (Int) -> Unit = {},
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..max).forEach { value ->
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = if (value <= rating) Warning else Color(0xFFD8D8D8),
                modifier = Modifier
                    .size(24.dp)
                    .then(if (interactive) Modifier.clickable { onRating(value) } else Modifier),
            )
        }
    }
}

@Composable
fun StatusChip(label: String, status: SnippyStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        SnippyStatus.CONFIRMED -> Color(0xFF1565C0)
        SnippyStatus.COMPLETED, SnippyStatus.ACTIVE -> Success
        SnippyStatus.CANCELLED -> TextSecondary
        SnippyStatus.PENDING -> Warning
        SnippyStatus.ERROR -> Error
    }
    Surface(modifier = modifier, color = color.copy(alpha = 0.12f), contentColor = color, shape = RoundedCornerShape(6.dp)) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = -600f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(animation = tween(1100), repeatMode = RepeatMode.Restart),
        label = "shimmer-offset",
    )
    Canvas(modifier = modifier.background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp))) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFEDEDED), Color.White.copy(alpha = 0.82f), Color(0xFFEDEDED)),
                start = Offset(offset, 0f),
                end = Offset(offset + size.width, size.height),
            ),
        )
    }
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    ctaText: String? = null,
    onCta: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(84.dp).background(LightRed, RoundedCornerShape(42.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Inbox, contentDescription = null, tint = Primary, modifier = Modifier.size(42.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(subtitle, color = TextSecondary, textAlign = TextAlign.Center)
        if (ctaText != null && onCta != null) {
            SnippyButton(text = ctaText, onClick = onCta, variant = SnippyButtonVariant.SECONDARY)
        }
    }
}

@Composable
fun ErrorStateView(
    title: String,
    subtitle: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Error, modifier = Modifier.size(52.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(subtitle, color = TextSecondary, textAlign = TextAlign.Center)
        SnippyButton(text = "Retry", onClick = onRetry)
    }
}

@Composable
fun SnippySnackbar(data: SnackbarData) {
    Snackbar(containerColor = Primary, contentColor = Color.White, snackbarData = data)
}

@Composable
fun SnippySnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data -> SnippySnackbar(data) }
}

@Composable
fun PriceText(amount: Int, modifier: Modifier = Modifier, color: Color = Primary) {
    Text(
        text = formatPrice(amount),
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
        color = color,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun ConfirmationDialog(
    title: String,
    body: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

