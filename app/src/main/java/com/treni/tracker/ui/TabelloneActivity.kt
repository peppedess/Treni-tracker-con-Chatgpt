package com.treni.tracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.treni.tracker.ui.screens.TabelloneScreen
import com.treni.tracker.ui.theme.TreniTrackerTheme

class TabelloneActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TreniTrackerTheme {
                TabelloneScreen(
                    onIndietro = { finish() }
                )
            }
        }
    }
}
