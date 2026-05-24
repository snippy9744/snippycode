package com.snippyseat.app.feature.booking

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.snippyseat.app.R
import com.snippyseat.app.core.format.SnippyZoneId
import com.snippyseat.app.data.booking.BookingDraft
import com.snippyseat.app.data.booking.BookingDraftStore
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class BookingConfirmationViewModel @Inject constructor(
    draftStore: BookingDraftStore,
) : ViewModel() {
    val draft = draftStore.draft
}

@Composable
fun BookingConfirmationScreen(
    onBackHome: () -> Unit,
    viewModel: BookingConfirmationViewModel = hiltViewModel(),
) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val bookingCode = draft.bookingCode ?: "SNIP-PENDING"

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SuccessAnimation()
            Text("Booking confirmed", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Show this QR at the salon counter.", color = TextSecondary, textAlign = TextAlign.Center)
            Surface(
                modifier = Modifier.clickable {
                    copyBookingCode(context, bookingCode)
                    scope.launch { snackbarHostState.showSnackbar("Copied!") }
                },
                color = LightRed,
                contentColor = Primary,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    bookingCode,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                    fontWeight = FontWeight.Bold,
                )
            }
            QrCode(content = bookingCode)
            DetailsCard(draft)
            Button(
                onClick = { addToCalendar(context, draft) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Add to Calendar")
            }
            OutlinedButton(
                onClick = onBackHome,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Outlined.Home, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Back to Home")
            }
        }
    }
}

@Composable
private fun SuccessAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.booking_success))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
    )
    Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
        LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.matchParentSize())
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(92.dp))
    }
}

@Composable
private fun QrCode(content: String) {
    val matrix = remember(content) {
        QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 33, 33)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 3.dp) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.QrCode2, contentDescription = null, tint = Primary)
            Canvas(modifier = Modifier.size(190.dp)) {
                val cell = size.minDimension / matrix.width
                drawRect(Color.White, size = size)
                for (x in 0 until matrix.width) {
                    for (y in 0 until matrix.height) {
                        if (matrix[x, y]) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(x * cell, y * cell),
                                size = Size(cell, cell),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsCard(draft: BookingDraft) {
    Surface(shape = RoundedCornerShape(8.dp), color = LightRed) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(draft.salon?.name.orEmpty(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(draft.selectedServices.joinToString { it.name }, color = TextSecondary)
            Text("${draft.selectedDate ?: ""} / ${draft.selectedSlot?.label.orEmpty()}", fontWeight = FontWeight.Bold)
            Text(draft.selectedStaff?.name ?: "Any available stylist", color = TextSecondary)
            Text(draft.visitType.label, color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}

private fun copyBookingCode(context: Context, code: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Booking ID", code))
}

private fun addToCalendar(context: Context, draft: BookingDraft) {
    val date = draft.selectedDate ?: return
    val startMinutes = draft.selectedSlot?.label?.split(":")?.mapNotNull { it.toIntOrNull() } ?: return
    val start = date.atTime(startMinutes[0], startMinutes[1]).atZone(SnippyZoneId).toInstant().toEpochMilli()
    val duration = draft.selectedServices.sumOf { it.durationMinutes }.coerceAtLeast(30)
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, "Snippy Seat - ${draft.salon?.name.orEmpty()}")
        putExtra(CalendarContract.Events.EVENT_LOCATION, draft.salon?.address.orEmpty())
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + duration * 60_000L)
    }
    context.startActivity(intent)
}
