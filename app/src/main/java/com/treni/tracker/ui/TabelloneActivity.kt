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

    private fun richiediPosizione() {
        val locationManager = getSystemService(LocationManager::class.java) ?: return
        try {
            LocationManagerCompat.getCurrentLocation(
                locationManager,
                LocationManager.NETWORK_PROVIDER,
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
}
