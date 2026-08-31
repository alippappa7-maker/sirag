package com.siraj.app.features.mihrab.qibla.presentation

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.ui.theme.SirajAccentDim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: QiblaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QiblaViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()
    var showPermissionRationale by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("القبلة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back)) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (!state.isSensorAvailable) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "حساس البوصلة (Rotation Vector) غير متوفر في هذا الجهاز. لا يمكن عرض الاتجاه.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                if (!state.hasLocationPermission) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "نحتاج إذن الموقع لتحديد اتجاه القبلة بدقة من مكانك الحالي.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { showPermissionRationale = true }) {
                                Text("منح الصلاحية")
                            }
                        }
                    }
                }
                
                if (state.needsCalibration && state.hasLocationPermission) {
                    Text(
                        text = "يرجى تحريك الهاتف على شكل رقم 8 لمعايرة البوصلة",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                // Compass UI
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Animate rotation for smooth compass movement
                    val animatedAzimuth by animateFloatAsState(
                        targetValue = -state.azimuth,
                        animationSpec = tween(durationMillis = 300)
                    )
                    
                    val animatedQibla by animateFloatAsState(
                        targetValue = -state.azimuth + state.qiblaDirection,
                        animationSpec = tween(durationMillis = 300)
                    )

                    // Compass dial (North, South, East, West)
                    Box(modifier = Modifier.fillMaxSize().rotate(animatedAzimuth), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val radius = size.width / 2
                            drawCircle(
                                color = Color.Gray,
                                radius = radius,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            
                            // North Mark
                            drawLine(
                                color = Color.Red,
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, 20.dp.toPx()),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                        
                        Text("ش", color = Color.Red, modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp), fontWeight = FontWeight.Bold)
                        Text("ج", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
                        Text("ش", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp))
                        Text("غ", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp))
                    }

                    // Qibla Pointer (Kaaba Icon or Arrow)
                    Box(modifier = Modifier.fillMaxSize().rotate(animatedQibla), contentAlignment = Alignment.TopCenter) {
                        Canvas(modifier = Modifier.size(24.dp, 60.dp)) {
                            val path = Path().apply {
                                moveTo(size.width / 2, 0f)
                                lineTo(size.width, size.height)
                                lineTo(size.width / 2, size.height * 0.8f)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(path = path, color = SirajAccentDim) // Emerald green for Qibla
                        }
                    }
                }

                // Info
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("المسافة التقريبية إلى مكة", style = MaterialTheme.typography.bodyMedium)
                        Text("${state.distanceKm.toInt()} كم", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("اتجاه القبلة: ${state.qiblaDirection.toInt()}°", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Text(
                    text = "دقة البوصلة تعتمد على حساسات جهازك. تأكد من الابتعاد عن المجالات المغناطيسية.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        if (showPermissionRationale) {
            AlertDialog(
                onDismissRequest = { showPermissionRationale = false },
                title = { Text("إذن الموقع") },
                text = { Text("يطلب سراج صلاحية الموقع لمرة واحدة لحساب اتجاه القبلة والمسافة بدقة، ولا يتم إرسال موقعك لأي جهة خارجية.") },
                confirmButton = {
                    Button(onClick = {
                        showPermissionRationale = false
                        viewModel.setLocationPermissionGranted(true)
                    }) { Text("موافق (تجريبي)") }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionRationale = false }) { Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel)) }
                }
            )
        }
    }
}
