package com.treni.tracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.treni.tracker.util.ThemeManager

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    error = LightError,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    error = DarkError,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

/**
 * Tema principale dell'app. Applica dynamic color su Android 12+
 * (stesso comportamento di DynamicColors.applyToActivitiesIfAvailable
 * usato nella versione View), con fallback sulla palette statica
 * sulle versioni precedenti o se l'utente disattiva il colore dinamico.
 */
@Composable
fun TreniTrackerTheme(
    darkTheme: Boolean = temaSceltoEScuro(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(LocalTreniExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TreniTypography,
            shapes = TreniShapes,
            content = content
        )
    }
}

/**
 * Determina se il tema attivo è scuro, in base alla scelta salvata
 * dall'utente (Sistema/Chiaro/Scuro) in ThemeManager — non solo dal
 * tema di sistema, così la preferenza esplicita viene sempre rispettata.
 */
@Composable
private fun temaSceltoEScuro(): Boolean {
    val context = LocalContext.current
    val tema = ThemeManager.leggiTema(context)
    return when (tema) {
        ThemeManager.TEMA_CHIARO -> false
        ThemeManager.TEMA_SCURO -> true
        else -> isSystemInDarkTheme()
    }
}
