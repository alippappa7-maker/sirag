package com.siraj.app.features.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.WorkspaceMember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkspaceViewModel = viewModel(factory = WorkspaceViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    var inviteEmail by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات مساحة العمل") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            
            Text("المساحة الحالية: ${uiState.activeWorkspace?.name ?: "جاري التحميل..."}", style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = inviteEmail,
                onValueChange = { inviteEmail = it },
                label = { Text("البريد الإلكتروني لدعوة عضو") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = { 
                    viewModel.inviteMember(inviteEmail) 
                    inviteEmail = ""
                },
                modifier = Modifier.padding(top = 8.dp).align(Alignment.End),
                enabled = inviteEmail.isNotBlank()
            ) {
                Text("إرسال دعوة")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("الأعضاء", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            when (val membersRes = uiState.members) {
                is Resource.Loading -> CircularProgressIndicator()
                is Resource.Error -> Text(membersRes.message, color = MaterialTheme.colorScheme.error)
                is Resource.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(membersRes.data) { member ->
                            MemberCard(member = member) {
                                viewModel.removeMember(member.userId)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberCard(member: WorkspaceMember, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(member.userName, style = MaterialTheme.typography.bodyLarge)
                Text(member.userEmail, style = MaterialTheme.typography.bodyMedium)
                Text("الدور: ${member.role.name}", style = MaterialTheme.typography.labelSmall)
            }
            if (member.role.name != "OWNER") {
                TextButton(onClick = onRemove, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("إزالة")
                }
            }
        }
    }
}
