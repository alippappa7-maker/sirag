package com.siraj.app.features.details.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siraj.app.core.ui.components.SirajSourceCard
import com.siraj.app.core.ui.components.SkeletonList
import com.siraj.app.mock.MockData

@Composable
fun DetailsScreen(id: String) {
    val isLoading = id.isEmpty() // Fake loading check

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
        } else {
            items(MockData.sources) { source ->
                SirajSourceCard(source = source)
            }
        }
    }
}
