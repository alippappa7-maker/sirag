package com.siraj.app.features.details.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siraj.app.core.ui.components.EmptyScreen
import com.siraj.app.core.ui.components.SirajSourceCard
import com.siraj.app.core.ui.components.SkeletonList
import com.siraj.app.domain.models.SourcePreview

@Composable
fun DetailsScreen(
    id: String,
    sources: List<SourcePreview> = emptyList(),
) {
    val isLoading = id.isEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "تفاصيل المصادر (المحتوى: $id)",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            item {
                SkeletonList(itemCount = 3)
            }
        } else if (sources.isEmpty()) {
            item {
                EmptyScreen(message = "لا توجد مصادر مرفقة حالياً لهذا العنصر.")
            }
        } else {
            items(sources) { source ->
                SirajSourceCard(source = source)
            }
        }
    }
}
