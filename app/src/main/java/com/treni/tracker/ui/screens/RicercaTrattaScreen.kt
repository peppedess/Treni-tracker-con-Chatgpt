package com.treni.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.treni.tracker.ui.RicercaTrattaViewModel
import com.treni.tracker.ui.components.PartenzaCard
import com.treni.tracker.ui.theme.LocalTreniExtraColors

/**
 * Campo di testo con suggerimenti "fatti in casa": una lista cliccabile
 * mostrata sotto il campo quando ci sono suggerimenti disponibili.
 * Evita ExposedDropdownMenuBox, la cui API è cambiata troppe volte tra
 * versioni di Material3 per essere usata in sicurezza qui.
 */
@Composable
private fun CampoConSuggerimenti(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RicercaTrattaScreen(
    onIndietro: () -> Unit,
    viewModel: RicercaTrattaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val extraColors = LocalTreniExtraColors.current
    val context = LocalContext.current

    var partenza by remember { mutableStateOf("") }
    var destinazione by remember { mutableStateOf("") }
    var suggerimentiPartenzaVisibili by remember { mutableStateOf(true) }
    var suggerimentiDestinazioneVisibili by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.messaggio) {
        uiState.messaggio?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.consumaMessaggio()
        }
    }

    LaunchedEffect(uiState.trenoAggiunto) {
        if (uiState.trenoAggiunto) {
            viewModel.consumaTrenoAggiunto()
            onIndietro()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extraColors.surfaceTratta)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        TopAppBar(
            title = { Text("Cerca per tratta") },
            navigationIcon = {
                IconButton(onClick = onIndietro) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = extraColors.surfaceTratta
            )
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 4.dp,
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.linearGradient(listOf(extraColors.gradPrimaryStart, extraColors.gradPrimaryEnd)))
                    .padding(20.dp)
            ) {
                CampoConSuggerimenti(
                    valore = partenza,
                    onValoreChange = {
                        partenza = it
                        suggerimentiPartenzaVisibili = true
                        viewModel.aggiornaSuggerimentiPartenza(it)
                    },
                    onSuggerimentoScelto = {
                        partenza = it
                        suggerimentiPartenzaVisibili = false
                    },
                    suggerimenti = if (suggerimentiPartenzaVisibili) uiState.suggerimentiPartenza else emptyList(),
                    label = "Stazione di partenza"
                )

                CampoConSuggerimenti(
                    valore = destinazione,
                    onValoreChange = {
                        destinazione = it
                        suggerimentiDestinazioneVisibili = true
                        viewModel.aggiornaSuggerimentiDestinazione(it)
                    },
                    onSuggerimentoScelto = {
                        destinazione = it
                        suggerimentiDestinazioneVisibili = false
                    },
                    suggerimenti = if (suggerimentiDestinazioneVisibili) uiState.suggerimentiDestinazione else emptyList(),
                    label = "Stazione di arrivo",
                    modifier = Modifier.padding(top = 16.dp)
                )

                Button(
                    onClick = {
                        suggerimentiPartenzaVisibili = false
                        suggerimentiDestinazioneVisibili = false
                        viewModel.cercaTratta(partenza, destinazione)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Cerca")
                }

                if (uiState.caricamento) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }

        uiState.messaggioVuoto?.let { testo ->
            Text(
                text = testo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            items(uiState.risultati) { partenzaTreno ->
                Box(modifier = Modifier.padding(bottom = 12.dp)) {
                    PartenzaCard(
                        partenza = partenzaTreno,
                        onAggiungi = { viewModel.aggiungiTreno(partenzaTreno) }
                    )
                }
            }
        }
    }
}
