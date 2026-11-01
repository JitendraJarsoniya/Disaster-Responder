package com.example.byhm.data.domain

import com.example.byhm.data.model.AlertItem
import com.example.byhm.data.model.AlertVerificationStatus
import com.example.byhm.data.model.CorroboratingSource
import com.example.byhm.data.model.OfficialSourceAuthority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlertCorrelationEngine {

    fun correlateAndDeduplicate(
        existingAlerts: List<AlertItem>,
        incomingAlerts: List<AlertItem>
    ): List<AlertItem> {
        val timeNow = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
        val resultMap = existingAlerts.associateBy { it.id }.toMutableMap()

        for (incoming in incomingAlerts) {
            // Find duplicate or matching event by location and core topic
            val matchKey = resultMap.values.find { existing ->
                isSameEvent(existing, incoming)
            }

            if (matchKey != null) {
                // Combine corroborating sources
                val combinedSources = (matchKey.corroboratingSources + incoming.corroboratingSources).distinctBy { it.sourceAuthority }
                val isMultiCorroborated = combinedSources.size >= 2
                val updatedStatus = if (isMultiCorroborated || matchKey.isVerifiedOfficial) {
                    AlertVerificationStatus.PUBLISHED
                } else {
                    AlertVerificationStatus.UNCONFIRMED
                }

                val mergedAlert = matchKey.copy(
                    verificationStatus = updatedStatus,
                    corroboratingSources = combinedSources,
                    lastCheckedTimestamp = timeNow,
                    isCorroboratedByMultipleSources = isMultiCorroborated,
                    unconfirmedReason = if (isMultiCorroborated) null else "Awaiting second independent official authority corroboration."
                )
                resultMap[matchKey.id] = mergedAlert
            } else {
                val evaluated = evaluateVerificationStatus(incoming, timeNow)
                resultMap[incoming.id] = evaluated
            }
        }

        return resultMap.values.toList()
    }

    private fun isSameEvent(a: AlertItem, b: AlertItem): Boolean {
        if (a.id == b.id) return true
        val locMatch = a.affectedArea.contains(b.affectedArea, ignoreCase = true) ||
                b.affectedArea.contains(a.affectedArea, ignoreCase = true)
        val titleMatch = a.title.take(15).lowercase() == b.title.take(15).lowercase()
        return locMatch && titleMatch
    }

    private fun evaluateVerificationStatus(alert: AlertItem, timeNow: String): AlertItem {
        val hasOfficialAuthority = alert.issuingAuthority.contains("NDMA") ||
                alert.issuingAuthority.contains("IMD") ||
                alert.issuingAuthority.contains("CWC") ||
                alert.issuingAuthority.contains("SDMA")

        val initialSources = if (hasOfficialAuthority) {
            listOf(
                CorroboratingSource(
                    sourceAuthority = if (alert.issuingAuthority.contains("IMD")) OfficialSourceAuthority.IMD else OfficialSourceAuthority.NDMA_SACHET,
                    externalAlertId = alert.id,
                    lastCheckedTimestamp = timeNow,
                    sourceNote = "Official issuing authority advisory active"
                )
            )
        } else emptyList()

        val status = if (alert.isVerifiedOfficial || hasOfficialAuthority) {
            AlertVerificationStatus.PUBLISHED
        } else {
            AlertVerificationStatus.UNCONFIRMED
        }

        return alert.copy(
            verificationStatus = status,
            corroboratingSources = initialSources,
            lastCheckedTimestamp = timeNow,
            unconfirmedReason = if (status == AlertVerificationStatus.UNCONFIRMED) "Single unverified community source. Pending official SDMA verification." else null
        )
    }
}
