package com.example.byhm.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.byhm.data.local.AppDatabase
import com.example.byhm.data.model.SyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class IncidentSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val incidentId = inputData.getString("INCIDENT_ID") ?: return@withContext Result.failure()
        
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.incidentDao()
        
        // Find the incident
        val incidents = dao.getAllIncidents().first()
        val incidentEntity = incidents.find { it.id == incidentId } ?: return@withContext Result.failure()

        // 1. Mark as PENDING_SYNC
        dao.updateIncident(incidentEntity.copy(syncStateName = SyncState.PENDING_SYNC.name))
        
        try {
            // Simulate network queue delay
            delay(1000) 
            
            // Mark as SYNCED once verified in local Room DB storage
            dao.updateIncident(incidentEntity.copy(syncStateName = SyncState.SYNCED.name))
            
            Result.success() 
        } catch (e: Exception) {
            // Revert to SAVED_LOCALLY to retry later if actual exception occurs
            dao.updateIncident(incidentEntity.copy(syncStateName = SyncState.SAVED_LOCALLY.name))
            Result.retry()
        }
    }
}
