package com.treni.tracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

        // Transizione morbida tra la vista suggerimenti e il tabellone vero e proprio
        AnimatedContent(
            targetState = uiState.nomeStazioneSelezionata != null,
            transitionSpec = {
                (fadeIn() + slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { altezza -> altezza / 10 }) togetherWith fadeOut()
            },
            label = "contenuto-tabellone",
            modifier = Modifier.fillMaxSize()
        ) { stazioneSelezionata ->
            if (stazioneSelezionata) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = uiState.nomeStazioneSelezionata.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
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

                    // Il LoadingIndicator espressivo appare e scompare con una molla,
                    // senza far "saltare" il layout della lista
                    AnimatedVisibility(
                        visible = uiState.caricamento,
                        enter = fadeIn() + expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            LoadingIndicator(modifier = Modifier.padding(24.dp))
                        }
                    }

                    // La lista scivola lateralmente con overshoot elastico
                    // nella direzione del tab scelto
                    AnimatedContent(
                        targetState = tabSelezionata,
                        transitionSpec = {
                            val versoSinistra = targetState > initialState
                            val molla = spring<androidx.compose.ui.unit.IntOffset>(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                            val entra = slideInHorizontally(animationSpec = molla) { larghezza ->
                                if (versoSinistra) larghezza / 3 else -larghezza / 3
                            } + fadeIn()
                            val esce = slideOutHorizontally(animationSpec = molla) { larghezza ->
                                if (versoSinistra) -larghezza / 3 else larghezza / 3
                            } + fadeOut()
                            entra togetherWith esce
                        },
                        label = "tab-partenze-arrivi",
                        modifier = Modifier.fillMaxSize()
                    ) { tab ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            if (tab == 0) {
                                items(
                                    uiState.partenze,
                                    key = { "${it.numeroTreno}-${it.orarioPartenza}" }
                                ) { p ->
                                    // animateItem: quando l'auto-refresh cambia ordine o
                                    // aggiunge/rimuove treni, le righe si muovono animate
                                    Column(modifier = Modifier.animateItem()) {
                                        RigaTabellone(
                                            orario = p.orarioPartenza,
                                            numeroTreno = p.numeroTreno,
                                            stazioneAltroCapo = "→ ${p.destinazione}",
                                            binario = p.binario,
                                            ritardo = p.ritardo
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            } else {
                                items(
                                    uiState.arrivi,
                                    key = { "${it.numeroTreno}-${it.orarioArrivo}" }
                                ) { a ->
                                    Column(modifier = Modifier.animateItem()) {
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
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    if (uiState.stazioniVicine.isNotEmpty()) {
                        item {
                            Text(
                                text = "Stazioni vicine a te",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                            )
                        }
                        items(uiState.stazioniVicine) { nome ->
                            Text(
                                text = nome,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        testoStazione = nome
                                        viewModel.selezionaStazione(nome)
                                    }
                                    .padding(vertical = 12.dp)
                            )
                            HorizontalDivider()
                        }
                    }

                    if (uiState.stazioniRecenti.isNotEmpty()) {
                        item {
                            Text(
                                text = "Cercate di recente",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(uiState.stazioniRecenti) { nome ->
                            Text(
                                text = nome,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        testoStazione = nome
                                        viewModel.selezionaStazione(nome)
                                    }
                                    .padding(vertical = 12.dp)
                            )
                            HorizontalDivider()
                        }
                    }

                    if (uiState.stazioniVicine.isEmpty() && uiState.stazioniRecenti.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
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
            }
        }
    }
}
