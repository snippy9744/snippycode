package com.snippyseat.app.feature.splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snippyseat.app.R
import com.snippyseat.app.ui.theme.Primary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    var visible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.7f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "logo-scale",
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "logo-alpha",
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (textVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "text-alpha",
    )
    val textOffset by animateFloatAsState(
        targetValue = if (textVisible) 0f else 20f,
        animationSpec = tween(durationMillis = 500),
        label = "text-offset",
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(600)
        textVisible = true
        delay(1_900)
        onFinished(viewModel.resolveDestination())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(156.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.snippy_seat_logo),
                    contentDescription = "Snippy Seat logo",
                    modifier = Modifier.size(156.dp),
                )
            }
            Text(
                text = "Snippy Seat",
                color = Color.White.copy(alpha = textAlpha),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 22.sp),
                modifier = Modifier.offset(y = textOffset.dp),
            )
        }
    }
}
