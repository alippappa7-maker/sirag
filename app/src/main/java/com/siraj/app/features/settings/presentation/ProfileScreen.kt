package com.siraj.app.features.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.ui.components.SirajButton
import com.siraj.app.core.ui.components.SirajTextField
import com.siraj.app.core.utils.Resource
import com.siraj.app.features.auth.presentation.AuthViewModel
import com.siraj.app.features.auth.presentation.AuthViewModelFactory



@Composable
fun ProfileScreen(
    onLogoutSuccess: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory())
) {
    val authState by viewModel.authState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    
    val user = (authState as? Resource.Success)?.data

    var name by remember { mutableStateOf("") }
    
    LaunchedEffect(user) {
        if (user != null && name.isEmpty()) {
            name = user.name
        }
    }

    LaunchedEffect(actionState) {
        if (actionState is Resource.Success) {
            viewModel.resetActionState()
            // Using side effect to clear state on logout is tricky, so checking user is safer
        }
    }
    
    LaunchedEffect(authState) {
        if (authState is Resource.Success && (authState as Resource.Success).data == null) {
            onLogoutSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "الملف الشخصي", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (user != null) {
            SirajTextField(value = name, onValueChange = { name = it }, label = "الاسم")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "الدور: ${user.role.name}", style = MaterialTheme.typography.bodyMedium)
            
            if (actionState is Resource.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (actionState as Resource.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            if (actionState is Resource.Loading) {
                CircularProgressIndicator()
            } else {
                SirajButton(text = "حفظ التغييرات", onClick = { viewModel.updateProfile(name) })
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            if (user.role.name == "ADMIN" || user.role.name == "OWNER") {
                SirajButton(text = "لوحة الإدارة", onClick = onNavigateToAdmin)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            SirajButton(text = "تسجيل الخروج", onClick = { viewModel.logout() })
        } else {
            CircularProgressIndicator()
        }
    }
}
