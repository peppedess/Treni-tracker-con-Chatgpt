package com.treni.tracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StazioneRecenteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salva(stazione: StazioneRecente)

    @Query("SELECT * FROM stazioni_recenti ORDER BY ultimaRicerca DESC LIMIT 5")
    suspend fun recenti(): List<StazioneRecente>
}
