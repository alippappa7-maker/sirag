package com.siraj.app.features.auth.presentation

import androidx.compose.foundation.clickable
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

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory())
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(actionState) {
        if (actionState is Resource.Success) {
            viewModel.resetActionState()
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "تسجيل الدخول", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        SirajTextField(value = email, onValueChange = { email = it }, label = "البريد الإلكتروني")
        Spacer(modifier = Modifier.height(16.dp))
        SirajTextField(value = password, onValueChange = { password = it }, label = "كلمة المرور")
        
        if (actionState is Resource.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (actionState as Resource.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (actionState is Resource.Loading) {
            CircularProgressIndicator()
        } else {
            SirajButton(text = "دخول", onClick = { viewModel.login(email, password) })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ليس لديك حساب؟ تسجيل جديد",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onNavigateToRegister() }
        )
    }
}
