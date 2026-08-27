package com.siraj.app.features.flashes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siraj.app.core.ui.components.ErrorScreen
import com.siraj.app.core.ui.components.SirajFlashCard
import com.siraj.app.mock.MockData

@Composable
fun FlashesScreen() {
    // Show error state conditionally, here just using mock data directly
    val isError = false 

    if (isError) {
        ErrorScreen(
            message = "تعذر تحميل الومضات في الوقت الحالي.",
            onRetry = { /* Retry logic */ }
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(MockData.flashes) { flash ->
                SirajFlashCard(flash = flash)
            }
        }
    }
}
