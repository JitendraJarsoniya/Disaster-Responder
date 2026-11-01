package com.example.byhm.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIncident(incident: IncidentEntity)

    @Update
    fun updateIncident(incident: IncidentEntity)
}
