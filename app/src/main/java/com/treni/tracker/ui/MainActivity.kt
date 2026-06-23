package com.treni.tracker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.treni.tracker.R
import com.treni.tracker.notification.Notifier
import com.treni.tracker.ui.screens.HomeScreen
import com.treni.tracker.ui.theme.TreniTrackerTheme
import com.treni.tracker.util.ThemeManager
import com.treni.tracker.worker.TrainCheckWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        Notifier.creaCanale(this)
        chiediPermessoNotifiche()
        pianificaControlloPeriodico()

        setContent {
            TreniTrackerTheme {
                HomeScreen(
                    onApriDettaglio = { id, numero, codPartenza, nomePartenza, destinazione, timestamp ->
                        val intent = Intent(this, TrenoDetailActivity::class.java).apply {
                            putExtra(TrenoDetailActivity.EXTRA_TRENO_ID, id)
                            putExtra(TrenoDetailActivity.EXTRA_NUMERO_TRENO, numero)
                            putExtra(TrenoDetailActivity.EXTRA_STAZIONE_PARTENZA_COD, codPartenza)
                            putExtra(TrenoDetailActivity.EXTRA_STAZIONE_PARTENZA_NOME, nomePartenza)
                            putExtra(TrenoDetailActivity.EXTRA_STAZIONE_DESTINAZIONE_NOME, destinazione)
                            putExtra(TrenoDetailActivity.EXTRA_TIMESTAMP_MS, timestamp)
                        }
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_up_enter, R.anim.scale_down_exit)
                    },
                    onApriRicercaTratta = {
                        startActivity(Intent(this, RicercaTrattaActivity::class.java))
                        overridePendingTransition(R.anim.slide_up_enter, R.anim.scale_down_exit)
                    },
                    onApriSceltaTema = { mostraSceltaTema() }
                )
            }
        }
    }

    private fun chiediPermessoNotifiche() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun pianificaControlloPeriodico() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<TrainCheckWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "controllo_treni",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun mostraSceltaTema() {
        val opzioni = arrayOf("Sistema (automatico)", "Sempre chiaro", "Sempre scuro")
        val temaAttuale = ThemeManager.leggiTema(this)
        AlertDialog.Builder(this)
            .setTitle("Tema dell'app")
            .setSingleChoiceItems(opzioni, temaAttuale) { dialog, scelta ->
                ThemeManager.salvaTema(this, scelta)
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}
