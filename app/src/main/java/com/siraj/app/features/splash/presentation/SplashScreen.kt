package com.siraj.app.features.splash.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.core.accessibility.LocalAccessibilityConfig
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.siraj.app.core.error.GlobalErrorHandler

private val SplashBackground = Color(0xFF0A1113)
private val EmeraldPrimary = Color(0xFF1A8068)
private val EmeraldContainer = Color(0xFF0D4038)
private val GoldSecondary = Color(0xFFD2A84A)
private val CyanGlow = Color(0xFF55D6C2)
private val LightSpark = Color(0xFFFFF4D0)
private val TextWhite = Color(0xFFEAF4F0)

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    val accessibilityConfig = LocalAccessibilityConfig.current
    val reduceMotion = accessibilityConfig.reduceMotion

    val pointProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val symbolProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val glowProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val titleAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val titleOffsetY = remember { Animatable(if (reduceMotion) 0f else 12f) }

    LaunchedEffect(key1 = reduceMotion) {
        try {
            if (reduceMotion) {
                delay(400)
            } else {
                coroutineScope {
                    // Phase 1: Light point emergence (0-250ms)
                    launch {
                        pointProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing)
                        )
                    }

                    // Phase 2: Formation of Siraj symbol (150-750ms)
                    launch {
                        delay(120)
                        symbolProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                        )
                    }

                    // Phase 3: Subtle cyan techno-spiritual glow (300-800ms)
                    launch {
                        delay(280)
                        glowProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                        )
                    }

                    // Phase 4: App name "سراج" fade-in and smooth upward slide (500-900ms)
                    launch {
                        delay(450)
                        launch {
                            titleAlpha.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                            )
                        }
                        launch {
                            titleOffsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                            )
                        }
                    }
                }
                delay(200) // Brief rest for pleasant viewing before seamless transition
            }
        } catch (e: Exception) {
            GlobalErrorHandler.handle(e)
            // Graceful fallback to avoid blocking
        } finally {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SirajAnimatedLogo(
                pointProgress = pointProgress.value,
                symbolProgress = symbolProgress.value,
                glowProgress = glowProgress.value,
                modifier = Modifier.testTag("splash_logo_canvas")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "سراج",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    letterSpacing = 0.sp
                ),
                color = TextWhite.copy(alpha = titleAlpha.value),
                modifier = Modifier
                    .offset(y = titleOffsetY.value.dp)
                    .testTag("splash_app_title")
            )
        }
    }
}

