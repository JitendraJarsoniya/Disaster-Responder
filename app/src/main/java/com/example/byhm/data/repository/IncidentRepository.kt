package com.example.byhm.data.repository

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.byhm.data.local.IncidentDao
import com.example.byhm.data.local.IncidentEntity
import com.example.byhm.data.model.AuditTrailItem
import com.example.byhm.data.model.IncidentCategory
import com.example.byhm.data.model.IncidentReport
import com.example.byhm.data.model.IncidentStatus
import com.example.byhm.data.model.SyncState
import com.example.byhm.workers.IncidentSyncWorker
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncidentRepository(
    private val incidentDao: IncidentDao,
    private val context: Context
) {

    private val gson = Gson()

    // Stream directly from Room database with automatic sample seeding on first run
    val incidents: Flow<List<IncidentReport>> = incidentDao.getAllIncidents().map { entities ->
        if (entities.isEmpty()) {
            withContext(Dispatchers.IO) {
                SampleData.sampleIncidents.forEach { sample ->
                    try {
                        incidentDao.insertIncident(IncidentEntity.fromIncidentReport(sample, gson))
                    } catch (e: Exception) {
                        Log.w("INCIDENT_REPO", "Failed to seed sample incident: ${e.localizedMessage}")
                    }
                }
            }
            SampleData.sampleIncidents
        } else {
            entities.map { it.toIncidentReport(gson) }
        }
    }

    suspend fun submitReport(
        reporterId: String,
        reporterName: String,
        category: IncidentCategory,
        description: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        photoUrl: String? = null
    ): IncidentReport {
        val timestampMillis = System.currentTimeMillis()
        val newId = "inc_$timestampMillis"
        val timeNow = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())

        val initialAudit = AuditTrailItem(
            status = IncidentStatus.SUBMITTED,
            timestamp = timeNow,
            updatedBy = reporterName,
            notes = "Report submitted via Mobile App"
        )

        val newReport = IncidentReport(
            id = newId,
            reporterId = reporterId,
            reporterName = reporterName,
            category = category,
            description = description,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            timestamp = timeNow,
            status = IncidentStatus.SUBMITTED,
            photoUrl = photoUrl,
            auditTrail = listOf(initialAudit),
            isVerified = false,
            syncState = SyncState.SYNCED
        )

        withContext(Dispatchers.IO) {
            incidentDao.insertIncident(IncidentEntity.fromIncidentReport(newReport, gson))
            Log.d("INCIDENT_REPO", "Inserted new report $newId into Room DB at location: $locationName ($latitude, $longitude)")
            enqueueSyncWorker(newId)
        }
        return newReport
    }

    private fun enqueueSyncWorker(incidentId: String) {
        val workData = Data.Builder()
            .putString("INCIDENT_ID", incidentId)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<IncidentSyncWorker>()
            .setInputData(workData)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }

    suspend fun updateIncidentStatus(
        incident: IncidentReport,
        newStatus: IncidentStatus,
        updatedBy: String,
        notes: String
    ) {
        val timeNow = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
        val newAudit = AuditTrailItem(
            status = newStatus,
            timestamp = timeNow,
            updatedBy = updatedBy,
            notes = notes
        )
        val updatedReport = incident.copy(
            status = newStatus,
            isVerified = newStatus != IncidentStatus.SUBMITTED && newStatus != IncidentStatus.REJECTED,
            auditTrail = incident.auditTrail + newAudit
        )
        withContext(Dispatchers.IO) {
            incidentDao.updateIncident(IncidentEntity.fromIncidentReport(updatedReport, gson))
            enqueueSyncWorker(updatedReport.id)
        }
    }
}
