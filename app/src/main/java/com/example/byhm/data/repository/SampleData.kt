package com.example.byhm.data.repository

import com.example.byhm.data.model.AlertItem
import com.example.byhm.data.model.AlertSeverity
import com.example.byhm.data.model.AuditTrailItem
import com.example.byhm.data.model.AvailabilityStatus
import com.example.byhm.data.model.IncidentCategory
import com.example.byhm.data.model.IncidentReport
import com.example.byhm.data.model.IncidentStatus
import com.example.byhm.data.model.Resource
import com.example.byhm.data.model.ResourceCategory
import com.example.byhm.data.model.UserProfile
import com.example.byhm.data.model.UserRole

object SampleData {

    val sampleUser = UserProfile(
        uid = "user_demo_001",
        name = "Aarav Sharma",
        email = "aarav.student@byhm.edu",
        phone = "+91 98765 43210",
        role = UserRole.RESIDENT,
        preferredLanguage = "en"
    )

    val sampleResponder = UserProfile(
        uid = "resp_demo_101",
        name = "Priya Verma (NDRF Lead)",
        email = "priya.responder@ndrf.gov.in",
        phone = "+91 98123 45678",
        role = UserRole.RESPONDER,
        organization = "National Disaster Response Force (NDRF)",
        isVerifiedResponder = true
    )

    val sampleAdmin = UserProfile(
        uid = "admin_demo_999",
        name = "Dr. Rajesh Kumar",
        email = "rajesh.admin@disaster.gov.in",
        phone = "+91 99999 00000",
        role = UserRole.ADMIN,
        organization = "State Disaster Management Authority"
    )

    val sampleResources = listOf(
        Resource(
            id = "res_101",
            name = "Apex Emergency Care Hospital",
            category = ResourceCategory.HOSPITAL,
            address = "Sector 12, Main Ring Road, New Delhi",
            contactPhone = "+91 11 2658 8500",
            latitude = 28.6139,
            longitude = 77.2090,
            status = AvailabilityStatus.AVAILABLE,
            capacityInfo = "14 ICU beds, Trauma Center Active",
            verificationSource = "District Health Office",
            lastUpdated = "10 mins ago"
        ),
        Resource(
            id = "res_102",
            name = "Central Stadium Relief Shelter",
            category = ResourceCategory.SHELTER,
            address = "Gate 3, Jawaharlal Nehru Stadium, New Delhi",
            contactPhone = "+91 11 2436 0000",
            latitude = 28.5828,
            longitude = 77.2344,
            status = AvailabilityStatus.AVAILABLE,
            capacityInfo = "Capacity: 1200 / Occupied: 350",
            verificationSource = "Municipal Relief Corp",
            lastUpdated = "25 mins ago"
        ),
        Resource(
            id = "res_103",
            name = "Red Cross Food & Water Depot",
            category = ResourceCategory.FOOD_WATER,
            address = "Community Center, Karol Bagh, New Delhi",
            contactPhone = "+91 11 2371 6441",
            latitude = 28.6514,
            longitude = 77.1907,
            status = AvailabilityStatus.LIMITED,
            capacityInfo = "Rations available for 500 families",
            verificationSource = "Indian Red Cross Society",
            lastUpdated = "1 hour ago"
        ),
        Resource(
            id = "res_104",
            name = "NDRF Flood Rescue Unit 4",
            category = ResourceCategory.RESCUE,
            address = "Yamuna Floodplain Relief Base",
            contactPhone = "112 / +91 11 2436 3260",
            latitude = 28.6280,
            longitude = 77.2489,
            status = AvailabilityStatus.AVAILABLE,
            capacityInfo = "6 Motorized Boats & 20 Rescuers on standby",
            verificationSource = "NDRF Control Room",
            lastUpdated = "5 mins ago"
        ),
        Resource(
            id = "res_105",
            name = "Community Health Shelter East",
            category = ResourceCategory.SHELTER,
            address = "Mayur Vihar Phase 1, Delhi",
            contactPhone = "+91 11 2271 9999",
            latitude = 28.6080,
            longitude = 77.2950,
            status = AvailabilityStatus.FULL,
            capacityInfo = "Shelter full - Redirecting to Stadium Shelter",
            verificationSource = "District Magistrate Office",
            lastUpdated = "40 mins ago"
        )
    )

