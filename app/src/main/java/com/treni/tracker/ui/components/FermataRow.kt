package com.treni.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.treni.tracker.network.StopInfo
import com.treni.tracker.ui.theme.LocalTreniExtraColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatoOra = SimpleDateFormat("HH:mm", Locale.ITALY)

private fun formattaOra(millis: Long?): String? = millis?.let { formatoOra.format(Date(it)) }

/**
 * Singola fermata nella timeline verticale del dettaglio treno:
 * un pallino colorato per lo stato, collegato da una linea alla fermata
 * successiva, nome stazione, ritardo, e orari programmato/effettivo di
 * arrivo e partenza (quando disponibili).
 */
@Composable
fun FermataRow(
    fermata: StopInfo,
    isUltima: Boolean,
    modifier: Modifier = Modifier
) {
    val extraColors = LocalTreniExtraColors.current
    val ritardo = fermata.ritardo

    val testoRitardo = when {
        !fermata.passata -> "In attesa"
        ritardo == null -> "—"
        ritardo > 0 -> "+$ritardo min"
        ritardo < 0 -> "$ritardo min"
        else -> "In orario"
    }

    val colore: Color = when {
        !fermata.passata -> extraColors.statusPendingGrey
        ritardo == null -> extraColors.statusPendingGrey
        ritardo > 0 -> extraColors.statusLate
        ritardo < 0 -> extraColors.statusEarly
        else -> extraColors.statusOntime
    }

    val arrivoTeoricoTxt = formattaOra(fermata.arrivoTeorico)
    val arrivoEffettivoTxt = formattaOra(fermata.arrivoEffettivo)
    val partenzaTeoricaTxt = formattaOra(fermata.partenzaTeorica)
    val partenzaEffettivaTxt = formattaOra(fermata.partenzaEffettiva)

    Row(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.width(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(colore)
            )
            if (!isUltima) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .width(2.dp)
                        .height(if (arrivoTeoricoTxt != null || partenzaTeoricaTxt != null) 64.dp else 40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fermata.stazione,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = testoRitardo,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (arrivoTeoricoTxt != null) {
                RigaOrario(
                    etichetta = "Arrivo",
                    teorico = arrivoTeoricoTxt,
                    effettivo = arrivoEffettivoTxt
                )
            }
            if (partenzaTeoricaTxt != null) {
                RigaOrario(
                    etichetta = "Partenza",
                    teorico = partenzaTeoricaTxt,
                    effettivo = partenzaEffettivaTxt
                )
            }
        }
    }
}

@Composable
private fun RigaOrario(etichetta: String, teorico: String, effettivo: String?) {
    Row(modifier = Modifier.padding(top = 2.dp)) {
        Text(
            text = "$etichetta programmato $teorico",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (effettivo != null && effettivo != teorico) {
            Text(
                text = "  →  $effettivo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
