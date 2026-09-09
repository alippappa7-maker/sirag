package com.siraj.app.features.ideation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.siraj.app.domain.models.ScenarioPlan
import com.siraj.app.domain.models.ScenarioScene

/**
 * شاشة عرض السيناريو المستقلة.
 * يمكن الوصول إليها من IdeationScreen أو مباشرة via Navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioScreen(
    scenarioId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProject: (String) -> Unit,
    viewModel: IdeationViewModel = viewModel(factory = IdeationViewModelFactory()),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scenario = uiState.scenario

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\u0633\u064a\u0646\u0627\u0631\u064a\u0648 \u0627\u0644\u0625\u0646\u062a\u0627\u062c") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\u0631\u062c\u0648\u0639")
                    }
                },
            )
        },
    ) { padding ->
        if (scenario == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("\u062c\u0627\u0631\u064a \u062a\u062d\u0645\u064a\u0644 \u0627\u0644\u0633\u064a\u0646\u0627\u0631\u064a\u0648...")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    ScenarioOverviewCard(scenario)
                }

                item {
                    Text(
                        "\u0627\u0644\u0645\u0634\u0627\u0647\u062f (${scenario.scenes.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                items(scenario.scenes) { scene ->
                    SceneDetailCard(scene)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.createProjectFromScenario(onProjectCreated = onNavigateToProject) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("\u0628\u062f\u0621 \u0627\u0644\u0645\u0634\u0631\u0648\u0639 \u0645\u0646 \u0647\u0630\u0627 \u0627\u0644\u0633\u064a\u0646\u0627\u0631\u064a\u0648")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScenarioOverviewCard(scenario: ScenarioPlan) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssistChip(onClick = {}, label = { Text("\u0627\u0644\u0645\u062f\u0629: ${scenario.totalDurationSeconds / 60}:${String.format("%02d", scenario.totalDurationSeconds % 60)}") })
                AssistChip(onClick = {}, label = { Text("${scenario.scenes.size} \u0645\u0634\u0627\u0647\u062f") })
                if (scenario.visualStyle.isNotBlank()) {
                    AssistChip(onClick = {}, label = { Text(scenario.visualStyle) })
                }
                if (scenario.musicMood.isNotBlank()) {
                    AssistChip(onClick = {}, label = { Text(scenario.musicMood) })
                }
            }
        }
    }
}

@Composable
private fun SceneDetailCard(scene: ScenarioScene) {
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

            if (!scene.arabicText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
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
            }

            if (scene.narration.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("\u0627\u0644\u0633\u0631\u062f:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(scene.narration, style = MaterialTheme.typography.bodyMedium)
            }

            if (scene.visualDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("\u0627\u0644\u0648\u0635\u0641 \u0627\u0644\u0628\u0635\u0631\u064a:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(scene.visualDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (!scene.overlayText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "\u0646\u0635 \u0639\u0644\u0649 \u0627\u0644\u0634\u0627\u0634\u0629: ${scene.overlayText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            if (scene.transitions.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "\u0627\u0644\u0627\u0646\u062a\u0642\u0627\u0644: ${scene.transitions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