    val sampleIncidents = listOf(
        IncidentReport(
            id = "inc_501",
            reporterId = "user_demo_001",
            reporterName = "Aarav Sharma",
            category = IncidentCategory.FLOOD,
            description = "Yamuna water level crossed danger mark near Old Bridge. Submerged road and stranded 15 cattle herders.",
            locationName = "Old Yamuna Bridge, East Delhi",
            latitude = 28.6600,
            longitude = 77.2500,
            timestamp = "Today, 14:30 IST",
            status = IncidentStatus.ASSIGNED,
            auditTrail = listOf(
                AuditTrailItem(IncidentStatus.SUBMITTED, "14:30 IST", "Aarav Sharma", "Report created via mobile app"),
                AuditTrailItem(IncidentStatus.UNDER_REVIEW, "14:35 IST", "Control Room", "Reviewed for duplicate check"),
                AuditTrailItem(IncidentStatus.VERIFIED, "14:40 IST", "Disaster Mgmt Admin", "Verified by drone survey"),
                AuditTrailItem(IncidentStatus.ASSIGNED, "14:50 IST", "NDRF Lead Priya", "Assigned to Rescue Unit 4")
            ),
            isVerified = true
        ),
        IncidentReport(
            id = "inc_502",
            reporterId = "user_demo_002",
            reporterName = "Meera Patel",
            category = IncidentCategory.FIRE,
            description = "Short circuit caused transformer fire near main market. High thick smoke near commercial shops.",
            locationName = "Lajpat Nagar Market, Delhi",
            latitude = 28.5685,
            longitude = 77.2433,
            timestamp = "Today, 15:10 IST",
            status = IncidentStatus.IN_PROGRESS,
            auditTrail = listOf(
                AuditTrailItem(IncidentStatus.SUBMITTED, "15:10 IST", "Meera Patel", "Report submitted"),
                AuditTrailItem(IncidentStatus.VERIFIED, "15:12 IST", "Fire Control Room", "3 Fire tenders dispatched"),
                AuditTrailItem(IncidentStatus.IN_PROGRESS, "15:20 IST", "Fire Station Lead", "Active firefighting in progress")
            ),
            isVerified = true
        ),
        IncidentReport(
            id = "inc_503",
            reporterId = "user_demo_003",
            reporterName = "Rohan Gupta",
            category = IncidentCategory.CYCLONE,
            description = "Fallen trees blocking main hospital access road due to high wind gusts.",
            locationName = "Outer Ring Road, Rohini",
            latitude = 28.7041,
            longitude = 77.1025,
            timestamp = "Today, 15:45 IST",
            status = IncidentStatus.SUBMITTED,
            auditTrail = listOf(
                AuditTrailItem(IncidentStatus.SUBMITTED, "15:45 IST", "Rohan Gupta", "Awaiting verification")
            ),
            isVerified = false
        )
    )

    val sampleAlerts = listOf(
        AlertItem(
            id = "alert_901",
            title = "Yamuna Flood Alert Level 2 Issued",
            description = "Yamuna river water level reached 205.8 meters. Low-lying area residents near Yamuna Bank are advised to move to designated Stadium Relief Shelters immediately.",
            severity = AlertSeverity.CRITICAL,
            issuingAuthority = "Central Water Commission & SDMA",
            affectedArea = "Yamuna Bank, Laxmi Nagar, Mayur Vihar",
            publishedTime = "Today, 13:00 IST",
            validUntil = "Valid for next 24 Hours",
            isVerifiedOfficial = true
        ),
        AlertItem(
            id = "alert_902",
            title = "Heavy Rainfall & High Wind Gust Advisory",
            description = "IMD forecasts moderate to heavy thunderstorm with wind speeds up to 50 km/h. Avoid standing under loose structures or old trees.",
            severity = AlertSeverity.HIGH,
            issuingAuthority = "India Meteorological Department (IMD)",
            affectedArea = "Delhi NCR Region",
            publishedTime = "Today, 11:30 IST",
            validUntil = "Today, 20:00 IST",
            isVerifiedOfficial = true
        )
    )
}
