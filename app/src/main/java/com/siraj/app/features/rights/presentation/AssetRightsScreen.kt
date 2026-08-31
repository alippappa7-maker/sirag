package com.siraj.app.features.rights.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.siraj.app.domain.models.RightsStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetRightsScreen(
    viewModel: AssetRightsViewModel,
    assetId: String,
    currentUserId: String,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(assetId) {
        viewModel.loadAsset(assetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة الحقوق والتراخيص") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val asset = uiState.asset
        if (asset == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(uiState.error ?: "لم يتم العثور على الأصل")
            }
            return@Scaffold
        }

        var sourceUrl by remember { mutableStateOf(asset.sourceUrl) }
        var creatorName by remember { mutableStateOf(asset.creatorName) }
        var provider by remember { mutableStateOf(asset.provider) }
        var licenseType by remember { mutableStateOf(asset.license) }
        var commercialUseAllowed by remember { mutableStateOf(asset.commercialUseAllowed) }
        var attributionRequired by remember { mutableStateOf(asset.attributionRequired) }
        var proofUrl by remember { mutableStateOf(asset.proofUrl) }
        var status by remember { mutableStateOf(asset.rightsStatus) }
        var reason by remember { mutableStateOf("") }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("معلومات الأصل: ${asset.type.name}", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = sourceUrl,
                onValueChange = { sourceUrl = it },
                label = { Text("رابط المصدر") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = creatorName,
                onValueChange = { creatorName = it },
                label = { Text("اسم صانع المحتوى") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = provider,
                onValueChange = { provider = it },
                label = { Text("المزود / المنصة") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = licenseType,
                onValueChange = { licenseType = it },
                label = { Text("نوع الترخيص") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = proofUrl,
                onValueChange = { proofUrl = it },
                label = { Text("رابط إثبات الترخيص (للاستخدام الداخلي فقط)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("مسموح للاستخدام التجاري؟")
                Switch(checked = commercialUseAllowed, onCheckedChange = { commercialUseAllowed = it })
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("يتطلب إسناد (Attribution)؟")
                Switch(checked = attributionRequired, onCheckedChange = { attributionRequired = it })
            }

            Text("حالة الحقوق", style = MaterialTheme.typography.titleSmall)
            // Simplified status selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { status = RightsStatus.COMMERCIAL_ALLOWED },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                if (status ==
                                    RightsStatus.COMMERCIAL_ALLOWED
                                ) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                        ),
                ) { Text("مصرح") }
                Button(
                    onClick = { status = RightsStatus.REJECTED },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                if (status ==
                                    RightsStatus.REJECTED
                                ) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                        ),
                ) { Text("مرفوض") }
            }

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("سبب القرار (سجل المراجعة)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    viewModel.updateRights(
                        assetId = asset.id,
                        reviewerId = currentUserId,
                        sourceUrl = sourceUrl,
                        creatorName = creatorName,
                        provider = provider,
                        licenseType = licenseType,
                        commercialUseAllowed = commercialUseAllowed,
                        modificationAllowed = asset.modificationAllowed,
                        attributionRequired = attributionRequired,
                        attributionText = asset.attribution,
                        proofUrl = proofUrl,
                        expiresAt = asset.expiresAt,
                        newStatus = status,
                        reason = reason,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("تحديث الحقوق")
            }

            if (uiState.isUpdated) {
                Text("تم تحديث البيانات بنجاح", color = MaterialTheme.colorScheme.primary)
            }
            uiState.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
