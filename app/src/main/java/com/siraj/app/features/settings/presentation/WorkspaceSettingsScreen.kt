package com.siraj.app.features.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkspaceViewModel = viewModel(factory = WorkspaceViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.error) {
        // Here you could show a snackbar for uiState.error if not null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مساحات العمل") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "مساحة جديدة")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                if (uiState.error != null) {
                    Text(text = uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.clearError() }) { Text("حسناً") }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 1. Workspace Selector
                WorkspaceSelector(uiState = uiState, onSelect = viewModel::setActiveWorkspace)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 2. Invitations
                if (uiState.invitations.isNotEmpty()) {
                    Text("الدعوات", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(uiState.invitations) { inv ->
                            InvitationCard(invitation = inv, onRespond = viewModel::respondToInvitation)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 3. Active Workspace Management
                uiState.activeWorkspace?.let { workspace ->
                    Text("إدارة ${workspace.name}", style = MaterialTheme.typography.titleMedium)
                    Text("دورك: ${uiState.currentUserRole?.name ?: ""}", style = MaterialTheme.typography.bodySmall)
                    
                    if (workspace.status == "ARCHIVED") {
                        Text("مساحة العمل هذه مؤرشفة", color = MaterialTheme.colorScheme.error)
                    } else {
                        val canManage = uiState.currentUserRole == WorkspaceRole.OWNER || uiState.currentUserRole == WorkspaceRole.MANAGER
                        
                        if (canManage) {
                            Spacer(modifier = Modifier.height(16.dp))
                            InviteMemberForm(onInvite = viewModel::inviteMember)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("الأعضاء", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        when (val membersRes = uiState.members) {
                            is Resource.Loading -> CircularProgressIndicator()
                            is Resource.Error -> Text(membersRes.message, color = MaterialTheme.colorScheme.error)
                            is Resource.Success -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(membersRes.data) { member ->
                                        MemberCard(
                                            member = member,
                                            currentUserRole = uiState.currentUserRole,
                                            currentUser = uiState.currentUser,
                                            onRemove = { viewModel.removeMember(member.userId) },
                                            onUpdateRole = { newRole -> viewModel.updateMemberRole(member.userId, newRole) },
                                            onTransferOwnership = { viewModel.transferOwnership(member.userId) }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (uiState.currentUserRole != WorkspaceRole.OWNER) {
                                OutlinedButton(onClick = { viewModel.leaveWorkspace() }) {
                                    Text("مغادرة مساحة العمل", color = MaterialTheme.colorScheme.error)
                                }
                            } else if (workspace.type == WorkspaceType.TEAM) {
                                OutlinedButton(onClick = { viewModel.archiveWorkspace() }) {
                                    Text("أرشفة مساحة العمل", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateWorkspaceDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, type -> 
                viewModel.createWorkspace(name, type)
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSelector(uiState: WorkspaceUiState, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = uiState.activeWorkspace?.name ?: "جاري التحميل...",
            onValueChange = {},
            readOnly = true,
            label = { Text("المساحة الحالية") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            uiState.workspaces.forEach { ws ->
                DropdownMenuItem(
                    text = { Text("${ws.name} (${ws.type.name})") },
                    onClick = {
                        onSelect(ws.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun InviteMemberForm(onInvite: (String, WorkspaceRole) -> Unit) {
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(WorkspaceRole.VIEWER) }
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("دعوة عضو جديد", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("البريد الإلكتروني") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("الدور: ${role.name}")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    WorkspaceRole.values().forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r.name) },
                            onClick = { role = r; expanded = false }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { 
                    onInvite(email, role)
                    email = ""
                },
                modifier = Modifier.align(Alignment.End),
                enabled = email.isNotBlank()
            ) {
                Text("إرسال الدعوة")
            }
        }
    }
}

@Composable
fun MemberCard(
    member: WorkspaceMember, 
    currentUserRole: WorkspaceRole?,
    currentUser: UserProfile?,
    onRemove: () -> Unit,
    onUpdateRole: (WorkspaceRole) -> Unit,
    onTransferOwnership: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isSelf = member.userId == currentUser?.id
    val isOwner = member.role == WorkspaceRole.OWNER
    val canManageRoles = (currentUserRole == WorkspaceRole.OWNER || currentUserRole == WorkspaceRole.MANAGER) && !isOwner

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${member.userName} ${if(isSelf) "(أنت)" else ""}", style = MaterialTheme.typography.bodyLarge)
                Text(member.userEmail, style = MaterialTheme.typography.bodyMedium)
                Text("الدور: ${member.role.name}", style = MaterialTheme.typography.labelSmall)
            }
            
            if (canManageRoles || (currentUserRole == WorkspaceRole.OWNER && !isSelf)) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.options))
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        if (canManageRoles) {
                            WorkspaceRole.values().filter { it != WorkspaceRole.OWNER }.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text("تعيين كـ ${r.name}") },
                                    onClick = { onUpdateRole(r); expanded = false }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("إزالة العضو", color = MaterialTheme.colorScheme.error) },
                                onClick = { onRemove(); expanded = false }
                            )
                        }
                        if (currentUserRole == WorkspaceRole.OWNER && !isSelf) {
                            DropdownMenuItem(
                                text = { Text("نقل الملكية", color = MaterialTheme.colorScheme.error) },
                                onClick = { onTransferOwnership(); expanded = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvitationCard(invitation: WorkspaceInvitation, onRespond: (String, Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("دعوة للانضمام إلى مساحة عمل", style = MaterialTheme.typography.bodyMedium)
                Text("الدور: ${invitation.role.name}", style = MaterialTheme.typography.labelSmall)
            }
            Row {
                TextButton(onClick = { onRespond(invitation.id, false) }) {
                    Text("رفض", color = MaterialTheme.colorScheme.error)
                }
                Button(onClick = { onRespond(invitation.id, true) }) {
                    Text("قبول")
                }
            }
        }
    }
}

@Composable
fun CreateWorkspaceDialog(onDismiss: () -> Unit, onCreate: (String, WorkspaceType) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(WorkspaceType.TEAM) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء مساحة عمل") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المساحة") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = type == WorkspaceType.TEAM,
                        onClick = { type = WorkspaceType.TEAM }
                    )
                    Text("فريق (Team)")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = type == WorkspaceType.PERSONAL,
                        onClick = { type = WorkspaceType.PERSONAL }
                    )
                    Text("شخصي (Personal)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, type) },
                enabled = name.isNotBlank()
            ) { Text("إنشاء") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel)) }
        }
    )
}
