package com.siraj.app.features.admin.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.siraj.app.data.repository.admin.FirebaseContentManagementRepositoryImpl

@Composable
fun AdminScreen() {
    // In a real app with DI, this would be injected via hiltViewModel()
    val repository = remember { FirebaseContentManagementRepositoryImpl() }
    val viewModel = remember { ContentManagementViewModel(repository) }
    
    ContentManagementScreen(
        viewModel = viewModel,
        onNavigateBack = { /* Handle back navigation */ }
    )
}
