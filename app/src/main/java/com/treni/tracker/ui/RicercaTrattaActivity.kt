package com.treni.tracker.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.treni.tracker.R
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.treni.tracker.data.AppDatabase
import com.treni.tracker.data.TrenoMonitorato
import com.treni.tracker.databinding.ActivityRicercaTrattaBinding
import com.treni.tracker.network.PartenzaTreno
import com.treni.tracker.network.StazioneAutocomplete
import com.treni.tracker.network.TrainResult
import com.treni.tracker.network.ViaggiaTrenoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class RicercaTrattaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRicercaTrattaBinding
    private lateinit var partenzeAdapter: PartenzeAdapter
    private val client = ViaggiaTrenoClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRicercaTrattaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Con targetSdk 35 il layout è edge-to-edge di default: applichiamo
        // manualmente il padding della status bar alla toolbar, altrimenti
        // il titolo finisce sotto l'orario/le icone di sistema.
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarInsets = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars())
            view.setPadding(view.paddingLeft, statusBarInsets.top, view.paddingRight, view.paddingBottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.scale_up_enter, R.anim.slide_down_exit)
        }

        partenzeAdapter = PartenzeAdapter(emptyList()) { partenza -> aggiungiTreno(partenza) }
        binding.recyclerPartenze.layoutManager = LinearLayoutManager(this)
        binding.recyclerPartenze.adapter = partenzeAdapter

        binding.btnCercaTratta.setOnClickListener { cercaTratta() }
    }

    private fun cercaTratta() {
        val partenza = binding.inputPartenza.text?.toString()?.trim()
        val destinazione = binding.inputDestinazione.text?.toString()?.trim()

        if (partenza.isNullOrEmpty() || destinazione.isNullOrEmpty()) {
            Toast.makeText(this, "Inserisci partenza e destinazione", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnCercaTratta.isEnabled = false
        binding.progressBarTratta.visibility = android.view.View.VISIBLE
        binding.textNessunRisultato.visibility = android.view.View.GONE

        lifecycleScope.launch {
            // 1. Trova il codice della stazione di partenza
            val stazioneResult = withContext(Dispatchers.IO) { client.autocompletaStazione(partenza) }

            if (stazioneResult !is TrainResult.Success || stazioneResult.data.isEmpty()) {
                binding.btnCercaTratta.isEnabled = true
                binding.progressBarTratta.visibility = android.view.View.GONE
                Toast.makeText(this@RicercaTrattaActivity, "Stazione di partenza non trovata", Toast.LENGTH_LONG).show()
                return@launch
            }

            val stazionePartenza = stazioneResult.data.first()

            // 2. Ottieni le partenze da quella stazione
            val partenzeResult = withContext(Dispatchers.IO) {
                client.partenzeDaStazione(stazionePartenza.codice, stazionePartenza.nome)
            }

            binding.btnCercaTratta.isEnabled = true
            binding.progressBarTratta.visibility = android.view.View.GONE

            when (partenzeResult) {
                is TrainResult.Success -> {
                    // 3. Filtra per destinazione (match parziale, case-insensitive)
                    val filtrate = partenzeResult.data.filter {
                        it.destinazione.contains(destinazione, ignoreCase = true)
                    }
                    if (filtrate.isEmpty()) {
                        partenzeAdapter.aggiorna(emptyList())
                        binding.textNessunRisultato.visibility = android.view.View.VISIBLE
                        binding.textNessunRisultato.text =
                            "Nessun treno diretto verso \"$destinazione\" in partenza ora da ${stazionePartenza.nome}.\n\n" +
                            "Nota: vengono mostrati solo i treni in partenza nelle prossime ore."
                    } else {
                        partenzeAdapter.aggiorna(filtrate)
                    }
                }
                is TrainResult.NoData -> {
                    partenzeAdapter.aggiorna(emptyList())
                    binding.textNessunRisultato.visibility = android.view.View.VISIBLE
                    binding.textNessunRisultato.text = partenzeResult.message
                }
                else -> {
                    Toast.makeText(this@RicercaTrattaActivity, "Errore di rete. Riprova.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun aggiungiTreno(partenza: PartenzaTreno) {
        binding.progressBarTratta.visibility = android.view.View.VISIBLE
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                client.andamentoTreno(partenza.codStazionePartenza, partenza.numeroTreno, partenza.timestampMs)
            }
            binding.progressBarTratta.visibility = android.view.View.GONE

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
                    withContext(Dispatchers.IO) {
                        AppDatabase.getInstance(this@RicercaTrattaActivity).trenoDao().inserisci(treno)
                    }
                    Toast.makeText(this@RicercaTrattaActivity, "Treno ${partenza.numeroTreno} aggiunto al monitoraggio", Toast.LENGTH_SHORT).show()
                    finish()
                    overridePendingTransition(R.anim.scale_up_enter, R.anim.slide_down_exit)
                }
                else -> {
                    Toast.makeText(this@RicercaTrattaActivity, "Impossibile aggiungere questo treno ora.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
