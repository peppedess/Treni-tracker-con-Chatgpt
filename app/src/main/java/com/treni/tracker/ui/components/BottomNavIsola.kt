package com.treni.tracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.treni.tracker.R

/**
 * Bottom navigation a isola flottante, stile pillola, coerente col
 * riferimento visivo (item attivo con sfondo pieno colorato).
 */
@Composable
fun BottomNavIsola(
    onHomeClick: () -> Unit,
    onTrattaClick: () -> Unit,
    onTabelloneClick: () -> Unit,
    onTemaClick: () -> Unit,
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
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.clickable { onHomeClick() }
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

            Row(
                modifier = Modifier
                    .clickable { onTrattaClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_train),
                    contentDescription = "Cerca per tratta",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .clickable { onTabelloneClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    Icons.Filled.List,
                    contentDescription = "Tabellone stazione",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .clickable { onTemaClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    Icons.Filled.WbSunny,
                    contentDescription = "Cambia tema",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
