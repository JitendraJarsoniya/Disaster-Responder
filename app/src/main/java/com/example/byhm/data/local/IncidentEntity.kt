package com.example.byhm.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.byhm.data.model.IncidentCategory
import com.example.byhm.data.model.IncidentStatus
import com.example.byhm.data.model.IncidentReport
import com.example.byhm.data.model.AuditTrailItem
import com.example.byhm.data.model.SyncState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val reporterName: String,
    val categoryName: String,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val statusName: String,
    val photoUrl: String?,
    val auditTrailJson: String,
    val isVerified: Boolean,
    val syncStateName: String
) {
    fun toIncidentReport(gson: Gson): IncidentReport {
        val auditType = object : TypeToken<List<AuditTrailItem>>() {}.type
        val auditList: List<AuditTrailItem> = gson.fromJson(auditTrailJson, auditType) ?: emptyList()

        return IncidentReport(
            id = id,
            reporterId = reporterId,
            reporterName = reporterName,
            category = IncidentCategory.valueOf(categoryName),
            description = description,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            timestamp = timestamp,
            status = IncidentStatus.valueOf(statusName),
            photoUrl = photoUrl,
            auditTrail = auditList,
            isVerified = isVerified,
            syncState = SyncState.valueOf(syncStateName)
        )
    }

    companion object {
        fun fromIncidentReport(report: IncidentReport, gson: Gson): IncidentEntity {
            return IncidentEntity(
                id = report.id,
                reporterId = report.reporterId,
                reporterName = report.reporterName,
                categoryName = report.category.name,
                description = report.description,
                locationName = report.locationName,
                latitude = report.latitude,
                longitude = report.longitude,
                timestamp = report.timestamp,
                statusName = report.status.name,
                photoUrl = report.photoUrl,
                auditTrailJson = gson.toJson(report.auditTrail),
                isVerified = report.isVerified,
                syncStateName = report.syncState.name
            )
        }
    }
}
