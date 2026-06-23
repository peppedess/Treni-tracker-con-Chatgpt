package com.treni.tracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.treni.tracker.data.AppDatabase
import com.treni.tracker.data.TrenoMonitorato
import com.treni.tracker.data.TrenoPreferito
import com.treni.tracker.network.TrainCandidate
import com.treni.tracker.network.TrainResult
import com.treni.tracker.network.ViaggiaTrenoClient
import com.treni.tracker.worker.TrainCheckWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Eventi una tantum da mostrare in UI (Toast, dialog di scelta candidati).
 * Separati dallo stato persistente per non essere ripetuti ad ogni recomposition.
 */
sealed class HomeEvent {
    data class Messaggio(val testo: String) : HomeEvent()
    data class SceltaCandidati(val candidati: List<TrainCandidate>) : HomeEvent()
}

data class HomeUiState(
    val treni: List<TrenoMonitorato> = emptyList(),
    val preferiti: List<TrenoPreferito> = emptyList(),
    val testoDashboard: String = "Nessun treno monitorato al momento",
    val caricamento: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).trenoDao()
    private val client = ViaggiaTrenoClient()

    private val _caricamento = MutableStateFlow(false)
    private val _evento = MutableStateFlow<HomeEvent?>(null)
    val evento: StateFlow<HomeEvent?> = _evento

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        combine(
            dao.osservaTreniAttivi().asFlow(),
            dao.osservaPreferiti().asFlow(),
            _caricamento
        ) { treni, preferiti, caricamento ->
            HomeUiState(
                treni = treni,
                preferiti = preferiti,
                testoDashboard = costruisciTestoDashboard(treni, preferiti.size),
                caricamento = caricamento
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }

    private fun costruisciTestoDashboard(treni: List<TrenoMonitorato>, numPreferiti: Int): String {
        val totale = treni.size
        val inRitardo = treni.count { (it.ultimoRitardo ?: 0) > 0 }
        return when {
            totale == 0 -> "Nessun treno monitorato al momento"
            inRitardo == 0 -> "$totale monitorati • Tutti in orario • $numPreferiti preferiti"
            else -> "$totale monitorati • $inRitardo in ritardo • $numPreferiti preferiti"
        }
    }

    fun consumaEvento() {
        _evento.value = null
    }

    fun cercaTreno(numero: String) {
        if (numero.isBlank()) return
        _caricamento.value = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { client.cercaNumeroTreno(numero) }
            _caricamento.value = false
            when (result) {
                is TrainResult.Success -> {
                    val candidati = result.data
                    if (candidati.size == 1) {
                        confermaTreno(candidati[0])
                    } else {
                        _evento.value = HomeEvent.SceltaCandidati(candidati)
                    }
                }
                is TrainResult.NotFound -> _evento.value = HomeEvent.Messaggio(result.message)
                else -> _evento.value = HomeEvent.Messaggio("Errore di rete. Riprova.")
            }
        }
    }

    fun confermaTreno(candidato: TrainCandidate) {
        _caricamento.value = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                client.andamentoTreno(candidato.stazionePartenzaCod, candidato.numero, candidato.timestampMs)
            }
            _caricamento.value = false

            when (result) {
                is TrainResult.Success -> {
                    val stato = result.data
                    val dataCorsa = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(java.util.Date(candidato.timestampMs))

                    val giaPresente = withContext(Dispatchers.IO) {
                        dao.contaTrenoMonitoratoOggi(candidato.numero, dataCorsa) > 0
                    }

                    if (giaPresente) {
                        _evento.value = HomeEvent.Messaggio("Il treno ${candidato.numero} è già nella lista monitorati")
                        return@launch
                    }

                    val treno = TrenoMonitorato(
                        numeroTreno = candidato.numero,
                        stazionePartenzaCod = candidato.stazionePartenzaCod,
                        stazionePartenzaNome = candidato.stazionePartenzaNome,
                        stazioneDestinazioneNome = stato.stazioneDestinazione,
                        timestampMs = candidato.timestampMs,
                        dataCorsa = dataCorsa
                    )

                    withContext(Dispatchers.IO) { dao.inserisci(treno) }
                    lanciaControlloImmediato()
                }
                is TrainResult.NoData -> _evento.value = HomeEvent.Messaggio(result.message)
                else -> _evento.value = HomeEvent.Messaggio("Errore di rete. Riprova.")
            }
        }
    }

    fun riaggiungiDaPreferito(pref: TrenoPreferito) {
        _caricamento.value = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { client.cercaNumeroTreno(pref.numeroTreno) }
            _caricamento.value = false
            when (result) {
                is TrainResult.Success -> {
                    val candidato = result.data.firstOrNull { it.stazionePartenzaCod == pref.stazionePartenzaCod }
                        ?: result.data.first()
                    confermaTreno(candidato)
                }
                is TrainResult.NotFound -> _evento.value = HomeEvent.Messaggio("Nessuna corsa oggi per il treno ${pref.numeroTreno}")
                else -> _evento.value = HomeEvent.Messaggio("Errore di rete. Riprova.")
            }
        }
    }

    fun rimuoviTreno(treno: TrenoMonitorato) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { dao.rimuovi(treno.id) }
        }
    }

    fun rimuoviPreferito(pref: TrenoPreferito) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { dao.rimuoviPreferito(pref.id) }
        }
    }

    fun aggiornaManualmente() {
        val richiesta = OneTimeWorkRequestBuilder<TrainCheckWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(getApplication()).enqueue(richiesta)
    }

    private fun lanciaControlloImmediato() {
        val richiesta = OneTimeWorkRequestBuilder<TrainCheckWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(getApplication()).enqueue(richiesta)
    }
}
