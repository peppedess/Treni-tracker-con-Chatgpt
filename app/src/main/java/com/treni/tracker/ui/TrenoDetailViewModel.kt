package com.treni.tracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.treni.tracker.data.AppDatabase
import com.treni.tracker.data.TrenoPreferito
import com.treni.tracker.network.StopInfo
import com.treni.tracker.network.TrainResult
import com.treni.tracker.network.ViaggiaTrenoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StatisticheTreno(
    val numCorse: Int,
    val ritardoMedio: Double,
    val percentualeRitardo: Int
)

data class DetailUiState(
    val numeroTreno: String = "",
    val tratta: String = "",
    val testoRitardo: String = "In attesa di aggiornamento",
    val categoria: String? = null,
    val fermate: List<StopInfo> = emptyList(),
    val giaPreferito: Boolean = false,
    val preferitoAbilitato: Boolean = true,
    val statistiche: StatisticheTreno? = null,
    val caricamento: Boolean = false,
    val messaggio: String? = null
)

class TrenoDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).trenoDao()
    private val client = ViaggiaTrenoClient()

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    private var statoCorrenteCondivisione = "In attesa di aggiornamento"

    fun inizializza(
        numeroTreno: String,
        stazionePartenzaCod: String,
        stazionePartenzaNome: String,
        stazioneDestinazioneNome: String?,
        timestampMs: Long
    ) {
        val tratta = "$stazionePartenzaNome → ${stazioneDestinazioneNome ?: "?"}"
        _uiState.value = _uiState.value.copy(numeroTreno = numeroTreno, tratta = tratta)

        verificaPreferito(numeroTreno, stazionePartenzaCod)
        caricaStatistiche(numeroTreno)
        caricaDettagli(stazionePartenzaCod, numeroTreno, timestampMs)
    }

    fun aggiorna(stazionePartenzaCod: String, numeroTreno: String, timestampMs: Long) {
        caricaDettagli(stazionePartenzaCod, numeroTreno, timestampMs)
    }

    fun consumaMessaggio() {
        _uiState.value = _uiState.value.copy(messaggio = null)
    }

    fun testoPerCondivisione(): String {
        val s = _uiState.value
        return "🚆 Treno ${s.numeroTreno}\n${s.tratta}\n$statoCorrenteCondivisione\n\n(via Treni Tracker)"
    }

    private fun verificaPreferito(numeroTreno: String, stazionePartenzaCod: String) {
        viewModelScope.launch {
            val giaPreferito = withContext(Dispatchers.IO) {
                dao.contaPreferito(numeroTreno, stazionePartenzaCod) > 0
            }
            _uiState.value = _uiState.value.copy(giaPreferito = giaPreferito)
        }
    }

    fun alternaPreferito(
        numeroTreno: String,
        stazionePartenzaCod: String,
        stazionePartenzaNome: String,
        stazioneDestinazioneNome: String?
    ) {
        _uiState.value = _uiState.value.copy(preferitoAbilitato = false)
        viewModelScope.launch {
            val statoAttuale = _uiState.value.giaPreferito
            if (statoAttuale) {
                withContext(Dispatchers.IO) {
                    dao.rimuoviPreferitoPerChiave(numeroTreno, stazionePartenzaCod)
                }
                _uiState.value = _uiState.value.copy(
                    giaPreferito = false,
                    preferitoAbilitato = true,
                    messaggio = "Rimosso dai preferiti"
                )
            } else {
                val esisteGia = withContext(Dispatchers.IO) {
                    dao.contaPreferito(numeroTreno, stazionePartenzaCod) > 0
                }
                if (esisteGia) {
                    _uiState.value = _uiState.value.copy(giaPreferito = true, preferitoAbilitato = true)
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    dao.inserisciPreferito(
                        TrenoPreferito(
                            numeroTreno = numeroTreno,
                            stazionePartenzaCod = stazionePartenzaCod,
                            stazionePartenzaNome = stazionePartenzaNome,
                            stazioneDestinazioneNome = stazioneDestinazioneNome
                        )
                    )
                }
                _uiState.value = _uiState.value.copy(
                    giaPreferito = true,
                    preferitoAbilitato = true,
                    messaggio = "Aggiunto ai preferiti"
                )
            }
        }
    }

    private fun caricaStatistiche(numeroTreno: String) {
        viewModelScope.launch {
            val ritardi = withContext(Dispatchers.IO) { dao.getRitardiPerCorsa(numeroTreno) }
            if (ritardi.isEmpty()) {
                _uiState.value = _uiState.value.copy(statistiche = null)
                return@launch
            }
            val numCorse = ritardi.size
            val ritardoMedio = ritardi.map { it.ritardoMax }.average()
            val corseInRitardo = ritardi.count { it.ritardoMax >= 5 }
            val percentualeRitardo = (corseInRitardo * 100) / numCorse

            _uiState.value = _uiState.value.copy(
                statistiche = StatisticheTreno(
                    numCorse = numCorse,
                    ritardoMedio = ritardoMedio,
                    percentualeRitardo = percentualeRitardo
                )
            )
        }
    }

    private fun caricaDettagli(stazionePartenzaCod: String, numeroTreno: String, timestampMs: Long) {
        _uiState.value = _uiState.value.copy(caricamento = true)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                client.andamentoTreno(stazionePartenzaCod, numeroTreno, timestampMs)
            }

            when (result) {
                is TrainResult.Success -> {
                    val stato = result.data
                    val ritardo = stato.ritardoMinuti
                    val testoStato = when {
                        ritardo > 0 -> "In ritardo di $ritardo min"
                        ritardo < 0 -> "In anticipo di ${kotlin.math.abs(ritardo)} min"
                        else -> "In orario"
                    }
                    statoCorrenteCondivisione = testoStato +
                        (stato.ultimaStazioneRilevata?.let { " (ultima fermata: $it)" } ?: "")

                    _uiState.value = _uiState.value.copy(
                        caricamento = false,
                        testoRitardo = testoStato,
                        categoria = stato.categoria,
                        fermate = stato.fermate
                    )
                }
                is TrainResult.NoData -> {
                    _uiState.value = _uiState.value.copy(caricamento = false, messaggio = result.message)
                }
                else -> {
                    _uiState.value = _uiState.value.copy(caricamento = false, messaggio = "Errore di rete. Riprova.")
                }
            }
        }
    }
}
