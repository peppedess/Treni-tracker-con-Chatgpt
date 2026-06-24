package com.treni.tracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.treni.tracker.ui.theme.LocalTreniExtraColors

/**
 * Riga del tabellone: orario, numero treno, stazione (destinazione o
 * provenienza secondo il contesto), binario e ritardo. Usata sia per
 * le partenze che per gli arrivi.
 */
@Composable
fun RigaTabellone(
    orario: String,
    numeroTreno: String,
    stazioneAltroCapo: String,
    binario: String?,
    ritardo: Int?,
    modifier: Modifier = Modifier
) {
    val extraColors = LocalTreniExtraColors.current

    val coloreRitardo = when {
        ritardo == null -> MaterialTheme.colorScheme.onSurfaceVariant
        ritardo > 0 -> extraColors.statusLate
        ritardo < 0 -> extraColors.statusEarly
        else -> extraColors.statusOntime
    }
    val testoRitardo = when {
        ritardo == null -> ""
        ritardo > 0 -> "+$ritardo"
        ritardo < 0 -> "$ritardo"
        else -> "puntuale"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = orario,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = 12.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Treno $numeroTreno",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stazioneAltroCapo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        binario?.let {
            Text(
                text = "Bin. $it",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        if (testoRitardo.isNotEmpty()) {
            Text(
                text = testoRitardo,
                style = MaterialTheme.typography.labelLarge,
                color = coloreRitardo
            )
        }
    }
}
