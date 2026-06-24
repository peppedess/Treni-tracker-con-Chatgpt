package com.treni.tracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Campo di testo con suggerimenti "fatti in casa": una lista cliccabile
 * mostrata sotto il campo quando ci sono suggerimenti disponibili.
 * Evita ExposedDropdownMenuBox, la cui API è cambiata troppe volte tra
 * versioni di Material3 per essere usata in sicurezza. Condiviso tra
 * la ricerca per tratta e il tabellone stazione.
 */
@Composable
fun CampoConSuggerimenti(
    valore: String,
    onValoreChange: (String) -> Unit,
    onSuggerimentoScelto: (String) -> Unit,
    suggerimenti: List<String>,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = valore,
            onValueChange = onValoreChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        if (suggerimenti.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column {
                    suggerimenti.take(6).forEach { nome ->
                        Text(
                            text = nome,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggerimentoScelto(nome) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
