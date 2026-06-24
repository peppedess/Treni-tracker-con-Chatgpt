package com.treni.tracker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.treni.tracker.ui.screens.TabelloneScreen
import com.treni.tracker.ui.theme.TreniTrackerTheme
import java.util.concurrent.Executors

class TabelloneActivity : ComponentActivity() {

    private val viewModel: TabelloneViewModel by lazy {
        androidx.lifecycle.ViewModelProvider(this)[TabelloneViewModel::class.java]
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concesso ->
        if (concesso) richiediPosizione()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chiediPermessoPosizione()

        setContent {
            TreniTrackerTheme {
                TabelloneScreen(
                    onIndietro = { finish() },
                    viewModel = viewModel
                )
            }
        }
    }

    private fun chiediPermessoPosizione() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            richiediPosizione()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    /**
     * Tenta di ottenere la posizione in due fasi: prima un risultato
     * immediato (anche impreciso) dall'ultima posizione conosciuta dal
     * sistema, poi un aggiornamento più fresco tramite il provider
     * effettivamente abilitato (GPS o rete, secondo cosa è attivo sul
     * telefono — chiedere a un provider disattivato non risponde mai).
     */
    private fun richiediPosizione() {
        val locationManager = getSystemService(LocationManager::class.java) ?: return

        try {
            // Fase 1: risultato immediato dall'ultima posizione nota, se esiste
            val providerImmediato = scegliProviderAttivo(locationManager) ?: return
            val ultimaNota = locationManager.getLastKnownLocation(providerImmediato)
            ultimaNota?.let {
                viewModel.aggiornaStazioniVicine(it.latitude, it.longitude)
            }

            // Fase 2: aggiornamento più fresco in background
            LocationManagerCompat.getCurrentLocation(
                locationManager,
                providerImmediato,
                null as androidx.core.os.CancellationSignal?,
                Executors.newSingleThreadExecutor(),
                androidx.core.util.Consumer<android.location.Location?> { location ->
                    location?.let {
                        viewModel.aggiornaStazioniVicine(it.latitude, it.longitude)
                    }
                }
            )
        } catch (e: SecurityException) {
            // Permesso revocato nel frattempo: nessuna stazione vicina mostrata,
            // il resto della schermata (recenti, ricerca manuale) resta funzionante.
        }
    }

    /**
     * Sceglie GPS se attivo, altrimenti rete se attiva, altrimenti null.
     * Chiedere la posizione a un provider disattivato non genera errore
     * ma semplicemente non risponde mai: è per questo che va verificato
     * esplicitamente prima di interrogarlo.
     */
    private fun scegliProviderAttivo(locationManager: LocationManager): String? {
        return when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
    }
}
