package com.treni.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.treni.tracker.ui.TabelloneViewModel
import com.treni.tracker.ui.components.CampoConSuggerimenti
import com.treni.tracker.ui.components.RigaTabellone
import com.treni.tracker.ui.theme.LocalTreniExtraColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabelloneScreen(
    onIndietro: () -> Unit,
    viewModel: TabelloneViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val extraColors = LocalTreniExtraColors.current
    val context = LocalContext.current

    var testoStazione by remember { mutableStateOf("") }
    var tabSelezionata by remember { mutableStateOf(0) }

    LaunchedEffect(uiState.messaggio) {
        uiState.messaggio?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.consumaMessaggio()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extraColors.surfaceTratta)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        TopAppBar(
            title = { Text("Tabellone stazione") },
            navigationIcon = {
                IconButton(onClick = onIndietro) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
            },
            actions = {
                if (uiState.codStazioneSelezionata != null) {
                    IconButton(onClick = { viewModel.aggiornaManualmente() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Aggiorna")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = extraColors.surfaceTratta
            )
        )

        Box(modifier = Modifier.padding(20.dp)) {
            CampoConSuggerimenti(
                valore = testoStazione,
                onValoreChange = {
                    testoStazione = it
                    viewModel.aggiornaSuggerimenti(it)
                },
                onSuggerimentoScelto = { nome ->
                    testoStazione = nome
                    viewModel.selezionaStazione(nome)
                },
                suggerimenti = uiState.suggerimenti,
                label = "Cerca una stazione"
            )
        }

        if (uiState.nomeStazioneSelezionata != null) {
            Text(
                text = uiState.nomeStazioneSelezionata.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, bottom = 8.dp)
            )

            TabRow(selectedTabIndex = tabSelezionata) {
                Tab(
                    selected = tabSelezionata == 0,
                    onClick = { tabSelezionata = 0 },
                    text = { Text("Partenze") }
                )
                Tab(
                    selected = tabSelezionata == 1,
                    onClick = { tabSelezionata = 1 },
                    text = { Text("Arrivi") }
                )
            }

            if (uiState.caricamento) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                if (tabSelezionata == 0) {
                    items(uiState.partenze) { p ->
                        RigaTabellone(
                            orario = p.orarioPartenza,
                            numeroTreno = p.numeroTreno,
                            stazioneAltroCapo = "→ ${p.destinazione}",
                            binario = p.binario,
                            ritardo = p.ritardo
                        )
                        HorizontalDivider()
                    }
                } else {
                    items(uiState.arrivi) { a ->
                        RigaTabellone(
                            orario = a.orarioArrivo,
                            numeroTreno = a.numeroTreno,
                            stazioneAltroCapo = "da ${a.provenienza}",
                            binario = a.binario,
                            ritardo = a.ritardo
                        )
                        HorizontalDivider()
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cerca una stazione per vedere partenze e arrivi in tempo reale.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
