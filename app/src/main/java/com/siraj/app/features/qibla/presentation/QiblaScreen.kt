package com.siraj.app.features.qibla.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    onNavigateBack: () -> Unit,
    viewModel: QiblaViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // طلب إذن الموقع
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            // TODO: الحصول على الموقع الفعلي عبر FusedLocationProvider
            // مؤقتاً: استخدام موقع افتراضي (مكة المكرمة)
            viewModel.setUserLocation(21.4225, 39.8262)
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
        viewModel.startSensors()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopSensors() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("اتجاه القبلة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            // بطاقة المعلومات
            QiblaInfoCard(uiState)

            // البوصلة
            QiblaCompass(uiState)

            // مؤشر الاتجاه
            DirectionIndicator(uiState)
        }
    }
}

@Composable
private fun QiblaInfoCard(uiState: QiblaUiState) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (uiState.distanceToKaaba > 0) {
                        val km = uiState.distanceToKaaba / 1000
                        if (km > 1000) "${String.format("%.0f", km)} كم" else "${String.format("%.1f", km)} كم"
                    } else "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text("المسافة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(32.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Explore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = "${uiState.qiblaDirection.toInt()}°",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text("الاتجاه", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun QiblaCompass(uiState: QiblaUiState) {
    val rotation by animateFloatAsState(
        targetValue = -uiState.currentHeading,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "compass_rotation",
    )

    val qiblaAngle = uiState.qiblaDirection
    val isPointing = uiState.isPointingToQibla

    val glowColor = if (isPointing) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPointing) 0.6f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        // حلقة التوهج
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape),
        ) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 8

            // حلقة خارجية متوهجة
            drawCircle(
                color = glowColor.copy(alpha = glowAlpha * 0.3f),
                radius = radius + 12,
                center = center,
                style = Stroke(width = 4.dp.toPx()),
            )

            // الدائرة الرئيسية
            drawCircle(
                color = glowColor.copy(alpha = 0.1f),
                radius = radius,
                center = center,
            )

            drawCircle(
                color = glowColor.copy(alpha = 0.8f),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        // البوصلة الدوارة
        Box(
            modifier = Modifier
                .size(240.dp)
                .rotate(rotation),
            contentAlignment = Alignment.Center,
        ) {
            CompassDial()

            // سهم القبلة
            val qiblaRotation = qiblaAngle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .rotate(qiblaRotation),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // الكعبة
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPointing) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(width = 36.dp, height = 48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "القبلة",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }

        // النقطة المركزية
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun CompassDial() {
    Canvas(modifier = Modifier.size(240.dp)) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 16

        // علامات الاتجاهات
        val directions = listOf("ش", "شرق", "ج", "غرب")
        for (i in 0 until 4) {
            val angle = i * 90.0
            val rad = Math.toRadians(angle - 90)
            val x = center.x + (radius - 24) * cos(rad).toFloat()
            val y = center.y + (radius - 24) * sin(rad).toFloat()
        }

        // خطوط صغيرة كل 10 درجات
        for (i in 0 until 36) {
            val angle = i * 10.0
            val rad = Math.toRadians(angle - 90)
            val innerR = if (i % 9 == 0) radius - 16 else radius - 8
            val x1 = center.x + innerR * cos(rad).toFloat()
            val y1 = center.y + innerR * sin(rad).toFloat()
            val x2 = center.x + radius * cos(rad).toFloat()
            val y2 = center.y + radius * sin(rad).toFloat()
            drawLine(
                color = Color.White.copy(alpha = if (i % 9 == 0) 0.8f else 0.3f),
                start = androidx.compose.ui.geometry.Offset(x1, y1),
                end = androidx.compose.ui.geometry.Offset(x2, y2),
                strokeWidth = if (i % 9 == 0) 3f else 1f,
            )
        }
    }
}

@Composable
private fun DirectionIndicator(uiState: QiblaUiState) {
    val isPointing = uiState.isPointingToQibla
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isPointing) 1f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPointing) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (isPointing) Icons.Default.CheckCircle else Icons.Default.NearMe,
                contentDescription = null,
                tint = if (isPointing) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isPointing) "أنت باتجاه القبلة" else "در إلى اتجاه القبلة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = if (isPointing) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
