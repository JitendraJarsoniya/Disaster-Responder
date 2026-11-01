package com.example.byhm.data.model

enum class AlertSeverity(val displayName: String) {
    CRITICAL("CRITICAL WARNING"),
    HIGH("HIGH ALERT"),
    MEDIUM("ADVISORY"),
    INFO("INFORMATIONAL")
}

data class AlertItem(
    val id: String,
    val title: String,
    val description: String,
    val hindiTitle: String? = null,
    val hindiDescription: String? = null,
    val category: String = "Flood",
    val hazardSubtype: String? = "General Hazard",
    val severity: AlertSeverity,
    val officialSeverity: String? = "Official Advisory",
    val issuingAuthority: String,
    val affectedArea: String,
    val publishedTime: String,
    val validUntil: String,
    val isVerifiedOfficial: Boolean = true,
    val sourceType: String = "Official Government Warning",
    val sourceUrl: String = "https://sachet.ndma.gov.in",
    val latitude: Double? = 28.6139,
    val longitude: Double? = 77.2090,
    val isExpired: Boolean = false,
    val safetyInstructions: List<String> = emptyList(),
    val isDemoData: Boolean = false,
    val verificationStatus: AlertVerificationStatus = AlertVerificationStatus.PUBLISHED,
    val corroboratingSources: List<CorroboratingSource> = emptyList(),
    val lastCheckedTimestamp: String = "Just Now",
    val isCorroboratedByMultipleSources: Boolean = false,
    val unconfirmedReason: String? = null,
    val version: Int = 1
)
