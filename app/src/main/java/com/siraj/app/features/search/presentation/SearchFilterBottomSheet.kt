package com.siraj.app.features.search.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.core.ui.components.SirajTechCard
import com.siraj.app.domain.models.search.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterBottomSheet(
    currentFilter: SearchFilter,
    onApplyFilter: (SearchFilter) -> Unit,
    onResetFilter: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(currentFilter.language) }
    var selectedContentType by remember { mutableStateOf(currentFilter.contentType) }
    var selectedVerification by remember { mutableStateOf(currentFilter.verificationFilter) }
    var selectedSortOption by remember { mutableStateOf(currentFilter.sortOption) }
    var onlyPrivateProjects by remember { mutableStateOf(currentFilter.onlyPrivateProjects) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("search_filter_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "خيارات التصفية",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "تصفية وفرز نتائج البحث",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.close))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 1. Sort Option
            Text(
                text = "ترتيب النتائج حسب:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchSortOption.values().forEach { sort ->
                    FilterChip(
                        selected = selectedSortOption == sort,
                        onClick = { selectedSortOption = sort },
                        label = { Text(sort.titleArabic) },
                        leadingIcon = if (selectedSortOption == sort) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. Language Filter
            Text(
                text = "اللغة:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    SearchLanguage.ALL,
                    SearchLanguage.ARABIC,
                    SearchLanguage.ENGLISH,
                    SearchLanguage.URDU
                ).forEach { lang ->
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = { selectedLanguage = lang },
                        label = { Text(lang.titleArabic) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. Verification Filter
            Text(
                text = "حالة الاعتماد والتوثيق:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchVerificationFilter.values().forEach { ver ->
                    FilterChip(
                        selected = selectedVerification == ver,
                        onClick = { selectedVerification = ver },
                        label = { Text(ver.titleArabic) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. Content Type Filter
            Text(
                text = "نوع المحتوى:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    SearchContentType.ALL,
                    SearchContentType.TEXT,
                    SearchContentType.AUDIO,
                    SearchContentType.VIDEO,
                    SearchContentType.TEMPLATE,
                    SearchContentType.REFERENCE
                ).chunked(3).forEach { rowList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowList.forEach { cType ->
                            FilterChip(
                                selected = selectedContentType == cType,
                                onClick = { selectedContentType = cType },
                                label = { Text(cType.titleArabic) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 5. Private Projects Only Switch
            SirajTechCard(
                isActive = onlyPrivateProjects,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "البحث في مشاريعي الخاصة فقط",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "قصر نتائج البحث على مشاريعك وسيناريوهاتك في مساحة العمل",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = onlyPrivateProjects,
                        onCheckedChange = { onlyPrivateProjects = it }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onResetFilter()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("إعادة ضبط")
                }

                Button(
                    onClick = {
                        val newFilter = currentFilter.copy(
                            language = selectedLanguage,
                            contentType = selectedContentType,
                            verificationFilter = selectedVerification,
                            sortOption = selectedSortOption,
                            onlyPrivateProjects = onlyPrivateProjects
                        )
                        onApplyFilter(newFilter)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                ) {
                    Text("تطبيق الفلاتر")
                }
            }
        }
    }
}
