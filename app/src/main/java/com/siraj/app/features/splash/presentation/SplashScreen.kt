package com.siraj.app.features.splash.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.R
import com.siraj.app.core.accessibility.LocalAccessibilityConfig
import com.siraj.app.ui.theme.SirajAccent
import kotlinx.coroutines.delay

private val SplashBackground = Color(0xFF041021) // A deep, premium navy blue

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    val accessibilityConfig = LocalAccessibilityConfig.current
    val reduceMotion = accessibilityConfig.reduceMotion

    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(if (reduceMotion) 400 else 2500)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(durationMillis = 1000))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.siraj_logo_premium),
                    contentDescription = "Siraj Logo",
                    modifier = Modifier
                        .size(160.dp)
                        .scale(if (reduceMotion) 1f else scale)
                        .clip(CircleShape)
                        .testTag("splash_logo_image"),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(durationMillis = 800, delayMillis = 500, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 6 },
                            animationSpec = tween(durationMillis = 800, delayMillis = 500, easing = FastOutSlowInEasing)
                        ),
            ) {
                Text(
                    text = "سراج",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        letterSpacing = 2.sp,
                    ),
                    color = Color(0xFFD4AF37), // Metallic gold
                    modifier = Modifier.testTag("splash_app_title"),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(durationMillis = 800, delayMillis = 800, easing = FastOutSlowInEasing)),
            ) {
                Text(
                    text = "نورٌ يهديك",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFEAF4F0).copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
