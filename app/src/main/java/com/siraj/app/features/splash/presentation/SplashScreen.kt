package com.siraj.app.features.splash.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        visible = true
        delay(2000)
        visible = false
        delay(500)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Techno-spiritual subtle glow
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1000)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1000)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Text(
                text = "سراج",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
