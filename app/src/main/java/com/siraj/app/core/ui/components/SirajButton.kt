package com.siraj.app.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.siraj.app.core.accessibility.AccessibilitySemantics.sirajTouchTarget
import com.siraj.app.core.accessibility.LocalAccessibilityConfig
import com.siraj.app.ui.theme.MyApplicationTheme

// Siraj Techno-Spiritual — Purple Button System
private val ButtonPurpleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
)

/**
 * Primary Siraj Button — Electric violet gradient
 */
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
                .heightIn(min = 48.dp)
                .sirajTouchTarget()
                .semantics {
                    role = Role.Button
                    if (contentDescriptionText != null) {
                        contentDescription = contentDescriptionText
                    }
                },
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        border =
            if (isHighContrast) {
                BorderStroke(
                    2.dp,
                    if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
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

/**
 * Secondary Siraj Button — Purple outline
 */
@Composable
fun SirajSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescriptionText: String? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .sirajTouchTarget()
                .semantics {
                    role = Role.Button
                    if (contentDescriptionText != null) {
                        contentDescription = contentDescriptionText
                    }
                },
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.5.dp,
            if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
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
