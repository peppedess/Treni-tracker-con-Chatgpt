package com.treni.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stazioni_recenti")
data class StazioneRecente(
    @PrimaryKey
    val nome: String,
    val ultimaRicerca: Long = System.currentTimeMillis()
)
