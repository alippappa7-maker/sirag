package com.siraj.app.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.siraj.app.core.accessibility.AccessibilitySemantics.sirajTouchTarget
import com.siraj.app.core.accessibility.LocalAccessibilityConfig
import com.siraj.app.ui.theme.MyApplicationTheme

@Composable
fun SirajButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescriptionText: String? = null,
) {
    val a11yConfig = LocalAccessibilityConfig.current
    val isHighContrast = a11yConfig.highContrastMode

    Button(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp) // Touch target minimum 48dp, scales flexibly
                .sirajTouchTarget()
                .semantics {
                    role = Role.Button
                    if (contentDescriptionText != null) {
                        contentDescription = contentDescriptionText
                    }
                },
        enabled = enabled,
        shape = MaterialTheme.shapes.small, // 8dp
        border =
            if (isHighContrast) {
                BorderStroke(
                    2.dp,
                    if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                )
            } else {
                null
            },
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SirajButtonPreview() {
    MyApplicationTheme {
        SirajButton(text = "اعتماد النص", onClick = {})
    }
}
