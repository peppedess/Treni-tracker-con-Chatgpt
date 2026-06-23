package com.treni.tracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.treni.tracker.ui.screens.TrenoDetailScreen
import com.treni.tracker.ui.theme.TreniTrackerTheme

class TrenoDetailActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TRENO_ID = "extra_treno_id"
        const val EXTRA_NUMERO_TRENO = "extra_numero_treno"
        const val EXTRA_STAZIONE_PARTENZA_COD = "extra_stazione_partenza_cod"
        const val EXTRA_STAZIONE_PARTENZA_NOME = "extra_stazione_partenza_nome"
        const val EXTRA_STAZIONE_DESTINAZIONE_NOME = "extra_stazione_destinazione_nome"
        const val EXTRA_TIMESTAMP_MS = "extra_timestamp_ms"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val numeroTreno = intent.getStringExtra(EXTRA_NUMERO_TRENO) ?: ""
        val stazionePartenzaCod = intent.getStringExtra(EXTRA_STAZIONE_PARTENZA_COD) ?: ""
        val stazionePartenzaNome = intent.getStringExtra(EXTRA_STAZIONE_PARTENZA_NOME) ?: ""
        val stazioneDestinazioneNome = intent.getStringExtra(EXTRA_STAZIONE_DESTINAZIONE_NOME)
        val timestampMs = intent.getLongExtra(EXTRA_TIMESTAMP_MS, 0L)

        setContent {
            TreniTrackerTheme {
                TrenoDetailScreen(
                    numeroTreno = numeroTreno,
                    stazionePartenzaCod = stazionePartenzaCod,
                    stazionePartenzaNome = stazionePartenzaNome,
                    stazioneDestinazioneNome = stazioneDestinazioneNome,
                    timestampMs = timestampMs,
                    onIndietro = { finish() }
                )
            }
        }
    }
}
