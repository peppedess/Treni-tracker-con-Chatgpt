package com.treni.tracker.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        shadowElevation = 3.dp,
        color = Color.Transparent,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(stato.gradStart, stato.gradEnd)))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = treno.numeroTreno,
                    style = MaterialTheme.typography.titleLarge,
                    color = stato.onColor,
                    modifier = Modifier.padding(end = 8.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stato.testoStato,
                    style = MaterialTheme.typography.titleMedium,
                    color = stato.onColor
                )
                IconButton(onClick = onRimuovi) {
                    Icon(Icons.Filled.Close, contentDescription = "Rimuovi", tint = stato.onColor)
                }
            }

            Text(
                text = "${treno.stazionePartenzaNome} → ${treno.stazioneDestinazioneNome ?: "?"}",
                style = MaterialTheme.typography.bodyMedium,
                color = stato.onColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = treno.ultimaStazioneNotificata?.let { "Ultima fermata rilevata: $it" }
                    ?: "In attesa del primo aggiornamento…",
                style = MaterialTheme.typography.labelMedium,
                color = stato.onColor.copy(alpha = 0.8f),
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
                    color = stato.onColor,
                    trackColor = stato.onColor.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
