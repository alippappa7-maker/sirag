package com.siraj.app.features.splash.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.siraj.app.core.accessibility.LocalAccessibilityConfig
import com.siraj.app.ui.theme.SirajAccent
import kotlinx.coroutines.delay

private val SplashBackground = Color(0xFF060B14)

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    val accessibilityConfig = LocalAccessibilityConfig.current
    val reduceMotion = accessibilityConfig.reduceMotion

    // Composition is loaded from assets; null while loading.
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("siraj_splash_star.json"))

    // Play once when the composition is ready. In reduced-motion we freeze on the
    // final frame (progress = 1f) instead of playing an animation.
    val progress = animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = !reduceMotion,
        speed = if (reduceMotion) 0f else 1f,
    )

    // Single effect: runs once for the screen's lifetime. Navigation happens only
    // after the animation (or the reduced-motion rest) completes — never in a
    // finally block, which would fire early when the effect is cancelled.
    LaunchedEffect(Unit) {
        try {
            if (reduceMotion) {
                delay(400)
            } else {
                // Wait for the Lottie composition to finish loading (safety cap).
                var loadWait = 0L
                while (composition == null && loadWait < 2000L) {
                    delay(50)
                    loadWait += 50
                }
                // Poll playback progress until it completes, with a hard safety cap
                // so navigation never hangs if the animation stalls.
                val deadline = System.currentTimeMillis() + 4000L
                while (progress.value < 0.99f && System.currentTimeMillis() < deadline) {
                    delay(33)
                }
                delay(150) // brief rest for a graceful handoff
            }
        } catch (_: Exception) {
            // Never block navigation on an animation hiccup.
        }
        onNavigateToHome()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(SplashBackground)
                .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LottieAnimation(
                composition = composition,
                progress = { if (reduceMotion) 1f else progress.value },
                modifier =
                    Modifier
                        .size(160.dp)
                        .testTag("splash_logo_canvas"),
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = reduceMotion || progress.value >= 0.35f,
                enter =
                    fadeIn(animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 6 },
                            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                        ),
            ) {
                Text(
                    text = "سراج",
                    style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            letterSpacing = 2.sp,
                        ),
                    color = Color(0xFFEAF4F0),
                    modifier = Modifier.testTag("splash_app_title"),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = reduceMotion || progress.value >= 0.5f,
                enter = fadeIn(animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)),
            ) {
                Text(
                    text = "نورٌ يهديك",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SirajAccent.copy(alpha = 0.8f),
                )
            }
        }
    }
}
