package com.treni.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.treni.tracker.ui.TrenoDetailViewModel
import com.treni.tracker.ui.components.FermataRow
import com.treni.tracker.ui.theme.LocalTreniExtraColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrenoDetailScreen(
    numeroTreno: String,
    stazionePartenzaCod: String,
    stazionePartenzaNome: String,
    stazioneDestinazioneNome: String?,
    timestampMs: Long,
    onIndietro: () -> Unit,
    viewModel: TrenoDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val extraColors = LocalTreniExtraColors.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.inizializza(numeroTreno, stazionePartenzaCod, stazionePartenzaNome, stazioneDestinazioneNome, timestampMs)
    }

    LaunchedEffect(uiState.messaggio) {
        uiState.messaggio?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.consumaMessaggio()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extraColors.surfaceDetail)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        TopAppBar(
            title = { Text("Treno $numeroTreno") },
            navigationIcon = {
                IconButton(onClick = onIndietro) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = extraColors.surfaceDetail
            )
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 24.dp, bottomEnd = 40.dp, bottomStart = 24.dp),
            shadowElevation = 4.dp,
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .background(Brush.linearGradient(listOf(extraColors.gradPrimaryStart, extraColors.gradPrimaryEnd)))
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.tratta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = uiState.testoRitardo,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                IconButton(onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, viewModel.testoPerCondivisione())
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Condividi stato treno"))
                }) {
                    Icon(Icons.Filled.Share, contentDescription = "Condividi", tint = Color.White)
                }
                IconButton(
                    onClick = {
                        viewModel.alternaPreferito(numeroTreno, stazionePartenzaCod, stazionePartenzaNome, stazioneDestinazioneNome)
                    },
                    enabled = uiState.preferitoAbilitato
                ) {
                    Icon(
                        if (uiState.giaPreferito) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Preferito",
                        tint = Color.White
                    )
                }
            }
        }

        uiState.testoStatistiche?.let { testo ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STORICO RITARDI",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = testo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (uiState.caricamento) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            items(uiState.fermate.size) { index ->
                FermataRow(
                    fermata = uiState.fermate[index],
                    isUltima = index == uiState.fermate.size - 1
                )
            }
        }
    }
}
