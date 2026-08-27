with open('app/src/main/java/com/siraj/app/features/settings/presentation/ProfileScreen.kt', 'r') as f:
    content = f.read()

import re
content = re.sub(r'^.*?package com.siraj.app.features.settings.presentation', 'package com.siraj.app.features.settings.presentation', content, flags=re.DOTALL)

imports = """
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
"""

content = content.replace("package com.siraj.app.features.settings.presentation", "package com.siraj.app.features.settings.presentation\n" + imports)

with open('app/src/main/java/com/siraj/app/features/settings/presentation/ProfileScreen.kt', 'w') as f:
    f.write(content)
