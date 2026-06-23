package com.treni.tracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.treni.tracker.data.AppDatabase
import com.treni.tracker.data.TrenoMonitorato
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
import java.text.SimpleDateFormat
import java.util.Locale

data class RicercaTrattaUiState(
    val suggerimentiPartenza: List<String> = emptyList(),
    val suggerimentiDestinazione: List<String> = emptyList(),
    val risultati: List<PartenzaTreno> = emptyList(),
    val messaggioVuoto: String? = null,
    val caricamento: Boolean = false,
    val messaggio: String? = null,
    val trenoAggiunto: Boolean = false
)

class RicercaTrattaViewModel(application: Application) : AndroidViewModel(application) {

    private val client = ViaggiaTrenoClient()
    private val dao = AppDatabase.getInstance(application).trenoDao()

    private val _uiState = MutableStateFlow(RicercaTrattaUiState())
    val uiState: StateFlow<RicercaTrattaUiState> = _uiState

    private var jobAutocompletePartenza: Job? = null
    private var jobAutocompleteDestinazione: Job? = null

    fun consumaMessaggio() {
        _uiState.value = _uiState.value.copy(messaggio = null)
    }

    fun consumaTrenoAggiunto() {
        _uiState.value = _uiState.value.copy(trenoAggiunto = false)
    }

    /** Aggiorna i suggerimenti per il campo partenza, con debounce. */
    fun aggiornaSuggerimentiPartenza(testo: String) {
        jobAutocompletePartenza?.cancel()
        if (testo.trim().length < 2) {
            _uiState.value = _uiState.value.copy(suggerimentiPartenza = emptyList())
            return
        }
        jobAutocompletePartenza = viewModelScope.launch {
            delay(300)
            val result = withContext(Dispatchers.IO) { client.autocompletaStazione(testo.trim()) }
            if (result is TrainResult.Success) {
                _uiState.value = _uiState.value.copy(suggerimentiPartenza = result.data.map { it.nome })
            }
        }
    }

    /** Aggiorna i suggerimenti per il campo destinazione, con debounce. */
    fun aggiornaSuggerimentiDestinazione(testo: String) {
        jobAutocompleteDestinazione?.cancel()
        if (testo.trim().length < 2) {
            _uiState.value = _uiState.value.copy(suggerimentiDestinazione = emptyList())
            return
        }
        jobAutocompleteDestinazione = viewModelScope.launch {
            delay(300)
            val result = withContext(Dispatchers.IO) { client.autocompletaStazione(testo.trim()) }
            if (result is TrainResult.Success) {
                _uiState.value = _uiState.value.copy(suggerimentiDestinazione = result.data.map { it.nome })
            }
        }
    }

    fun cercaTratta(partenza: String, destinazione: String) {
        if (partenza.isBlank() || destinazione.isBlank()) {
            _uiState.value = _uiState.value.copy(messaggio = "Inserisci partenza e destinazione")
            return
        }

        _uiState.value = _uiState.value.copy(caricamento = true, messaggioVuoto = null)

        viewModelScope.launch {
            val stazioneResult = withContext(Dispatchers.IO) { client.autocompletaStazione(partenza) }

            if (stazioneResult !is TrainResult.Success || stazioneResult.data.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    caricamento = false,
                    messaggio = "Stazione di partenza non trovata"
                )
                return@launch
            }

            val stazionePartenza = stazioneResult.data.first()

            val partenzeResult = withContext(Dispatchers.IO) {
                client.partenzeDaStazione(stazionePartenza.codice, stazionePartenza.nome)
            }

            when (partenzeResult) {
                is TrainResult.Success -> {
                    val filtrate = partenzeResult.data.filter {
                        it.destinazione.contains(destinazione, ignoreCase = true)
                    }
                    if (filtrate.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            caricamento = false,
                            risultati = emptyList(),
                            messaggioVuoto = "Nessun treno diretto verso \"$destinazione\" in partenza ora da ${stazionePartenza.nome}.\n\n" +
                                "Nota: vengono mostrati solo i treni in partenza nelle prossime ore."
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(caricamento = false, risultati = filtrate, messaggioVuoto = null)
                    }
                }
                is TrainResult.NoData -> {
                    _uiState.value = _uiState.value.copy(
                        caricamento = false,
                        risultati = emptyList(),
                        messaggioVuoto = partenzeResult.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(caricamento = false, messaggio = "Errore di rete. Riprova.")
                }
            }
        }
    }

    fun aggiungiTreno(partenza: PartenzaTreno) {
        _uiState.value = _uiState.value.copy(caricamento = true)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                client.andamentoTreno(partenza.codStazionePartenza, partenza.numeroTreno, partenza.timestampMs)
            }

            when (result) {
                is TrainResult.Success -> {
                    val stato = result.data
                    val dataCorsa = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(java.util.Date(partenza.timestampMs))
                    val treno = TrenoMonitorato(
                        numeroTreno = partenza.numeroTreno,
                        stazionePartenzaCod = partenza.codStazionePartenza,
                        stazionePartenzaNome = partenza.nomeStazionePartenza,
                        stazioneDestinazioneNome = stato.stazioneDestinazione ?: partenza.destinazione,
                        timestampMs = partenza.timestampMs,
                        dataCorsa = dataCorsa,
                        categoria = stato.categoria
                    )
                    withContext(Dispatchers.IO) { dao.inserisci(treno) }
                    _uiState.value = _uiState.value.copy(
                        caricamento = false,
                        messaggio = "Treno ${partenza.numeroTreno} aggiunto al monitoraggio",
                        trenoAggiunto = true
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(caricamento = false, messaggio = "Impossibile aggiungere questo treno ora.")
                }
            }
        }
    }
}
