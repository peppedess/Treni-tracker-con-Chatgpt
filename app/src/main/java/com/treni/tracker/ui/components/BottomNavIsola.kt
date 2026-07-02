package com.treni.tracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.treni.tracker.R

/**
 * Restituisce la scala da applicare a una voce della nav in base allo stato
 * di pressione: 1.0 a riposo, 0.90 premuta, con ritorno elastico a molla.
 */
@Composable
private fun scalaPressione(interactionSource: MutableInteractionSource): Float {
    val premuta by interactionSource.collectIsPressedAsState()
    val scala by animateFloatAsState(
        targetValue = if (premuta) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scalaVoceNav"
    )
    return scala
}

/**
 * Bottom navigation a isola flottante, stile pillola, coerente col
 * riferimento visivo (item attivo con sfondo pieno colorato).
 * Ogni voce ha uno "squish" elastico alla pressione.
 */
@Composable
fun BottomNavIsola(
    onHomeClick: () -> Unit,
    onTrattaClick: () -> Unit,
    onTabelloneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voce attiva: Home, con pillola colorata
            val sorgenteHome = remember { MutableInteractionSource() }
            val scalaHome = scalaPressione(sorgenteHome)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scalaHome
                        scaleY = scalaHome
                    }
                    .clickable(
                        interactionSource = sorgenteHome,
                        indication = LocalIndication.current
                    ) { onHomeClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Home",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            val sorgenteTratta = remember { MutableInteractionSource() }
            val scalaTratta = scalaPressione(sorgenteTratta)
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scalaTratta
                        scaleY = scalaTratta
                    }
                    .clickable(
                        interactionSource = sorgenteTratta,
                        indication = LocalIndication.current
                    ) { onTrattaClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_train),
                    contentDescription = "Cerca per tratta",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val sorgenteTabellone = remember { MutableInteractionSource() }
            val scalaTabellone = scalaPressione(sorgenteTabellone)
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scalaTabellone
                        scaleY = scalaTabellone
                    }
                    .clickable(
                        interactionSource = sorgenteTabellone,
                        indication = LocalIndication.current
                    ) { onTabelloneClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    Icons.Filled.List,
                    contentDescription = "Tabellone stazione",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
