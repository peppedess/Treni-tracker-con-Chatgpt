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
import androidx.compose.ui.unit.dp
import com.treni.tracker.network.StopInfo
import com.treni.tracker.ui.theme.LocalTreniExtraColors

/**
 * Singola fermata nella timeline verticale del dettaglio treno:
 * un pallino colorato per lo stato, collegato da una linea alla fermata
 * successiva, nome stazione e ritardo a fianco.
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
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, bottom = 20.dp),
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
    }
}
