package com.treni.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.treni.tracker.ui.StatisticheTreno
import com.treni.tracker.ui.theme.LocalTreniExtraColors

/**
 * Card statistiche storico ritardi: tre metriche affiancate con numero
 * grande in evidenza e etichetta sotto, invece di un blocco di testo
 * concatenato. Il colore della percentuale di ritardo segue la stessa
 * semantica delle card treno (verde/giallo/rosso secondo gravità).
 */
@Composable
fun CardStoricoRitardi(
    statistiche: StatisticheTreno,
    modifier: Modifier = Modifier
) {
    val extraColors = LocalTreniExtraColors.current

    val coloreRitardo: Color = when {
        statistiche.percentualeRitardo >= 50 -> extraColors.statusLate
        statistiche.percentualeRitardo >= 20 -> extraColors.cardPendingOn
        else -> extraColors.statusOntime
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "STORICO RITARDI",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticaSingola(
                    valore = "${statistiche.numCorse}",
                    etichetta = "corse",
                    colore = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                StatisticaSingola(
                    valore = "%.1f".format(statistiche.ritardoMedio),
                    etichetta = "min medi",
                    colore = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                StatisticaSingola(
                    valore = "${statistiche.percentualeRitardo}%",
                    etichetta = "in ritardo",
                    colore = coloreRitardo
                )
            }
        }
    }
}

@Composable
private fun StatisticaSingola(valore: String, etichetta: String, colore: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valore,
            style = MaterialTheme.typography.headlineSmall,
            color = colore,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = etichetta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
