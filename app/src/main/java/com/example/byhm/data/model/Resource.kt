package com.example.byhm.data.model

enum class ResourceCategory(val displayName: String) {
    SHELTER("Emergency Shelter"),
    HOSPITAL("Hospital / Medical Center"),
    FOOD_WATER("Food & Clean Water Distribution"),
    RESCUE("Search & Rescue Service")
}

enum class AvailabilityStatus(val displayName: String) {
    AVAILABLE("High Availability"),
    LIMITED("Limited / Near Capacity"),
    FULL("Full / At Capacity"),
    UNKNOWN("Status Unverified")
}

data class Resource(
    val id: String,
    val name: String,
    val category: ResourceCategory,
    val address: String,
    val contactPhone: String,
    val latitude: Double,
    val longitude: Double,
    val status: AvailabilityStatus,
    val capacityInfo: String,
    val verificationSource: String,
    val lastUpdated: String,
    val isVerified: Boolean = true
)
