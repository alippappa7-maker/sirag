package com.siraj.app.features.quran.presentation

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.quran.Ayah
import com.siraj.app.domain.models.quran.Surah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    onNavigateToSurah: (Int, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: QuranViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuranViewModel(context.applicationContext as Application) as T
            }
        }
    )
    
    val surahsState by viewModel.surahs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("القرآن الكريم والتفاسير") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("بحث عن سورة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            when (val state = surahsState) {
                is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is Resource.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message, color = MaterialTheme.colorScheme.error) }
                is Resource.Success -> {
                    val filteredSurahs = state.data.filter { it.nameArabic.contains(searchQuery) }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredSurahs) { surah ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onNavigateToSurah(surah.id, surah.nameArabic) }
                            ) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(surah.nameArabic, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text("${surah.revelationPlace} • ${surah.versesCount} آيات", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(surah.id.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "المصدر: Quran.com API - واجهة قرآنية عامة",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahScreen(
    surahId: Int,
    surahName: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SurahViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SurahViewModel(context.applicationContext as Application, surahId) as T
            }
        }
    )
    
    val ayahsState by viewModel.ayahsWithBookmarks.collectAsState(initial = Resource.Loading)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سورة $surahName") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = ayahsState) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is Resource.Success -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(state.data) { ayah ->
                            AyahCard(
                                ayah = ayah,
                                onToggleBookmark = { viewModel.toggleBookmark(ayah.verseKey, ayah.verseKey.split(":")[1].toInt(), ayah.isBookmarked) },
                                onSaveNote = { note -> viewModel.saveNote(ayah.verseKey, ayah.verseKey.split(":")[1].toInt(), note) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AyahCard(
    ayah: Ayah,
    onToggleBookmark: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showNoteDialog by remember { mutableStateOf(false) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = { Text(ayah.verseKey) })
                Row {
                    IconButton(onClick = { /* TODO Audio Player Integration */ }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "تلاوة")
                    }
                    IconButton(onClick = { showNoteDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "ملاحظات")
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            if (ayah.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "علامة مرجعية",
                            tint = if (ayah.isBookmarked) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Arabic Text
            Text(
                text = ayah.textUthmani,
                style = MaterialTheme.typography.headlineMedium.copy(lineHeight = 42.sp),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("التفسير (الميسر)") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Translation") })
                if (ayah.note != null) {
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("ملاحظاتي") })
                }
            }
            
            Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                when (selectedTab) {
                    0 -> Text(ayah.tafsir ?: "لا يوجد تفسير متاح", style = MaterialTheme.typography.bodyLarge)
                    1 -> Text(ayah.translation ?: "No translation available", style = MaterialTheme.typography.bodyLarge)
                    2 -> Text(ayah.note ?: "", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
    
    if (showNoteDialog) {
        var noteText by remember { mutableStateOf(ayah.note ?: "") }
        Dialog(onDismissRequest = { showNoteDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ملاحظة خاصة للآية ${ayah.verseKey}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("اكتب ملاحظاتك أو خواطرك هنا...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showNoteDialog = false }) { Text("إلغاء") }
                        Button(onClick = { 
                            onSaveNote(noteText)
                            showNoteDialog = false
                        }) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }
}
