package com.siraj.app.features.share.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.siraj.app.core.ui.components.SirajButton
import androidx.compose.material3.CircularProgressIndicator
import com.siraj.app.domain.models.share.ShareType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedContentRouterScreen(
    linkId: String,
    token: String?,
    viewModel: SharedContentViewModel,
    onNavigateToProject: (String) -> Unit,
    onNavigateToAudio: (String) -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateHome: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(linkId, token) {
        viewModel.validateLink(linkId, token)
    }

    // Effect to automatically route on success
    LaunchedEffect(state) {
        if (state is SharedContentState.Success) {
            val link = (state as SharedContentState.Success).shareLink
            when (link.type) {
                ShareType.PROJECT -> onNavigateToProject(link.targetId)
                ShareType.AUDIO -> onNavigateToAudio(link.targetId)
                ShareType.QURAN -> onNavigateToQuran()
                // Default routing for other types in this MVP
                else -> onNavigateHome()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فتح الرابط") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is SharedContentState.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "جاري التحقق من الرابط...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                is SharedContentState.Error -> {
                    val message = (state as SharedContentState.Error).message
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        SirajButton(
                            text = "العودة للرئيسية",
                            onClick = onNavigateHome,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                is SharedContentState.Success -> {
                    // Empty state as it will navigate automatically via LaunchedEffect
                }
            }
        }
    }
}
