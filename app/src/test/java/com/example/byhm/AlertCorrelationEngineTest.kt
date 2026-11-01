package com.example.byhm

import com.example.byhm.data.domain.AlertCorrelationEngine
import com.example.byhm.data.model.AlertItem
import com.example.byhm.data.model.AlertSeverity
import com.example.byhm.data.model.AlertVerificationStatus
import com.example.byhm.data.model.CorroboratingSource
import com.example.byhm.data.model.OfficialSourceAuthority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertCorrelationEngineTest {

    @Test
    fun testAlertDeduplicationAndMultiSourceCorroboration() {
        val alert1 = AlertItem(
            id = "alert_yamuna_01",
            title = "Yamuna Flood Level 2 Warning",
            description = "River level reached 205.8m",
            severity = AlertSeverity.CRITICAL,
            issuingAuthority = "NDMA SACHET",
            affectedArea = "Delhi NCR - Yamuna Floodplain",
            publishedTime = "10:00 IST",
            validUntil = "22:00 IST",
            isVerifiedOfficial = true,
            corroboratingSources = listOf(
                CorroboratingSource(OfficialSourceAuthority.NDMA_SACHET, "ndma_101", "10:00 IST", "Active Advisory")
            )
        )

        val alert2Duplicate = AlertItem(
            id = "alert_yamuna_02",
            title = "Yamuna Flood Level 2 Warning",
            description = "CWC Flood Advisory for Yamuna",
            severity = AlertSeverity.CRITICAL,
            issuingAuthority = "Central Water Commission (CWC)",
            affectedArea = "Delhi NCR - Yamuna Floodplain",
            publishedTime = "10:15 IST",
            validUntil = "22:00 IST",
            isVerifiedOfficial = true,
            corroboratingSources = listOf(
                CorroboratingSource(OfficialSourceAuthority.CWC, "cwc_302", "10:15 IST", "Gauge level confirmed")
            )
        )

        val correlated = AlertCorrelationEngine.correlateAndDeduplicate(listOf(alert1), listOf(alert2Duplicate))

        assertEquals(1, correlated.size)
        val merged = correlated.first()
        assertTrue("Alert corroborated by 2 sources must be flagged multi-corroborated", merged.isCorroboratedByMultipleSources)
        assertEquals(2, merged.corroboratingSources.size)
        assertEquals(AlertVerificationStatus.PUBLISHED, merged.verificationStatus)
    }

    @Test
    fun testUnconfirmedSingleSourceAlert() {
        val unconfirmedAlert = AlertItem(
            id = "alert_unconfirmed_99",
            title = "Local storm rumor in alleyway",
            description = "Unverified local report",
            severity = AlertSeverity.INFO,
            issuingAuthority = "Community Reporter",
            affectedArea = "Unknown Sector",
            publishedTime = "12:00 IST",
            validUntil = "18:00 IST",
            isVerifiedOfficial = false
        )

        val correlated = AlertCorrelationEngine.correlateAndDeduplicate(emptyList(), listOf(unconfirmedAlert))

        assertEquals(1, correlated.size)
        val result = correlated.first()
        assertEquals(AlertVerificationStatus.UNCONFIRMED, result.verificationStatus)
        assertFalse(result.isCorroboratedByMultipleSources)
        assertTrue(result.unconfirmedReason!!.contains("Single unverified"))
    }
}
