package com.siraj.app.features.ideation.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.GeneratedIdea
import com.siraj.app.domain.models.IdeaGenerationRequest
import com.siraj.app.domain.models.RiskLevel
import com.siraj.app.domain.models.ScenarioPlan
import com.siraj.app.domain.models.ScenarioScene

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProject: (String) -> Unit,
    onNavigateToScenario: (String) -> Unit = {},
    viewModel: IdeationViewModel = viewModel(factory = IdeationViewModelFactory()),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState.generationMode) {
                            GenerationMode.SCENARIO -> "\u0633\u064a\u0646\u0627\u0631\u064a\u0648 \u0627\u0644\u0625\u0646\u062a\u0627\u062c"
                            GenerationMode.IDEAS -> "\u0645\u0648\u0644\u062f \u0627\u0644\u0623\u0641\u0643\u0627\u0631"
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            uiState.generationMode == GenerationMode.SCENARIO -> viewModel.backToIdeas()
                            uiState.generatedIdeas.isNotEmpty() -> viewModel.clearIdeas()
                            else -> onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\u0631\u062c\u0648\u0639")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isGenerating || uiState.isGeneratingScenario) {
                LoadingView(uiState.isGeneratingScenario)
            } else if (uiState.generationMode == GenerationMode.SCENARIO && uiState.scenario != null) {
                ScenarioResultView(
                    scenario = uiState.scenario!!,
                    onCreateProject = { viewModel.createProjectFromScenario(onProjectCreated = onNavigateToProject) },
                    onBack = { viewModel.backToIdeas() },
                    onOpenProject = onNavigateToProject,
                )
            } else if (uiState.generatedIdeas.isNotEmpty()) {
                IdeasResultView(
                    ideas = uiState.generatedIdeas,
                    onGenerateScenario = { idea -> viewModel.generateScenario(idea) },
                    onConvertToProject = { idea -> viewModel.convertToProject(idea, onProjectCreated = onNavigateToProject) },
                    onDismiss = { id -> viewModel.dismissIdea(id) },
                    onReport = { id -> viewModel.reportIdea(id, "\u0645\u062d\u062a\u0648\u0649 \u063a\u064a\u0631 \u0645\u0646\u0627\u0633\u0628") },
                    onRegenerate = { viewModel.generateIdeas() },
                )
            } else {
                IdeationFormView(
                    request = uiState.request,
                    onUpdateRequest = { update -> viewModel.updateRequest(update) },
                    onGenerate = { viewModel.generateIdeas() },
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// شاشة التحميل
// ═══════════════════════════════════════════════════════

@Composable
private fun LoadingView(isScenario: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (isScenario) "\u062c\u0627\u0631\u064a \u062a\u0648\u0644\u064a\u062f \u0627\u0644\u0633\u064a\u0646\u0627\u0631\u064a\u0648 \u0628\u0627\u0644\u0630\u0643\u0627\u0621 \u0627\u0644\u0627\u0635\u0637\u0646\u0627\u0639\u064a..."
            else
                "\u062c\u0627\u0631\u064a \u0627\u0644\u0639\u0635\u0641 \u0627\u0644\u0630\u0647\u0646\u064a \u0648\u062a\u0648\u0644\u064a\u062f \u0627\u0644\u0623\u0641\u0643\u0627\u0631...",
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "\u0642\u062f \u064a\u0633\u062a\u063a\u0631\u0642 \u0647\u0630\u0627 15-30 \u062b\u0627\u0646\u064a\u0629",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ═══════════════════════════════════════════════════════
// نموذج الإدخال
// ═══════════════════════════════════════════════════════

@Composable
private fun IdeationFormView(
    request: IdeaGenerationRequest,
    onUpdateRequest: ((IdeaGenerationRequest) -> IdeaGenerationRequest) -> Unit,
    onGenerate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "\u0623\u0636\u064a\u0641 \u0641\u0643\u0631\u0629 \u0648\u0633\u0623\u062d\u0648\u0644\u0647\u0627 \u0644\u0633\u064a\u0646\u0627\u0631\u064a\u0648 \u0625\u0646\u062a\u0627\u062c \u0643\u0627\u0645\u0644!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "\u0623\u062e\u0628\u0631\u0646\u064a \u0639\u0646 \u0627\u0644\u0645\u0648\u0636\u0648\u0639 \u0648\u0623\u0646\u0627 \u0623\u0639\u0637\u064a \u0623\u0641\u0643\u0627\u0631\u0627\u064b \u0645\u0639 \u0633\u064a\u0646\u0627\u0631\u064a\u0648 \u0628\u0627\u0644\u0645\u0634\u0627\u0647\u062f \u0648\u0627\u0644\u062a\u0648\u0642\u064a\u062a.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = request.subject,
            onValueChange = { s -> onUpdateRequest { it.copy(subject = s) } },
            label = { Text("\u0645\u0627 \u0627\u0644\u0645\u0648\u0636\u0648\u0639 \u0627\u0644\u0630\u064a \u062a\u0631\u064a\u062f \u0627\u0644\u062a\u062d\u062f\u062b \u0639\u0646\u0647\u061f") },
            placeholder = { Text("\u0645\u062b\u0627\u0644: \u0623\u0647\u0645\u064a\u0629 \u0627\u0644\u0635\u0628\u0631 \u0641\u064a \u0627\u0644\u062d\u064a\u0627\u0629 \u0627\u0644\u064a\u0648\u0645\u064a\u0629") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )

        DropdownOption(
            label = "\u0627\u0644\u062c\u0645\u0647\u0648\u0631 \u0627\u0644\u0645\u0633\u062a\u0647\u062f\u0641",
            options = listOf("\u0639\u0627\u0645", "\u0634\u0628\u0627\u0628", "\u0646\u0633\u0627\u0621", "\u0623\u0637\u0641\u0627\u0644", "\u0637\u0644\u0627\u0628 \u062c\u0627\u0645\u0639\u064a\u0646", "\u062f\u0639\u0627\u0629", "\u0645\u0639\u0644\u0645\u0648\u0646"),
            selected = request.audience,
            onSelected = { a -> onUpdateRequest { it.copy(audience = a) } },
        )

        DropdownOption(
            label = "\u0627\u0644\u0645\u0646\u0635\u0629",
            options = listOf(
                "TikTok / Reels (9:16)",
                "YouTube Shorts (9:16)",
                "YouTube Long (16:9)",
                "Twitter / X (1:1)",
                "Instagram Post (1:1)",
                "WhatsApp Status (9:16)",
            ),
            selected = request.platform,
            onSelected = { p -> onUpdateRequest { it.copy(platform = p) } },
        )

        DropdownOption(
            label = "\u0627\u0644\u0645\u062f\u0629",
            options = listOf(
                "\u0642\u0635\u064a\u0631 \u062c\u062f\u0627\u064b (15 \u062b\u0627\u0646\u064a\u0629)",
                "\u0642\u0635\u064a\u0631 (\u0623\u0642\u0644 \u0645\u0646 \u062f\u0642\u064a\u0642\u0629)",
                "\u0645\u062a\u0648\u0633\u0637 (1-3 \u062f\u0642\u0627\u0626\u0642)",
                "\u0637\u0648\u064a\u0644 (3-10 \u062f\u0642\u0627\u0626\u0642)",
                "\u0637\u0648\u064a\u0644 \u062c\u062f\u0627\u064b (\u0623\u0643\u062b\u0631 \u0645\u0646 10 \u062f\u0642\u0627\u0626\u0642)",
            ),
            selected = request.duration,
            onSelected = { d -> onUpdateRequest { it.copy(duration = d) } },
        )

        DropdownOption(
            label = "\u0627\u0644\u0646\u0628\u0631\u0629",
            options = listOf("\u062a\u062d\u0641\u064a\u0632\u064a", "\u062a\u0639\u0644\u064a\u0645\u064a", "\u062a\u0623\u0645\u0644\u064a", "\u0642\u0635\u0635\u064a", "\u0646\u0642\u062f\u064a \u0628\u0646\u0627\u0621\u0629", "\u0641\u0643\u0647\u064a"),
            selected = request.tone,
            onSelected = { t -> onUpdateRequest { it.copy(tone = t) } },
        )

        DropdownOption(
            label = "\u0627\u0644\u0647\u062f\u0641",
            options = listOf("\u062a\u0648\u0639\u064a\u0629", "\u062a\u0639\u0644\u064a\u0645", "\u062f\u0639\u0648\u0629", "\u062a\u0631\u0636\u064a\u062d", "\u062a\u0639\u0632\u064a\u0632 \u0644\u0644\u0625\u0633\u0644\u0627\u0645"),
            selected = request.goal,
            onSelected = { g -> onUpdateRequest { it.copy(goal = g) } },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = request.hasReligiousElement,
                onCheckedChange = { c -> onUpdateRequest { it.copy(hasReligiousElement = c) } },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("\u064a\u062a\u0636\u0645\u0646 \u0639\u0646\u0627\u0635\u0631 \u062f\u064a\u0646\u064a\u0627\u064b (\u0642\u0631\u0622\u0646 / \u062d\u062f\u064a\u062b)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = request.subject.isNotBlank(),
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("\u062a\u0648\u0644\u064a\u062f \u0627\u0644\u0623\u0641\u0643\u0627\u0631 \u0648\u0627\u0644\u0633\u064a\u0646\u0627\u0631\u064a\u0648\u0627\u062a")
        }
    }
}

// ═══════════════════════════════════════════════════════
// قائمة الأفكار الناتجة
// ═══════════════════════════════════════════════════════

@Composable
private fun IdeasResultView(
    ideas: List<GeneratedIdea>,
    onGenerateScenario: (GeneratedIdea) -> Unit,
    onConvertToProject: (GeneratedIdea) -> Unit,
    onDismiss: (String) -> Unit,
    onReport: (String) -> Unit,
    onRegenerate: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("\u0627\u0644\u0623\u0641\u0643\u0627\u0631 \u0627\u0644\u0645\u0642\u062a\u0631\u062d\u0629", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "${ideas.size} \u0623\u0641\u0643\u0627\u0631. \u0627\u062e\u062a\u0631 \u0641\u0643\u0631\u0629 \u0644\u062a\u062d\u0648\u064a\u0644\u0647\u0627 \u0644\u0633\u064a\u0646\u0627\u0631\u064a\u0648.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(ideas) { idea ->
            IdeaCard(
                idea = idea,
                onGenerateScenario = { onGenerateScenario(idea) },
                onConvertToProject = { onConvertToProject(idea) },
                onDismiss = { onDismiss(idea.id) },
                onReport = { onReport(idea.id) },
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onRegenerate,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("\u0625\u0639\u0627\u062f\u0629 \u062a\u0648\u0644\u064a\u062f \u0623\u0641\u0643\u0627\u0631 \u062c\u062f\u064a\u062f\u0629")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// بطاقة فكرة
// ═══════════════════════════════════════════════════════

@Composable
private fun IdeaCard(
    idea: GeneratedIdea,
    onGenerateScenario: () -> Unit,
    onConvertToProject: () -> Unit,
    onDismiss: () -> Unit,
    onReport: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                idea.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("\u0627\u0644\u062e\u0637\u0627\u0641 (Hook):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text(idea.hook, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Text("\u0627\u0644\u0645\u0644\u062e\u0635:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text(idea.summary, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(idea.audience) })
                AssistChip(onClick = {}, label = { Text("${idea.suggestedScenes.size} \u0645\u0634\u0627\u0647\u062f \u0645\u0642\u062a\u0631\u062d") })
                if (idea.tags.isNotEmpty()) {
                    AssistChip(onClick = {}, label = { Text(idea.tags.first()) })
                }
            }

            if (idea.needsReview || idea.riskLevel == RiskLevel.HIGH) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "\u062a\u0646\u0628\u064a\u0647",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "\u0645\u062e\u0627\u0637\u0631\u0629 ${idea.riskLevel.name}: ${idea.disclaimer ?: "\u064a\u062d\u062a\u0627\u062c \u0645\u0631\u0627\u062c\u0639\u0629 \u0628\u0634\u0631\u064a\u0629"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // أزرار الإجراءات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Delete, contentDescription = "\u0631\u0641\u0636", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onReport) {
                    Icon(Icons.Default.Flag, contentDescription = "\u0625\u063a\u0644\u0627\u0637", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.weight(1f))

                // الزر الرئيسي: تحويل لسيناريو
                Button(
                    onClick = onGenerateScenario,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("\u062a\u062d\u0648\u064a\u0644 \u0644\u0633\u064a\u0646\u0627\u0631\u064a\u0648")
                }

                // زر ثانوي: بدء مشروع مباشر
                OutlinedButton(onClick = onConvertToProject) {
                    Text("\u0628\u062f\u0621 \u0627\u0644\u0645\u0634\u0631\u0648\u0639")
                }
            }
        }
    }
}


// ═══════════════════════════════════════════════════════
// عرض السيناريو الناتج
// ═══════════════════════════════════════════════════════

@Composable
private fun ScenarioResultView(
    scenario: ScenarioPlan,
    onCreateProject: () -> Unit,
    onBack: () -> Unit,
    onOpenProject: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ملخص السيناريو
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        scenario.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        scenario.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text("\u0627\u0644\u0645\u062f\u0629: ${scenario.totalDurationSeconds / 60}:${String.format("%02d", scenario.totalDurationSeconds % 60)}")
                            },
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("${scenario.scenes.size} \u0645\u0634\u0627\u0647\u062f") },
                        )
                        if (scenario.visualStyle.isNotBlank()) {
                            AssistChip(onClick = {}, label = { Text(scenario.visualStyle) })
                        }
                    }
                }
            }
        }

        // الخطاف والخاتمة
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("\u0627\u0644\u062e\u0637\u0627\u0641 \u0627\u0644\u0628\u062f\u0627\u064a\u0629:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(scenario.openingHook)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("\u0627\u0644\u062f\u0639\u0648\u0629 \u0644\u0644\u0641\u0639\u0644:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(scenario.closingCTA.ifBlank { "\u0644\u0645 \u064a\u062a\u0645 \u062a\u062d\u062f\u064a\u062f \u062f\u0639\u0648\u0629 \u0644\u0644\u0641\u0639\u0644 \u0627\u0644\u0645\u0646\u0627\u0633\u0628" })
                }
            }
        }

        // عنوان قسم المشاهد
        item {
            Text(
                "\u0627\u0644\u0645\u0634\u0627\u0647\u062f",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        // المشاهد كخط زمني
        items(scenario.scenes) { scene ->
            ScenarioSceneCard(scene)
        }

        // أزرار الإجراءات
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreateProject,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("\u0628\u062f\u0621 \u0627\u0644\u0645\u0634\u0631\u0648\u0639 \u0645\u0646 \u0647\u0630\u0627 \u0627\u0644\u0633\u064a\u0646\u0627\u0631\u064a\u0648")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("\u0627\u0644\u0639\u0648\u062f\u0629 \u0644\u0642\u0627\u0626\u0645\u0629 \u0627\u0644\u0623\u0641\u0643\u0627\u0631")
            }
        }
    }
}

@Composable
private fun ScenarioSceneCard(scene: ScenarioScene) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "\u0645\u0634\u0647\u062f ${scene.order}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "${scene.durationSeconds}\u062b",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                scene.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            // النص القرآني/الحديثي
            if (!scene.arabicText.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            scene.arabicText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        if (!scene.arabicSource.isNullOrBlank()) {
                            Text(
                                scene.arabicSource,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // السرد
            if (scene.narration.isNotBlank()) {
                Text("\u0627\u0644\u0633\u0631\u062f:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(scene.narration, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // الوصف البصري
            if (scene.visualDescription.isNotBlank()) {
                Text("\u0627\u0644\u0648\u0635\u0641 \u0627\u0644\u0628\u0635\u0631\u064a:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(
                    scene.visualDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // النص على الشاشة
            if (!scene.overlayText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "\u0646\u0635 \u0639\u0644\u0649 \u0627\u0644\u0634\u0627\u0634\u0629: ${scene.overlayText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// قائمة منسدلة
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownOption(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    },
                )
            }
        }
    }
}
