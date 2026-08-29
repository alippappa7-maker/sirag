package com.siraj.app.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.*
import com.siraj.app.features.settings.presentation.privacy.PrivacyCenterScreen

enum class SettingsPage(val title: String) {
    MAIN("الإعدادات"),
    ACCOUNT("الحساب"),
    WORKSPACE("مساحة العمل"),
    BILLING("الاستخدام والفوترة"),
    APPEARANCE("المظهر"),
    ACCESSIBILITY("إمكانية الوصول والشمول"),
    LANGUAGE("اللغة والمنطقة"),
    NOTIFICATIONS("الإشعارات"),
    MIHRAB("إعدادات المحراب"),
    VIDEO("إعدادات الفيديو"),
    LIBRARY("إعدادات المكتبة"),
    PRIVACY("مركز الخصوصية وبيانات المستخدم"),
    ISLAMIC("المحتوى الشرعي"),
    STORAGE("التخزين والبيانات"),
    SUPPORT("الدعم"),
    ABOUT("حول التطبيق")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToWorkspaceSettings: () -> Unit = {},
    onNavigateToActivityHistory: () -> Unit = {},
    onNavigateToBilling: () -> Unit = {},
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory())
) {
    var currentPage by remember { mutableStateOf(SettingsPage.MAIN) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveMessage) {
        uiState.saveMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentPage.title) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentPage == SettingsPage.MAIN) {
                            onNavigateBack()
                        } else {
                            currentPage = SettingsPage.MAIN
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (currentPage) {
                SettingsPage.MAIN -> MainSettingsList(
                    onPageSelect = { currentPage = it },
                    onLogout = { viewModel.logout(onLogout) },
                    onNavigateToBilling = onNavigateToBilling
                )
                SettingsPage.ACCOUNT -> AccountSettings(uiState, viewModel, onLogout)
                SettingsPage.WORKSPACE -> onNavigateToWorkspaceSettings()
                SettingsPage.APPEARANCE -> AppearanceSettings(uiState, viewModel)
                SettingsPage.ACCESSIBILITY -> AccessibilitySettings(uiState, viewModel)
                SettingsPage.LANGUAGE -> LanguageSettings(uiState, viewModel)
                SettingsPage.NOTIFICATIONS -> NotificationSettings(uiState, viewModel)
                SettingsPage.MIHRAB -> MihrabSettings(uiState, viewModel)
                SettingsPage.VIDEO -> VideoSettings(uiState, viewModel)
                SettingsPage.LIBRARY -> LibrarySettings(uiState, viewModel)
                SettingsPage.PRIVACY -> PrivacyCenterScreen(
                    userProfile = uiState.profile,
                    onUpdatePreferences = { updateFunc ->
                        viewModel.updatePreferences(updateFunc)
                    },
                    onNavigateToNotifications = { currentPage = SettingsPage.NOTIFICATIONS },
                    onNavigateToActivityHistory = onNavigateToActivityHistory,
                    onNavigateBack = { currentPage = SettingsPage.MAIN },
                    onAccountDeleted = { viewModel.logout(onLogout) }
                )
                SettingsPage.ISLAMIC -> IslamicSettings(uiState, viewModel)
                SettingsPage.STORAGE -> StorageSettings(uiState, viewModel, onLogout, onNavigateToActivityHistory, onNavigateToPrivacyCenter = { currentPage = SettingsPage.PRIVACY })
                SettingsPage.SUPPORT -> SupportSettings(onMessage = { viewModel.showMessage(it) })
                SettingsPage.ABOUT -> AboutSettings()
                SettingsPage.BILLING -> onNavigateToBilling()
            }
            
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun MainSettingsList(onPageSelect: (SettingsPage) -> Unit, onLogout: () -> Unit, onNavigateToBilling: () -> Unit) {
    val items = listOf(
        SettingsItemData("الحساب", Icons.Default.Person, SettingsPage.ACCOUNT),
        SettingsItemData("الاستخدام والفوترة", Icons.Default.CreditCard, SettingsPage.BILLING),
        SettingsItemData("مساحة العمل", Icons.Default.Build, SettingsPage.WORKSPACE),
        SettingsItemData("المظهر", Icons.Default.Palette, SettingsPage.APPEARANCE),
        SettingsItemData("إمكانية الوصول والشمول", Icons.Default.AccessibilityNew, SettingsPage.ACCESSIBILITY),
        SettingsItemData("اللغة والمنطقة", Icons.Default.Language, SettingsPage.LANGUAGE),
        SettingsItemData("الإشعارات", Icons.Default.Notifications, SettingsPage.NOTIFICATIONS),
        SettingsItemData("إعدادات المحراب", Icons.Default.Star, SettingsPage.MIHRAB),
        SettingsItemData("إعدادات الفيديو", Icons.Default.PlayArrow, SettingsPage.VIDEO),
        SettingsItemData("إعدادات المكتبة", Icons.Default.List, SettingsPage.LIBRARY),
        SettingsItemData("الخصوصية والأمان", Icons.Default.Lock, SettingsPage.PRIVACY),
        SettingsItemData("المحتوى الشرعي", Icons.Default.CheckCircle, SettingsPage.ISLAMIC),
        SettingsItemData("التخزين والبيانات", Icons.Default.Storage, SettingsPage.STORAGE),
        SettingsItemData("الدعم", Icons.Default.Info, SettingsPage.SUPPORT),
        SettingsItemData("حول التطبيق", Icons.Default.Info, SettingsPage.ABOUT)
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items) { item ->
            SettingsListItem(
                title = item.title,
                icon = item.icon,
                onClick = {
                    if (item.page == SettingsPage.BILLING) {
                        onNavigateToBilling()
                    } else {
                        onPageSelect(item.page)
                    }
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.logout))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

data class SettingsItemData(val title: String, val icon: ImageVector, val page: SettingsPage)

@Composable
fun SettingsListItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
