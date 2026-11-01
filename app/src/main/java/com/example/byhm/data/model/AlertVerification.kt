package com.example.byhm.data.model

enum class AlertVerificationStatus(val displayName: String) {
    PENDING_VERIFICATION("Pending Review"),
    UNCONFIRMED("Unconfirmed Report"),
    VERIFIED("Official Warning"),
    PUBLISHED("Published Alert"),
    EXPIRED("Expired Advisory")
}

enum class OfficialSourceAuthority(
    val fullName: String,
    val websiteUrl: String,
    val isApiAccessible: Boolean,
    val unavailableReason: String? = null
) {
    IMD(
        fullName = "India Meteorological Department (IMD)",
        websiteUrl = "https://mausam.imd.gov.in",
        isApiAccessible = false,
        unavailableReason = "Public web portal active. Direct REST/JSON API endpoint requires official IMD credentials."
    ),
    NDMA_SACHET(
        fullName = "NDMA SACHET National Disaster Portal",
        websiteUrl = "https://sachet.ndma.gov.in",
        isApiAccessible = false,
        unavailableReason = "Web dashboard active. Direct RSS feed integration pending government server clearance."
    ),
    CWC(
        fullName = "Central Water Commission Flood Forecast (CWC)",
        websiteUrl = "https://ffs.rc.cwc.gov.in",
        isApiAccessible = false,
        unavailableReason = "Public flood portal active. Automated API feed requires SDMA network token."
    ),
    NDRF(
        fullName = "National Disaster Response Force (NDRF)",
        websiteUrl = "https://ndrf.gov.in",
        isApiAccessible = false,
        unavailableReason = "Operational advisory portal active. Direct push notification API unavailable."
    ),
    INCOIS(
        fullName = "Indian National Centre for Ocean Information Services (INCOIS)",
        websiteUrl = "https://incois.gov.in",
        isApiAccessible = false,
        unavailableReason = "Coastal warning portal active. Direct API feed requires INCOIS authorization."
    ),
    SDMA_LOCAL(
        fullName = "State & District Disaster Management Authority",
        websiteUrl = "https://ndma.gov.in/State-Disaster-Management-Authorities",
        isApiAccessible = true,
        unavailableReason = null
    )
}

data class CorroboratingSource(
    val sourceAuthority: OfficialSourceAuthority,
    val externalAlertId: String,
    val lastCheckedTimestamp: String,
    val sourceNote: String
)
