package com.treni.tracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.treni.tracker.R
import com.treni.tracker.data.TrenoPreferito
import com.treni.tracker.network.TrainCandidate
import com.treni.tracker.ui.HomeEvent
import com.treni.tracker.ui.HomeViewModel
import com.treni.tracker.ui.components.BottomNavIsola
import com.treni.tracker.ui.components.PreferitoChip
import com.treni.tracker.ui.components.TrenoCard
import com.treni.tracker.ui.theme.LocalTreniExtraColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onApriDettaglio: (Long, String, String, String, String?, Long) -> Unit,
    onApriRicercaTratta: () -> Unit,
    onApriSceltaTema: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val evento by viewModel.evento.collectAsState()
    val extraColors = LocalTreniExtraColors.current
    val context = LocalContext.current

    var numeroTreno by remember { mutableStateOf("") }
    var dialogScelta by remember { mutableStateOf<List<TrainCandidate>?>(null) }

    LaunchedEffect(evento) {
        when (val ev = evento) {
            is HomeEvent.Messaggio -> {
                android.widget.Toast.makeText(context, ev.testo, android.widget.Toast.LENGTH_LONG).show()
                viewModel.consumaEvento()
            }
            is HomeEvent.SceltaCandidati -> {
                dialogScelta = ev.candidati
                viewModel.consumaEvento()
            }
            null -> {}
        }
    }

    dialogScelta?.let { candidati ->
        AlertDialog(
            onDismissRequest = { dialogScelta = null },
            title = { Text("Scegli la corsa giusta") },
            text = {
                Column {
                    candidati.forEach { candidato ->
                        Text(
                            text = "Treno ${candidato.numero} da ${candidato.stazionePartenzaNome}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.confermaTreno(candidato)
                                    dialogScelta = null
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { dialogScelta = null }) {
                    Text("Annulla")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(extraColors.surfaceHome)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {

            // Header con blob decorativo dietro al titolo
            Box {
                Surface(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 18.dp)
                        .size(width = 78.dp, height = 56.dp)
                        .alpha(0.35f),
                    shape = RoundedCornerShape(
                        topStart = 60.dp, topEnd = 34.dp, bottomEnd = 60.dp, bottomStart = 20.dp
                    ),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {}

                Column(modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 20.dp, bottom = 8.dp)) {
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = extraColors.gradPrimaryStart)) {
                                        append("Treni")
                                    }
                                    append(" ")
                                    withStyle(SpanStyle(color = extraColors.gradPrimaryEnd)) {
                                        append("Tracker")
                                    }
                                },
                                style = MaterialTheme.typography.displaySmall
                            )
                            Text(
                                text = "Monitoraggio live • Stato rete",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onApriSceltaTema) {
                            Icon(
                                Icons.Filled.WbSunny,
                                contentDescription = "Cambia tema",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Dashboard statistiche
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = uiState.testoDashboard,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(14.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.Transparent
                    ) {
                        Column(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(extraColors.gradPrimaryStart, extraColors.gradPrimaryEnd)
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            OutlinedTextField(
                                value = numeroTreno,
                                onValueChange = { numeroTreno = it },
                                label = { Text("Numero treno (es. 2345)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.94f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "scaleBottoneCerca"
                            )

                            Button(
                                onClick = { viewModel.cercaTreno(numeroTreno) },
                                interactionSource = interactionSource,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .height(56.dp)
                                    .graphicsLayer { scaleX = scale; scaleY = scale },
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text("Cerca", fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "Inserisci il numero del treno di oggi. Se è ambiguo, ti faccio scegliere la corsa giusta.",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 16.dp)
                            )

                            AnimatedVisibility(visible = uiState.caricamento) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.preferiti.isNotEmpty()) {
                    item {
                        Text(
                            text = "I tuoi treni preferiti",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            items(uiState.preferiti, key = { it.id }) { pref: TrenoPreferito ->
                                PreferitoChip(
                                    preferito = pref,
                                    onClick = { viewModel.riaggiungiDaPreferito(pref) },
                                    onLongClick = { viewModel.rimuoviPreferito(pref) }
                                )
                            }
                        }
                    }
                }

                if (uiState.treni.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_train),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp).alpha(0.5f)
                            )
                            Text(
                                text = "Nessun treno monitorato.\nAggiungine uno qui sopra.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                } else {
                    items(uiState.treni, key = { it.id }) { treno ->
                        Box(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .animateItem(
                                    fadeInSpec = androidx.compose.animation.core.tween(220),
                                    placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    fadeOutSpec = androidx.compose.animation.core.tween(150)
                                )
                        ) {
                            TrenoCard(
                                treno = treno,
                                onClick = {
                                    onApriDettaglio(
                                        treno.id,
                                        treno.numeroTreno,
                                        treno.stazionePartenzaCod,
                                        treno.stazionePartenzaNome,
                                        treno.stazioneDestinazioneNome,
                                        treno.timestampMs
                                    )
                                },
                                onRimuovi = { viewModel.rimuoviTreno(treno) }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(96.dp)) }
            }
        }

        BottomNavIsola(
            onHomeClick = { /* già in home */ },
            onTrattaClick = onApriRicercaTratta,
            onTemaClick = onApriSceltaTema,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
