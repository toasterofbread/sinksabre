package dev.toastbits.sinksabre.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colour_scheme: ColorScheme =
        darkColorScheme().copy(
            // primary = accent,
            // onPrimary = on_accent,
            // inversePrimary = vibrant_accent,
            // secondary = on_background,
            // onSecondary = background,
            // tertiary = vibrant_accent,
            // onTertiary = vibrant_accent.getContrasted(),

            // primaryContainer = primary_container,
            // onPrimaryContainer = primary_container.getContrasted(),
            // secondaryContainer = secondary_container,
            // onSecondaryContainer = secondary_container.getContrasted(),
            // tertiaryContainer = tertiary_container,
            // onTertiaryContainer = tertiary_container.getContrasted(),

            // background = background,
            // onBackground = on_background,

            // surface = background.amplifyPercent(0.1f),
            // onSurface = on_background,
            // surfaceVariant = background.amplifyPercent(0.2f),
            // onSurfaceVariant = on_background,
            // surfaceTint = accent.blendWith(background, 0.75f),
            // inverseSurface = vibrant_accent,
            // inverseOnSurface = vibrant_accent.getContrasted(),

            // outline = on_background,
            // outlineVariant = vibrant_accent
        )

    val default_typography: Typography = MaterialTheme.typography
    val font_family: FontFamily = FontFamily.Default

    val typography: Typography = remember(default_typography) {
        with(default_typography) {
            copy(
                displayLarge = displayLarge.copy(fontFamily = font_family),
                displayMedium = displayMedium.copy(fontFamily = font_family),
                displaySmall = displaySmall.copy(fontFamily = font_family),
                headlineLarge = headlineLarge.copy(fontFamily = font_family),
                headlineMedium = headlineMedium.copy(fontFamily = font_family),
                headlineSmall = headlineSmall.copy(fontFamily = font_family),
                titleLarge = titleLarge.copy(fontFamily = font_family),
                titleMedium = titleMedium.copy(fontFamily = font_family),
                titleSmall = titleSmall.copy(fontFamily = font_family),
                bodyLarge = bodyLarge.copy(fontFamily = font_family),
                bodyMedium = bodyMedium.copy(fontFamily = font_family),
                bodySmall = bodySmall.copy(fontFamily = font_family),
                labelLarge = labelLarge.copy(fontFamily = font_family),
                labelMedium = labelMedium.copy(fontFamily = font_family),
                labelSmall = labelSmall.copy(fontFamily = font_family)
            )
        }
    }

    MaterialTheme(
        colorScheme = colour_scheme,
        typography = typography
    ) {
        content()
    }
}
