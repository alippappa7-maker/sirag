package com.siraj.app.features.taxonomy.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.domain.models.taxonomy.*
import com.siraj.app.domain.repository.taxonomy.ContentTaxonomyFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentTaxonomyManagementScreen(
    viewModel: ContentTaxonomyViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAuditDialog by remember { mutableStateOf(false) }
    var showMigrationDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ClassifiedContentItem?>(null) }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("تصنيف المحتوى والمصادر", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("نظام التصنيف الموحد، التحقق الخادمي، وحصر الأصول", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("taxonomy_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showAuditDialog = true }, modifier = Modifier.testTag("taxonomy_audit_button")) {
                        Icon(Icons.Default.Assessment, contentDescription = "تقرير التدقيق والتصنيف")
                    }
                    IconButton(onClick = { viewModel.runSampleLegacyMigration(); showMigrationDialog = true }, modifier = Modifier.testTag("taxonomy_migration_button")) {
                        Icon(Icons.Default.MoveToInbox, contentDescription = "ترحيل البيانات القديمة")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Audit Summary Bar
            uiState.auditReport?.let { report ->
                TaxonomyAuditSummaryCard(
                    report = report,
                    onOpenAudit = { showAuditDialog = true }
                )
            }

            // 2. Search & Filter Section
            TaxonomyFilterBar(
                currentFilter = uiState.filter,
                onFilterChanged = { origin, discipline, media, isQuran, isAi, query ->
                    viewModel.updateFilter(
                        originType = origin,
                        disciplineType = discipline,
                        mediaType = media,
                        isQuranOnly = isQuran,
                        isAiOnly = isAi,
                        query = query
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // 3. Content Items List
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Text("لا توجد مواد مطابقة لخيارات التصنيف المحددة", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(uiState.filteredItems, key = { it.id }) { item ->
                        ClassifiedContentCard(
                            item = item,
                            onEditTaxonomy = { itemToEdit = item }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAuditDialog && uiState.auditReport != null) {
        TaxonomyAuditDialog(
            report = uiState.auditReport!!,
            onDismiss = { showAuditDialog = false }
        )
    }

    if (showMigrationDialog && uiState.migrationResult != null) {
        TaxonomyMigrationDialog(
            result = uiState.migrationResult!!,
            onDismiss = { showMigrationDialog = false }
        )
    }

    itemToEdit?.let { item ->
        EditTaxonomyDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onSave = { updatedMetadata ->
                viewModel.updateTaxonomyMetadata(item.id, updatedMetadata)
                itemToEdit = null
            }
        )
    }
}

@Composable
private fun TaxonomyAuditSummaryCard(
    report: TaxonomyAuditReport,
    onOpenAudit: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onOpenAudit() }
            .testTag("taxonomy_audit_summary_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(12.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text("جاهزية تصنيف المحتوى: ${report.compliancePercentage.toInt()}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
                Text(
                    text = "إجمالي: ${report.totalItemsCount} مادة | ${report.quranTextLockedCount} قرآن مقفل | ${report.aiGeneratedItemsCount} AI | غير مصنف: ${report.unclassifiedItemsCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = onOpenAudit, modifier = Modifier.testTag("view_audit_report_btn")) {
                Text("عرض التقرير", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TaxonomyFilterBar(
    currentFilter: ContentTaxonomyFilter,
    onFilterChanged: (ContentOriginType?, ContentDisciplineType?, ContentMediaType?, Boolean, Boolean, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf(currentFilter.query) }
    var selectedOrigin by remember { mutableStateOf(currentFilter.originType) }
    var selectedDiscipline by remember { mutableStateOf(currentFilter.disciplineType) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onFilterChanged(selectedOrigin, selectedDiscipline, currentFilter.mediaType, currentFilter.isQuranOnly, currentFilter.isAiOnly, it)
            },
            placeholder = { Text("بحث بالعنوان أو المصدر أو التخريج...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        onFilterChanged(selectedOrigin, selectedDiscipline, currentFilter.mediaType, currentFilter.isQuranOnly, currentFilter.isAiOnly, "")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("taxonomy_search_input"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedOrigin == null && selectedDiscipline == null,
                onClick = {
                    selectedOrigin = null
                    selectedDiscipline = null
                    onFilterChanged(null, null, null, false, false, searchQuery)
                },
                label = { Text("الكل", fontSize = 11.sp) }
            )

            // Origin filter options
            ContentOriginType.values().forEach { origin ->
                FilterChip(
                    selected = selectedOrigin == origin,
                    onClick = {
                        selectedOrigin = if (selectedOrigin == origin) null else origin
                        onFilterChanged(selectedOrigin, selectedDiscipline, currentFilter.mediaType, currentFilter.isQuranOnly, currentFilter.isAiOnly, searchQuery)
                    },
                    label = { Text(origin.titleArabic, fontSize = 11.sp) }
                )
            }

            // Quran Only
            FilterChip(
                selected = selectedDiscipline == ContentDisciplineType.QURAN_TEXT,
                onClick = {
                    selectedDiscipline = if (selectedDiscipline == ContentDisciplineType.QURAN_TEXT) null else ContentDisciplineType.QURAN_TEXT
                    onFilterChanged(selectedOrigin, selectedDiscipline, currentFilter.mediaType, currentFilter.isQuranOnly, currentFilter.isAiOnly, searchQuery)
                },
                label = { Text("القرآن الكريم", fontSize = 11.sp) }
            )

            // Tafsir Only
            FilterChip(
                selected = selectedDiscipline == ContentDisciplineType.TAFSIR,
                onClick = {
                    selectedDiscipline = if (selectedDiscipline == ContentDisciplineType.TAFSIR) null else ContentDisciplineType.TAFSIR
                    onFilterChanged(selectedOrigin, selectedDiscipline, currentFilter.mediaType, currentFilter.isQuranOnly, currentFilter.isAiOnly, searchQuery)
                },
                label = { Text("التفسير", fontSize = 11.sp) }
            )

            // Hadith Only
            FilterChip(
                selected = selectedDiscipline == ContentDisciplineType.HADITH,
                onClick = {
                    selectedDiscipline = if (selectedDiscipline == ContentDisciplineType.HADITH) null else ContentDisciplineType.HADITH
                    onFilterChanged(selectedOrigin, selectedDiscipline, currentFilter.mediaType, currentFilter.isQuranOnly, currentFilter.isAiOnly, searchQuery)
                },
                label = { Text("الحديث الشريف", fontSize = 11.sp) }
            )
        }
    }
}

@Composable
private fun ClassifiedContentCard(
    item: ClassifiedContentItem,
    onEditTaxonomy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("content_item_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    ContentOriginBadge(originType = item.metadata.originType, isAiAssisted = item.metadata.isAiAssisted)
                    ContentDisciplineBadge(disciplineType = item.metadata.disciplineType)
                }
                
                ContentRightsBadge(rightsStatus = item.metadata.rightsStatus)
            }

            // Locked Quran Banner if Quranic text
            if (item.metadata.isQuranicText || item.metadata.isLockedImmutable) {
                LockedQuranIndicator()
            }

            // Title & Snippet
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = item.contentSnippet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Source Provenance
            SourceProvenanceCard(metadata = item.metadata)

            // Pipeline & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.AltRoute, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                    Text(
                        text = item.metadata.reviewPipelinePath.titleArabic,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp
                    )
                }

                if (!item.metadata.isLockedImmutable) {
                    OutlinedButton(
                        onClick = onEditTaxonomy,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("edit_taxonomy_btn_${item.id}")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تعديل التصنيف", fontSize = 11.sp)
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "مقفل ومحمي",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaxonomyAuditDialog(
    report: TaxonomyAuditReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("تقرير تدقيق تصنيف المحتوى والمصادر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = report.auditSummary, style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider()
                Text("تفصيل الإحصاءات:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Text("• إجمالي المواد المسجلة: ${report.totalItemsCount}")
                Text("• المواد القرآنية المقفلة (Immutable): ${report.quranTextLockedCount}")
                Text("• المواد التحريرية الرسمية: ${report.editorialItemsCount}")
                Text("• محتوى المستخدمين (UGC): ${report.userGeneratedItemsCount}")
                Text("• المواد المولدة بالذكاء الاصطناعي (AI): ${report.aiGeneratedItemsCount}")
                Text("• المواد المرخصة خارجياً: ${report.licensedExternalCount}")
                Text("• مواد بحاجة لتحديد الترخيص: ${report.rightsMissingCount}")
                Text("• مواد غير مصنفة: ${report.unclassifiedItemsCount}")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.testTag("close_audit_dialog_btn")) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
private fun TaxonomyMigrationDialog(
    result: TaxonomyMigrationResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color(0xFF2E7D32))
                Text("نتائج ترحيل البيانات القديمة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("تم ترحيل ${result.successCount} من أصل ${result.totalMigrated} مادة بنجاح إلى البنية التصنيفية الموحدة.")
                HorizontalDivider()
                Text("سجل الترحيل:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                LazyColumn(modifier = Modifier.height(140.dp)) {
                    items(result.migrationLog) { log ->
                        Text("• $log", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.testTag("close_migration_dialog_btn")) {
                Text("تم")
            }
        }
    )
}

@Composable
private fun EditTaxonomyDialog(
    item: ClassifiedContentItem,
    onDismiss: () -> Unit,
    onSave: (ContentTaxonomyMetadata) -> Unit
) {
    var selectedOrigin by remember { mutableStateOf(item.metadata.originType) }
    var selectedDiscipline by remember { mutableStateOf(item.metadata.disciplineType) }
    var selectedRights by remember { mutableStateOf(item.metadata.rightsStatus) }
    var sourceTitle by remember { mutableStateOf(item.metadata.sourceTitle ?: "") }
    var scholarName by remember { mutableStateOf(item.metadata.authorOrScholarName ?: "") }
    var sourceRef by remember { mutableStateOf(item.metadata.sourceReference ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل تصنيف المادة ومصدرها", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("المادة: ${item.title}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                
                // Origin selector
                Text("نوع الأصل (Origin):", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ContentOriginType.values().forEach { origin ->
                        FilterChip(
                            selected = selectedOrigin == origin,
                            onClick = { selectedOrigin = origin },
                            label = { Text(origin.titleArabic, fontSize = 10.sp) }
                        )
                    }
                }

                // Discipline selector
                Text("المجال الشرعي / المعرفي:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ContentDisciplineType.values().forEach { discipline ->
                        FilterChip(
                            selected = selectedDiscipline == discipline,
                            onClick = { selectedDiscipline = discipline },
                            label = { Text(discipline.titleArabic, fontSize = 10.sp) }
                        )
                    }
                }

                // Rights selector
                Text("حالة الحقوق والترخيص:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaxonomyRightsStatus.values().forEach { rights ->
                        FilterChip(
                            selected = selectedRights == rights,
                            onClick = { selectedRights = rights },
                            label = { Text(rights.titleArabic, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = sourceTitle,
                    onValueChange = { sourceTitle = it },
                    label = { Text("اسم المصدر المعتمد", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = scholarName,
                    onValueChange = { scholarName = it },
                    label = { Text("المؤلف / العالم", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sourceRef,
                    onValueChange = { sourceRef = it },
                    label = { Text("التخريج / المرجع", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedMeta = item.metadata.copy(
                        originType = selectedOrigin,
                        disciplineType = selectedDiscipline,
                        rightsStatus = selectedRights,
                        sourceTitle = sourceTitle.ifBlank { null },
                        authorOrScholarName = scholarName.ifBlank { null },
                        sourceReference = sourceRef.ifBlank { null }
                    )
                    onSave(updatedMeta)
                },
                modifier = Modifier.testTag("save_taxonomy_btn")
            ) {
                Text("حفظ التصنيف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