@Composable
private fun SirajAnimatedLogo(
    pointProgress: Float,
    symbolProgress: Float,
    glowProgress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(136.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f
            val scale = (canvasWidth / 108f) * symbolProgress.coerceAtLeast(0.01f)

            // 1. Subtle Cyan Ambient Glow Aura
            if (glowProgress > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CyanGlow.copy(alpha = 0.20f * glowProgress),
                            CyanGlow.copy(alpha = 0.06f * glowProgress),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = (canvasWidth * 0.48f) * (0.8f + 0.2f * glowProgress)
                    ),
                    radius = canvasWidth * 0.48f,
                    center = Offset(centerX, centerY)
                )
            }

            // 2. Initial Focal Point of Light
            if (pointProgress > 0f) {
                val pointAlpha = if (symbolProgress > 0.6f) {
                    (1f - (symbolProgress - 0.6f) * 2.5f).coerceIn(0f, 1f)
                } else {
                    pointProgress
                }
                if (pointAlpha > 0f) {
                    drawCircle(
                        color = CyanGlow.copy(alpha = 0.55f * pointAlpha),
                        radius = 8f * pointProgress,
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = LightSpark.copy(alpha = pointAlpha),
                        radius = 3.5f * pointProgress,
                        center = Offset(centerX, centerY)
                    )
                }
            }

            // 3. Lantern Symbol Formed through Vector Paths
            if (symbolProgress > 0.05f) {
                val alpha = symbolProgress.coerceIn(0f, 1f)

                fun x(coordX: Float): Float = centerX + (coordX - 54f) * (scale / (canvasWidth / 108f))
                fun y(coordY: Float): Float = centerY + (coordY - 54f) * (scale / (canvasHeight / 108f))

                // Lantern Glass Body / Backing
                val bodyPath = Path().apply {
                    moveTo(x(45f), y(40f))
                    lineTo(x(63f), y(40f))
                    lineTo(x(67f), y(64f))
                    lineTo(x(41f), y(64f))
                    close()
                }
                drawPath(
                    path = bodyPath,
                    color = EmeraldContainer.copy(alpha = 0.85f * alpha)
                )
                drawPath(
                    path = bodyPath,
                    color = EmeraldPrimary.copy(alpha = alpha),
                    style = Stroke(
                        width = 1.8f * (canvasWidth / 108f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Lantern Dome (Top)
                val domePath = Path().apply {
                    moveTo(x(42f), y(34f))
                    lineTo(x(66f), y(34f))
                    lineTo(x(63f), y(40f))
                    lineTo(x(45f), y(40f))
                    close()
                }
                drawPath(
                    path = domePath,
                    color = EmeraldPrimary.copy(alpha = alpha)
                )

                // Top Hook
                val hookPath = Path().apply {
                    moveTo(x(47f), y(31f))
                    cubicTo(x(47f), y(25.5f), x(61f), y(25.5f), x(61f), y(31f))
                }
                drawPath(
                    path = hookPath,
                    color = EmeraldPrimary.copy(alpha = alpha),
                    style = Stroke(width = 2.2f * (canvasWidth / 108f), cap = StrokeCap.Round)
                )

                // Crescent & Star Accent on Top (Gold)
                val crescentPath = Path().apply {
                    moveTo(x(54f), y(18f))
                    cubicTo(x(56.5f), y(18f), x(58.5f), y(19.5f), x(58.5f), y(22f))
                    cubicTo(x(56.5f), y(21f), x(54.5f), y(21.5f), x(53.5f), y(23.5f))
                    cubicTo(x(52f), y(22f), x(52.5f), y(19f), x(54f), y(18f))
                    close()
                }
                drawPath(
                    path = crescentPath,
                    color = GoldSecondary.copy(alpha = alpha)
                )

                // Lantern Base (Emerald with Gold Rim)
                val basePath = Path().apply {
                    moveTo(x(40f), y(64f))
                    lineTo(x(68f), y(64f))
                    lineTo(x(65f), y(71f))
                    lineTo(x(43f), y(71f))
                    close()
                }
                drawPath(
                    path = basePath,
                    color = EmeraldPrimary.copy(alpha = alpha)
                )

                // Base Gold Trim
                val rimPath = Path().apply {
                    moveTo(x(43f), y(71f))
                    lineTo(x(65f), y(71f))
                    lineTo(x(63f), y(73.5f))
                    lineTo(x(45f), y(73.5f))
                    close()
                }
                drawPath(
                    path = rimPath,
                    color = GoldSecondary.copy(alpha = alpha)
                )

                // Inner Flame Core (Siraj Light - Gold & Spark)
                val flamePath = Path().apply {
                    moveTo(x(54f), y(45f))
                    cubicTo(x(50f), y(50f), x(51f), y(55f), x(54f), y(58f))
                    cubicTo(x(57f), y(55f), x(58f), y(50f), x(54f), y(45f))
                    close()
                }
                drawPath(
                    path = flamePath,
                    color = GoldSecondary.copy(alpha = alpha)
                )

                val innerSparkPath = Path().apply {
                    moveTo(x(54f), y(48f))
                    cubicTo(x(52.5f), y(51f), x(53f), y(53.5f), x(54f), y(55f))
                    cubicTo(x(55f), y(53.5f), x(55.5f), y(51f), x(54f), y(48f))
                    close()
                }
                drawPath(
                    path = innerSparkPath,
                    color = LightSpark.copy(alpha = alpha)
                )

                // Subtle Cyan Techno Node Dot
                drawCircle(
                    color = CyanGlow.copy(alpha = alpha),
                    radius = 2.0f * (canvasWidth / 108f),
                    center = Offset(x(54f), y(41f))
                )
            }
        }
    }
}
