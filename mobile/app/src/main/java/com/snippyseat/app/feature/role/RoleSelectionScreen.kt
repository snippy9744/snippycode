package com.snippyseat.app.feature.role

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snippyseat.app.core.model.UserRole
import com.snippyseat.app.ui.theme.Error
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.TextSecondary

@Composable
fun RoleSelectionScreen(
    onDone: (UserRole) -> Unit,
    viewModel: RoleSelectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 22.dp, vertical = 34.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "How will you use Snippy Seat?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Choose your starting experience. You can manage details later.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(28.dp))
        RoleCard(
            title = "I'm a Customer",
            subtitle = "Find salons and book your time slot",
            icon = Icons.Outlined.ContentCut,
            selected = uiState.selectedRole == UserRole.USER,
            onClick = { viewModel.select(UserRole.USER) },
        )
        Spacer(modifier = Modifier.height(16.dp))
        RoleCard(
            title = "I'm a Salon / Stylist",
            subtitle = "List services, manage bookings, and track earnings",
            icon = Icons.Outlined.Storefront,
            selected = uiState.selectedRole == UserRole.SELLER,
            onClick = { viewModel.select(UserRole.SELLER) },
        )
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = error, color = Error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(26.dp))
        Button(
            onClick = { viewModel.continueWithSelection(onDone) },
            enabled = !uiState.loading,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            if (uiState.loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(text = "Continue")
            }
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.02f else 1f, label = "role-scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Primary else Color(0xFFE8E8E8),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) LightRed else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(34.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            if (selected) {
                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, tint = Primary)
            }
        }
    }
}
