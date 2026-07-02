package com.treni.tracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.treni.tracker.data.TrenoMonitorato
import com.treni.tracker.ui.theme.LocalTreniExtraColors

private data class StatoVisivo(
    val gradStart: Color,
    val gradEnd: Color,
    val onColor: Color,
    val testoStato: String
)

@Composable
private fun statoVisivoPer(ritardo: Int?): StatoVisivo {
    val colors = LocalTreniExtraColors.current
    return when {
        ritardo == null -> StatoVisivo(colors.gradPendingStart, colors.gradPendingEnd, colors.cardPendingOn, "In attesa")
        ritardo > 0 -> StatoVisivo(colors.gradLateStart, colors.gradLateEnd, colors.cardLateOn, "+$ritardo min")
        ritardo < 0 -> StatoVisivo(colors.gradEarlyStart, colors.gradEarlyEnd, colors.cardEarlyOn, "$ritardo min")
        else -> StatoVisivo(colors.gradOntimeStart, colors.gradOntimeEnd, colors.cardOntimeOn, "In orario")
    }
}

/**
 * Card di un treno monitorato: sfondo gradient pieno coordinato allo stato
 * (ritardo/anticipo/in orario/in attesa), numero treno, tratta, stato compatto,
 * e barra di avanzamento del percorso se i dati sono disponibili.
 *
 * Tocchi espressivi:
 * - "squish" elastico alla pressione (scala con molla)
 * - i colori del gradiente sfumano dolcemente quando lo stato cambia
 *   (es. da "in orario" a ritardo) invece di scattare
 * - il testo dello stato scorre verso l'alto come un contatore quando cambia
 */
@Composable
fun TrenoCard(
    treno: TrenoMonitorato,
    onClick: () -> Unit,
    onRimuovi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stato = statoVisivoPer(treno.ultimoRitardo)
    val shape = RoundedCornerShape(28.dp)

    // Transizione morbida dei colori quando lo stato del treno cambia
    val mollaColore = spring<Color>(stiffness = Spring.StiffnessLow)
    val gradStart by animateColorAsState(stato.gradStart, mollaColore, label = "gradStart")
    val gradEnd by animateColorAsState(stato.gradEnd, mollaColore, label = "gradEnd")
    val coloreTesto by animateColorAsState(stato.onColor, mollaColore, label = "onColor")

    // Squish elastico alla pressione
    val interactionSource = remember { MutableInteractionSource() }
    val premuta by interactionSource.collectIsPressedAsState()
    val scala by animateFloatAsState(
        targetValue = if (premuta) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scalaCard"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scala
                scaleY = scala
            },
        shape = shape,
        shadowElevation = 3.dp,
        color = Color.Transparent,
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(gradStart, gradEnd)))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = treno.numeroTreno,
                    style = MaterialTheme.typography.titleLarge,
                    color = coloreTesto,
                    modifier = Modifier.padding(end = 8.dp)
                )
                treno.categoria?.takeIf { it.isNotBlank() }?.let { cat ->
                    androidx.compose.material3.Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        color = coloreTesto.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium,
                            color = coloreTesto,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                // Il testo dello stato scorre come un contatore quando cambia
                AnimatedContent(
                    targetState = stato.testoStato,
                    transitionSpec = {
                        (fadeIn() + slideInVertically { altezza -> altezza / 2 }) togetherWith
                            (fadeOut() + slideOutVertically { altezza -> -altezza / 2 })
                    },
                    label = "testoStato"
                ) { testo ->
                    Text(
                        text = testo,
                        style = MaterialTheme.typography.titleMedium,
                        color = coloreTesto
                    )
                }
                IconButton(onClick = onRimuovi) {
                    Icon(Icons.Filled.Close, contentDescription = "Rimuovi", tint = coloreTesto)
                }
            }

            Text(
                text = "${treno.stazionePartenzaNome} → ${treno.stazioneDestinazioneNome ?: "?"}",
                style = MaterialTheme.typography.bodyMedium,
                color = coloreTesto.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = treno.ultimaStazioneNotificata?.let { "Ultima fermata rilevata: $it" }
                    ?: "In attesa del primo aggiornamento…",
                style = MaterialTheme.typography.labelMedium,
                color = coloreTesto.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 10.dp)
            )

            val totale = treno.numeroFermateTotali
            val indice = treno.indiceFermataCorrente
            if (totale != null && totale > 0 && indice != null) {
                val progressoAnimato by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = indice.toFloat() / totale.toFloat(),
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 600),
                    label = "progressoPercorso"
                )
                LinearProgressIndicator(
                    progress = { progressoAnimato },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = coloreTesto,
                    trackColor = coloreTesto.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
