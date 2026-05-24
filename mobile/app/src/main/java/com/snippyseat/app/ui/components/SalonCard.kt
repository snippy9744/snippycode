package com.snippyseat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.snippyseat.app.data.salon.Salon
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary
import com.snippyseat.app.ui.theme.Warning
import java.util.Locale

@Composable
fun SalonCard(
    salon: Salon,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = if (onClick == null) modifier else modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        if (compact) {
            CompactSalonCardContent(salon = salon)
        } else {
            FullSalonCardContent(salon = salon)
        }
    }
}

@Composable
private fun CompactSalonCardContent(salon: Salon) {
    Column(modifier = Modifier.width(220.dp)) {
        SalonImage(salon = salon, modifier = Modifier.fillMaxWidth().aspectRatio(1.35f))
        SalonInfo(salon = salon, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun FullSalonCardContent(salon: Salon) {
    Row(
        modifier = Modifier.padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SalonImage(
            salon = salon,
            modifier = Modifier
                .size(width = 110.dp, height = 118.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        SalonInfo(salon = salon, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SalonImage(
    salon: Salon,
    modifier: Modifier,
) {
    Box(modifier = modifier.background(LightRed, RoundedCornerShape(8.dp))) {
        AsyncImage(
            model = salon.photoUrl,
            contentDescription = salon.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        if (salon.isPromoted) {
            Surface(
                color = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            ) {
                Text(
                    text = "Promoted",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun SalonInfo(
    salon: Salon,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = salon.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = salon.tags.joinToString(" / "),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RatingText(salon = salon)
            Text(text = "${formatDistance(salon.distanceKm)} km", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "From Rs ${salon.priceFrom}",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                color = Primary,
                fontWeight = FontWeight.Bold,
            )
            StatusChip(open = salon.isOpen)
        }
        if (salon.offersHomeService) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.HomeWork, contentDescription = null, tint = Success, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Home Service", color = Success, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun RatingText(salon: Salon) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = Warning, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = String.format(Locale.US, "%.1f", salon.rating),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
        )
        Text(text = " (${salon.reviewCount})", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StatusChip(open: Boolean) {
    val color = if (open) Success else TextSecondary
    val label = if (open) "Open" else "Closed"

    Surface(color = color.copy(alpha = 0.12f), contentColor = color, shape = RoundedCornerShape(6.dp)) {
        Text(text = label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
    }
}

private fun formatDistance(value: Double): String = String.format(Locale.US, "%.1f", value)
