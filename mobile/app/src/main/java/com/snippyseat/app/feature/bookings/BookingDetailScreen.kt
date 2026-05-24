package com.snippyseat.app.feature.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snippyseat.app.ui.components.SnippyTopBar
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.TextSecondary

@Composable
fun BookingDetailScreen(
    bookingId: String,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SnippyTopBar(title = "Booking detail", onBack = onBack)
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp), shape = RoundedCornerShape(8.dp), color = LightRed) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(bookingId, style = MaterialTheme.typography.titleLarge, color = Primary, fontWeight = FontWeight.Bold)
                Text("Deep-linked booking summary placeholder", color = TextSecondary)
                Text("Full booking data will load from the backend when /bookings/{id} is exposed.", color = TextSecondary)
            }
        }
    }
}

