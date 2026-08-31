package com.siraj.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val processing: Color,
    val onProcessing: Color,
)

@Immutable
data class StatusColors(
    val successBg: Color,
    val successFg: Color,
    val warningBg: Color,
    val warningFg: Color,
    val errorBg: Color,
    val errorFg: Color,
    val infoBg: Color,
    val infoFg: Color,
    val neutralBg: Color,
    val neutralFg: Color,
    val draftBg: Color,
    val draftFg: Color,
)

val LocalExtendedColors =
    staticCompositionLocalOf {
        ExtendedColors(
            success = Color.Unspecified,
            onSuccess = Color.Unspecified,
            warning = Color.Unspecified,
            onWarning = Color.Unspecified,
            processing = Color.Unspecified,
            onProcessing = Color.Unspecified,
        )
    }

val LocalStatusColors =
    staticCompositionLocalOf {
        StatusColors(
            successBg = Color.Unspecified,
            successFg = Color.Unspecified,
            warningBg = Color.Unspecified,
            warningFg = Color.Unspecified,
            errorBg = Color.Unspecified,
            errorFg = Color.Unspecified,
            infoBg = Color.Unspecified,
            infoFg = Color.Unspecified,
            neutralBg = Color.Unspecified,
            neutralFg = Color.Unspecified,
            draftBg = Color.Unspecified,
            draftFg = Color.Unspecified,
        )
    }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current

val MaterialTheme.statusColors: StatusColors
    @Composable
    get() = LocalStatusColors.current

private val DarkColorScheme =
    darkColorScheme(
        primary = md_theme_dark_primary,
        onPrimary = md_theme_dark_onPrimary,
        primaryContainer = md_theme_dark_primaryContainer,
        onPrimaryContainer = md_theme_dark_onPrimaryContainer,
        secondary = md_theme_dark_secondary,
        onSecondary = md_theme_dark_onSecondary,
        secondaryContainer = md_theme_dark_secondaryContainer,
        onSecondaryContainer = md_theme_dark_onSecondaryContainer,
        tertiary = md_theme_dark_tertiary,
        onTertiary = md_theme_dark_onTertiary,
        tertiaryContainer = md_theme_dark_tertiaryContainer,
        onTertiaryContainer = md_theme_dark_onTertiaryContainer,
        error = md_theme_dark_error,
        errorContainer = md_theme_dark_errorContainer,
        onError = md_theme_dark_onError,
        onErrorContainer = md_theme_dark_onErrorContainer,
        background = md_theme_dark_background,
        onBackground = md_theme_dark_onBackground,
        surface = md_theme_dark_surface,
        onSurface = md_theme_dark_onSurface,
        surfaceVariant = md_theme_dark_surfaceVariant,
        onSurfaceVariant = md_theme_dark_onSurfaceVariant,
        outline = md_theme_dark_outline,
        surfaceTint = md_theme_dark_surfaceTint,
        inverseSurface = md_theme_dark_inverseSurface,
        inverseOnSurface = md_theme_dark_inverseOnSurface,
        inversePrimary = md_theme_dark_inversePrimary,
        scrim = md_theme_dark_scrim,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = md_theme_light_primary,
        onPrimary = md_theme_light_onPrimary,
        primaryContainer = md_theme_light_primaryContainer,
        onPrimaryContainer = md_theme_light_onPrimaryContainer,
        secondary = md_theme_light_secondary,
        onSecondary = md_theme_light_onSecondary,
        secondaryContainer = md_theme_light_secondaryContainer,
        onSecondaryContainer = md_theme_light_onSecondaryContainer,
        tertiary = md_theme_light_tertiary,
        onTertiary = md_theme_light_onTertiary,
        tertiaryContainer = md_theme_light_tertiaryContainer,
        onTertiaryContainer = md_theme_light_onTertiaryContainer,
        error = md_theme_light_error,
        errorContainer = md_theme_light_errorContainer,
        onError = md_theme_light_onError,
        onErrorContainer = md_theme_light_onErrorContainer,
        background = md_theme_light_background,
        onBackground = md_theme_light_onBackground,
        surface = md_theme_light_surface,
        onSurface = md_theme_light_onSurface,
        surfaceVariant = md_theme_light_surfaceVariant,
        onSurfaceVariant = md_theme_light_onSurfaceVariant,
        outline = md_theme_light_outline,
        surfaceTint = md_theme_light_surfaceTint,
        inverseSurface = md_theme_light_inverseSurface,
        inverseOnSurface = md_theme_light_inverseOnSurface,
        inversePrimary = md_theme_light_inversePrimary,
        scrim = md_theme_light_scrim,
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    fontScaleMultiplier: Float = 1.0f,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val scaledTypography = getScaledTypography(fontScaleMultiplier)

    val extendedColors =
        if (darkTheme) {
            ExtendedColors(
                success = siraj_dark_success,
                onSuccess = siraj_dark_onSuccess,
                warning = siraj_dark_warning,
                onWarning = siraj_dark_onWarning,
                processing = siraj_dark_processing,
                onProcessing = siraj_dark_onProcessing,
            )
        } else {
            ExtendedColors(
                success = siraj_light_success,
                onSuccess = siraj_light_onSuccess,
                warning = siraj_light_warning,
                onWarning = siraj_light_onWarning,
                processing = siraj_light_processing,
                onProcessing = siraj_light_onProcessing,
            )
        }

    val statusColors =
        if (darkTheme) {
            StatusColors(
                successBg = StatusSuccessBgDark,
                successFg = StatusSuccessFgDark,
                warningBg = StatusWarningBgDark,
                warningFg = StatusWarningFgDark,
                errorBg = StatusErrorBgDark,
                errorFg = StatusErrorFgDark,
                infoBg = StatusInfoBgDark,
                infoFg = StatusInfoFgDark,
                neutralBg = StatusNeutralBgDark,
                neutralFg = StatusNeutralFgDark,
                draftBg = StatusDraftBgDark,
                draftFg = StatusDraftFgDark,
            )
        } else {
            StatusColors(
                successBg = StatusSuccessBgLight,
                successFg = StatusSuccessFgLight,
                warningBg = StatusWarningBgLight,
                warningFg = StatusWarningFgLight,
                errorBg = StatusErrorBgLight,
                errorFg = StatusErrorFgLight,
                infoBg = StatusInfoBgLight,
                infoFg = StatusInfoFgLight,
                neutralBg = StatusNeutralBgLight,
                neutralFg = StatusNeutralFgLight,
                draftBg = StatusDraftBgLight,
                draftFg = StatusDraftFgLight,
            )
        }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalExtendedColors provides extendedColors,
        LocalStatusColors provides statusColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            shapes = SirajShapes,
            content = content,
        )
    }
}
