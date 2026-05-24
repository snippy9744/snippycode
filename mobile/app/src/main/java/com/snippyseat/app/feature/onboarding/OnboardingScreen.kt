package com.snippyseat.app.feature.onboarding

import androidx.annotation.RawRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.snippyseat.app.R
import com.snippyseat.app.ui.theme.Divider
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.TextSecondary

private data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    @RawRes val animation: Int,
    val icon: ImageVector,
)

private val onboardingSlides = listOf(
    OnboardingSlide(
        title = "Find salons near you",
        subtitle = "Explore trusted salons, stylists, ratings, and home-service options around your area.",
        animation = R.raw.onboarding_map,
        icon = Icons.Outlined.LocationOn,
    ),
    OnboardingSlide(
        title = "Book your time slot instantly",
        subtitle = "Pick services, choose a slot, and reserve your chair without waiting in line.",
        animation = R.raw.onboarding_slots,
        icon = Icons.Outlined.CalendarMonth,
    ),
    OnboardingSlide(
        title = "Skip the queue. Walk in ready.",
        subtitle = "Arrive when your stylist is ready and enjoy a smoother grooming day.",
        animation = R.raw.onboarding_chair,
        icon = Icons.Outlined.Chair,
    ),
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingSlides.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        if (pagerState.currentPage < onboardingSlides.lastIndex) {
            TextButton(
                onClick = onDone,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 28.dp, end = 18.dp),
            ) {
                Text(text = "Skip", color = TextSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(44.dp))
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                OnboardingPage(slide = onboardingSlides[page])
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onboardingSlides.indices.forEach { index ->
                    val active = pagerState.currentPage == index
                    val color by animateColorAsState(
                        targetValue = if (active) Primary else Divider,
                        label = "dot-color",
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (active) 1.25f else 1f,
                        label = "dot-scale",
                    )
                    Canvas(
                        modifier = Modifier
                            .size(if (active) 18.dp else 9.dp, 9.dp)
                            .scale(scale),
                    ) {
                        drawRoundRect(
                            color = color,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Text(text = if (pagerState.currentPage == onboardingSlides.lastIndex) "Get Started" else "Continue")
            }
        }
    }
}

@Composable
private fun OnboardingPage(slide: OnboardingSlide) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(slide.animation))
    val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(230.dp)
                .background(LightRed, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            // Replace raw placeholder JSON files with final Lottie assets when brand animations are ready.
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.matchParentSize(),
            )
            Icon(
                imageVector = slide.icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(88.dp),
            )
        }
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = slide.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = slide.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}
