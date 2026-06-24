package com.treni.tracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.treni.tracker.network.ArrivoTreno
import com.treni.tracker.network.PartenzaTreno
import com.treni.tracker.network.TrainResult
import com.treni.tracker.network.ViaggiaTrenoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TabelloneUiState(
    val nomeStazioneSelezionata: String? = null,
    val codStazioneSelezionata: String? = null,
    val suggerimenti: List<String> = emptyList(),
    val partenze: List<PartenzaTreno> = emptyList(),
    val arrivi: List<ArrivoTreno> = emptyList(),
    val caricamento: Boolean = false,
    val messaggio: String? = null
)

class TabelloneViewModel(application: Application) : AndroidViewModel(application) {

    private val client = ViaggiaTrenoClient()

    private val _uiState = MutableStateFlow(TabelloneUiState())
    val uiState: StateFlow<TabelloneUiState> = _uiState

    private var jobAutocomplete: Job? = null
    private var jobAutoRefresh: Job? = null

    fun consumaMessaggio() {
        _uiState.value = _uiState.value.copy(messaggio = null)
    }

    fun aggiornaSuggerimenti(testo: String) {
        jobAutocomplete?.cancel()
        if (testo.trim().length < 2) {
            _uiState.value = _uiState.value.copy(suggerimenti = emptyList())
            return
        }
        jobAutocomplete = viewModelScope.launch {
            delay(300)
            val result = withContext(Dispatchers.IO) { client.autocompletaStazione(testo.trim()) }
            if (result is TrainResult.Success) {
                _uiState.value = _uiState.value.copy(suggerimenti = result.data.map { it.nome })
            }
        }
    }

    fun selezionaStazione(nome: String) {
        _uiState.value = _uiState.value.copy(
            nomeStazioneSelezionata = nome,
            suggerimenti = emptyList()
        )

        jobAutoRefresh?.cancel()
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { client.autocompletaStazione(nome) }
            if (result is TrainResult.Success && result.data.isNotEmpty()) {
                val cod = result.data.first().codice
                _uiState.value = _uiState.value.copy(codStazioneSelezionata = cod)
                caricaTabellone(cod, nome)
                avviaAutoRefresh(cod, nome)
            } else {
                _uiState.value = _uiState.value.copy(messaggio = "Stazione non trovata")
            }
        }
    }

    private fun avviaAutoRefresh(cod: String, nome: String) {
        jobAutoRefresh = viewModelScope.launch {
            while (true) {
                delay(60_000) // il tabellone si aggiorna da solo ogni minuto
                caricaTabellone(cod, nome)
            }
        }
    }

    fun aggiornaManualmente() {
        val cod = _uiState.value.codStazioneSelezionata
        val nome = _uiState.value.nomeStazioneSelezionata
        if (cod != null && nome != null) {
            caricaTabellone(cod, nome)
        }
    }

    private fun caricaTabellone(cod: String, nome: String) {
        _uiState.value = _uiState.value.copy(caricamento = true)
        viewModelScope.launch {
            val partenzeResult = withContext(Dispatchers.IO) { client.partenzeDaStazione(cod, nome) }
            val arriviResult = withContext(Dispatchers.IO) { client.arriviPerStazione(cod, nome) }

            val partenze = (partenzeResult as? TrainResult.Success)?.data ?: emptyList()
            val arrivi = (arriviResult as? TrainResult.Success)?.data ?: emptyList()

            _uiState.value = _uiState.value.copy(
                caricamento = false,
                partenze = partenze,
                arrivi = arrivi
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        jobAutoRefresh?.cancel()
    }
}
