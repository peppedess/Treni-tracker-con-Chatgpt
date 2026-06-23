package com.treni.tracker.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colori "extra" non previsti dal ColorScheme standard di Material3:
 * le card piene per stato treno, i gradient di superficie, e gli
 * sfondi dedicati per schermata (Home/Dettaglio/Tratta).
 *
 * Esposti tramite CompositionLocal così ogni composable può leggerli
 * con LocalTreniExtraColors.current, in modo simile a MaterialTheme.colorScheme.
 */
data class TreniExtraColors(
    val cardLateBg: Color,
    val cardLateOn: Color,
    val cardOntimeBg: Color,
    val cardOntimeOn: Color,
    val cardEarlyBg: Color,
    val cardEarlyOn: Color,
    val cardPendingBg: Color,
    val cardPendingOn: Color,
    val statusOntime: Color,
    val statusLate: Color,
    val statusEarly: Color,
    val statusPendingGrey: Color,
    val gradPrimaryStart: Color,
    val gradPrimaryEnd: Color,
    val gradLateStart: Color,
    val gradLateEnd: Color,
    val gradOntimeStart: Color,
    val gradOntimeEnd: Color,
    val gradEarlyStart: Color,
    val gradEarlyEnd: Color,
    val gradPendingStart: Color,
    val gradPendingEnd: Color,
    val surfaceHome: Color,
    val surfaceDetail: Color,
    val surfaceTratta: Color
)

val LightExtraColors = TreniExtraColors(
    cardLateBg = LightCardLateBg, cardLateOn = LightCardLateOn,
    cardOntimeBg = LightCardOntimeBg, cardOntimeOn = LightCardOntimeOn,
    cardEarlyBg = LightCardEarlyBg, cardEarlyOn = LightCardEarlyOn,
    cardPendingBg = LightCardPendingBg, cardPendingOn = LightCardPendingOn,
    statusOntime = LightStatusOntime, statusLate = LightStatusLate,
    statusEarly = LightStatusEarly, statusPendingGrey = LightStatusPendingGrey,
    gradPrimaryStart = LightGradPrimaryStart, gradPrimaryEnd = LightGradPrimaryEnd,
    gradLateStart = LightGradLateStart, gradLateEnd = LightGradLateEnd,
    gradOntimeStart = LightGradOntimeStart, gradOntimeEnd = LightGradOntimeEnd,
    gradEarlyStart = LightGradEarlyStart, gradEarlyEnd = LightGradEarlyEnd,
    gradPendingStart = LightGradPendingStart, gradPendingEnd = LightGradPendingEnd,
    surfaceHome = LightSurfaceHome, surfaceDetail = LightSurfaceDetail, surfaceTratta = LightSurfaceTratta
)

val DarkExtraColors = TreniExtraColors(
    cardLateBg = DarkCardLateBg, cardLateOn = DarkCardLateOn,
    cardOntimeBg = DarkCardOntimeBg, cardOntimeOn = DarkCardOntimeOn,
    cardEarlyBg = DarkCardEarlyBg, cardEarlyOn = DarkCardEarlyOn,
    cardPendingBg = DarkCardPendingBg, cardPendingOn = DarkCardPendingOn,
    statusOntime = DarkStatusOntime, statusLate = DarkStatusLate,
    statusEarly = DarkStatusEarly, statusPendingGrey = DarkStatusPendingGrey,
    gradPrimaryStart = DarkGradPrimaryStart, gradPrimaryEnd = DarkGradPrimaryEnd,
    gradLateStart = DarkGradLateStart, gradLateEnd = DarkGradLateEnd,
    gradOntimeStart = DarkGradOntimeStart, gradOntimeEnd = DarkGradOntimeEnd,
    gradEarlyStart = DarkGradEarlyStart, gradEarlyEnd = DarkGradEarlyEnd,
    gradPendingStart = DarkGradPendingStart, gradPendingEnd = DarkGradPendingEnd,
    surfaceHome = DarkSurfaceHome, surfaceDetail = DarkSurfaceDetail, surfaceTratta = DarkSurfaceTratta
)

val LocalTreniExtraColors = staticCompositionLocalOf { LightExtraColors }
