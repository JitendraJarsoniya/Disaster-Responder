package com.example.byhm.data.model

enum class IncidentCategory(val displayName: String) {
    FLOOD("Flood"),
    FIRE("Fire / Explosion"),
    EARTHQUAKE("Earthquake"),
    CYCLONE("Cyclone / Storm"),
    OTHER("Other Emergency")
}

enum class IncidentStatus(val displayName: String) {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    VERIFIED("Verified"),
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    REJECTED("Rejected")
}

enum class SyncState(val displayName: String) {
    SAVED_LOCALLY("Saved Locally"),
    PENDING_SYNC("Pending Sync"),
    SYNCED("Synced"),
    FAILED("Sync Failed")
}

data class AuditTrailItem(
    val status: IncidentStatus,
    val timestamp: String,
    val updatedBy: String,
    val notes: String
)

data class IncidentReport(
    val id: String,
    val reporterId: String,
    val reporterName: String,
    val category: IncidentCategory,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val status: IncidentStatus,
    val photoUrl: String? = null,
    val auditTrail: List<AuditTrailItem> = emptyList(),
    val isDuplicateFlagged: Boolean = false,
    val isVerified: Boolean = false,
    val syncState: SyncState = SyncState.SAVED_LOCALLY
)
