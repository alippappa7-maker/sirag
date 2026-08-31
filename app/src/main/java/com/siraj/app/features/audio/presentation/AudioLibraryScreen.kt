package com.siraj.app.features.audio.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siraj.app.core.ui.components.EmptyScreen
import com.siraj.app.core.ui.components.SirajAudioCard
import com.siraj.app.mock.MockData

@Composable
fun AudioLibraryScreen() {
    if (MockData.audios.isEmpty()) {
        EmptyScreen(message = "المكتبة الصوتية فارغة حالياً.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(MockData.audios) { audio ->
                SirajAudioCard(audio = audio)
            }
        }
    }
}
