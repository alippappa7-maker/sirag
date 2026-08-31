package com.siraj.app.features.project.presentation.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiImageGeneratorScreen(
    projectId: String,
    sceneId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: AiImageGeneratorViewModel = viewModel(factory = AiImageGeneratorViewModelFactory(projectId, sceneId)),
) {
    val project by viewModel.projectState.collectAsState()
    val selectedSceneId by viewModel.selectedSceneId.collectAsState()
    val prompt by viewModel.prompt.collectAsState()
    val negativePrompt by viewModel.negativePrompt.collectAsState()
    val style by viewModel.style.collectAsState()
    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val count by viewModel.count.collectAsState()
    val status by viewModel.status.collectAsState()
    val generatedImages by viewModel.generatedImages.collectAsState()
    val userCredits by viewModel.userCredits.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showNegativePrompt by remember { mutableStateOf(false) }
    var selectedImageForAction by remember { mutableStateOf<GeneratedImageItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val samplePrompts =
        listOf(
            "مسجد أندلسي أثري عند الغروب مع انعكاس الماء",
            "مخطوطة عربية مذهبة بإضاءة سينمائية دافئة",
            "طبيعة جبلية ساحرة تحت سماء صافية مرصعة بالنجوم",
            "زخارف هندسية إسلامية ثلاثية الأبعاد بظلال متقنة",
        )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("توليد الصور بالذكاء الاصطناعي") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =
                                androidx.compose.ui.res
                                    .stringResource(com.siraj.app.R.string.back),
                        )
                    }
                },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$userCredits رصيد",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Safety & Ethical Disclosure Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ضوابط التوليد البصري الآمن",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "توليد الصور للأغراض الجمالية والإنتاجية فقط. يُمنع منعاً باتاً تجسيد الأنبياء أو الرسل أو الصحابة. جميع المخرجات تحمل وسم AI-generated.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Target Scene Selector
            project?.let { proj ->
                if (proj.scenes.isNotEmpty()) {
                    Text("ربط بالمشهد المستهدف", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        proj.scenes.forEach { sc ->
                            FilterChip(
                                selected = selectedSceneId == sc.id,
                                onClick = { viewModel.selectScene(sc.id) },
                                label = { Text(sc.title.ifBlank { "مشهد ${sc.orderIndex + 1}" }) },
                            )
                        }
                    }
                }
            }

            // Prompt Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("وصف الصورة (Prompt)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { viewModel.updatePrompt(it) },
                    placeholder = { Text("اكتب وصفاً دقيقاً للمشهد البصري...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4,
                )

                // Quick Prompt Presets
                Text("اقتراحات سريعة:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    samplePrompts.forEach { p ->
                        SuggestionChip(
                            onClick = { viewModel.updatePrompt(p) },
                            label = { Text(p, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
            }

            // Negative Prompt (Collapsible)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showNegativePrompt = !showNegativePrompt },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("نص الاستبعاد (Negative Prompt - اختياري)", style = MaterialTheme.typography.labelMedium)
                    Icon(
                        if (showNegativePrompt) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                    )
                }
                AnimatedVisibility(visible = showNegativePrompt) {
                    OutlinedTextField(
                        value = negativePrompt,
                        onValueChange = { viewModel.updateNegativePrompt(it) },
                        placeholder = { Text("عناصر لا ترغب بظهورها (مثال: نصوص عشوائية، تشويش، جودة منخفضة)") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
            }

            // Style Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("الأسلوب الفني (Style)", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AiImageStyle.values().forEach { st ->
                        FilterChip(
                            selected = style == st,
                            onClick = { viewModel.updateStyle(st) },
                            label = { Text(st.displayName) },
                        )
                    }
                }
            }

            // Aspect Ratio Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("أبعاد ومقاس الصورة", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AiImageAspectRatio.values().forEach { ratio ->
                        FilterChip(
                            selected = aspectRatio == ratio,
                            onClick = { viewModel.updateAspectRatio(ratio) },
                            label = { Text(ratio.displayName) },
                        )
                    }
                }
            }

            // Quantity / Batch Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("عدد الصور", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "التكلفة: ${count * 2} رصيد",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 4).forEach { c ->
                        FilterChip(
                            selected = count == c,
                            onClick = { viewModel.updateCount(c) },
                            label = { Text("$c") },
                        )
                    }
                }
            }

            // Generation Button & Action State
            if (status == AiImageStatus.QUEUED || status == AiImageStatus.PROCESSING) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Text(
                            text =
                                if (status ==
                                    AiImageStatus.QUEUED
                                ) {
                                    "جاري إدراج الطلب في طابور الخادم الآمن..."
                                } else {
                                    "جاري التوليد باستخدام Imagen 3..."
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        OutlinedButton(
                            onClick = { viewModel.cancelCurrentGeneration() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إلغاء الطلب واستعادة الرصيد")
                        }
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.generateImages(isRegenerate = false) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("توليد الصور (${count * 2} رصيد)", fontWeight = FontWeight.Bold)
                }
            }

            // Results Section
            if (generatedImages.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "النتائج المولدة (${generatedImages.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    TextButton(onClick = { viewModel.generateImages(isRegenerate = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("توليد بدائل جديدة")
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(generatedImages) { img ->
                        GeneratedImageCard(
                            image = img,
                            onClick = { selectedImageForAction = img },
                        )
                    }
                }
            }
        }

        // Action Sheet / Dialog for Selected Image
        selectedImageForAction?.let { img ->
            AlertDialog(
                onDismissRequest = { selectedImageForAction = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إجراءات الصورة المولدة")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "معاينة: ${img.promptText.take(40)}...",
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp),
                            )
                            // AI Watermark Badge
                            Surface(
                                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text(
                                    text = "AI-GENERATED",
                                    color = Color.Yellow,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }

                        Text("الموديل: ${img.model}", style = MaterialTheme.typography.labelMedium)
                        Text("الأبعاد: ${img.width} × ${img.height} (${img.style.displayName})", style = MaterialTheme.typography.bodySmall)

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = img.licenseNotice,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                confirmButton = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                viewModel.attachToScene(img, asBackground = true)
                                selectedImageForAction = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تعيين كخلفية للمشهد")
                        }
                        FilledTonalButton(
                            onClick = {
                                viewModel.saveToAssetLibrary(img)
                                selectedImageForAction = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ في مكتبة وسائط المشروع")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteImage(img)
                                selectedImageForAction = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حذف الصورة")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedImageForAction = null }) {
                        Text(
                            androidx.compose.ui.res
                                .stringResource(com.siraj.app.R.string.close),
                        )
                    }
                },
            )
        }
    }
}

@Composable
fun GeneratedImageCard(
    image: GeneratedImageItem,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = image.promptText.take(30) + "...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp),
                )
            }

            // AI Badge
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(
                    text = "AI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }

            // Style Badge
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(
                    text = image.style.displayName.take(12),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}
