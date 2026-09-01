package com.siraj.app.features.onboarding.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.siraj.app.core.ui.components.SirajButton
import com.siraj.app.ui.theme.spacing

@Composable
fun OnboardingScreen(onNavigateToLogin: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        // Techno-spiritual background glow
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1500)),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            Color.Transparent,
                                        ),
                                    radius = 800f,
                                ),
                        ),
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter =
                fadeIn(animationSpec = tween(1000)) +
                    slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = tween(1000),
                    ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.extraLarge),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "مرحباً بك في سراج",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                Text(
                    text = "المنصة الأولى لصناعة ونشر المحتوى الإسلامي العربي الموثق.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                // رسالة ترحيب روحانية منسوبة إلى أبي عبيدة بن الجراح رضي الله عنه
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.medium,
                            )
                            .padding(MaterialTheme.spacing.large),
                ) {
                    Text(
                        text = "مالي أراكَ حزيناً؟ أأحُرمتَ الجنّةَ أم بُشِّرتَ بالنّارِ؟ هوِّنْ عليكَ، فما هيَ إلا دنيا؛ إنّما هيَ أيّامٌ ونمضي، فلا تحزن.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Text(
                        text = "— منسوبة إلى أبي عبيدة بن الجرّاح رضي الله عنه، مواسياً عمر بن الخطّاب رضي الله عنه",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

                // Using SirajButton directly as it should pick up primary color scheme
                SirajButton(
                    text = "ابدأ الآن",
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth(0.8f),
                )
            }
        }
    }
}
