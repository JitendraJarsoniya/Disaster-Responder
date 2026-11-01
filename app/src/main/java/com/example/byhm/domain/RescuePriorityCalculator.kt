package com.example.byhm.domain

import com.example.byhm.data.model.IncidentCategory
import com.example.byhm.data.model.IncidentReport

enum class PriorityLevel(val displayName: String) {
    CRITICAL("CRITICAL PRIORITY (P1)"),
    HIGH("HIGH PRIORITY (P2)"),
    MEDIUM("MEDIUM PRIORITY (P3)"),
    LOW("LOW PRIORITY (P4)")
}

data class PriorityEvaluation(
    val level: PriorityLevel,
    val score: Int,
    val reasons: List<String>
)

object RescuePriorityCalculator {

    fun calculatePriority(report: IncidentReport): PriorityEvaluation {
        var score = 0
        val reasons = mutableListOf<String>()

        // 1. Base Category Scoring
        when (report.category) {
            IncidentCategory.EARTHQUAKE -> {
                score += 35
                reasons.add("High structural collapse & post-shake hazard (+35)")
            }
            IncidentCategory.FIRE -> {
                score += 30
                reasons.add("Active thermal & smoke inhalation hazard (+30)")
            }
            IncidentCategory.FLOOD -> {
                score += 25
                reasons.add("Rising water & submerged infrastructure hazard (+25)")
            }
            IncidentCategory.CYCLONE -> {
                score += 20
                reasons.add("High wind gusts & projectile hazard (+20)")
            }
            IncidentCategory.OTHER -> {
                score += 10
                reasons.add("Standard emergency category baseline (+10)")
            }
        }

        val desc = report.description.lowercase()

        // 2. Immediate Danger & Stranded Individuals
        val dangerKeywords = listOf("trapped", "stranded", "submerged", "danger", "fasa", "fase", "फंसा", "फंसे", "rescue")
        if (dangerKeywords.any { desc.contains(it) }) {
            score += 25
            reasons.add("Keywords indicate trapped or stranded victims (+25)")
        }

        // 3. Casualties and Medical Emergency
        val medicalKeywords = listOf("injured", "bleeding", "wound", "unconscious", "casualty", "chot", "khoon", "चोट", "खून", "दर्द")
        if (medicalKeywords.any { desc.contains(it) }) {
            score += 25
            reasons.add("Medical casualties or injuries detected in report (+25)")
        }

        // 4. Critical Infrastructure Impact
        val infraKeywords = listOf("hospital", "bridge", "school", "highway", "power", "transformer")
        if (infraKeywords.any { desc.contains(it) }) {
            score += 15
            reasons.add("Impacts vital community infrastructure (+15)")
        }

        // 5. Verification Boost
        if (report.isVerified) {
            score += 10
            reasons.add("Report confirmed & verified by Administrator (+10)")
        }

        // Determine Final Priority Level
        val level = when {
            score >= 60 -> PriorityLevel.CRITICAL
            score >= 40 -> PriorityLevel.HIGH
            score >= 20 -> PriorityLevel.MEDIUM
            else -> PriorityLevel.LOW
        }

        if (reasons.isEmpty()) {
            reasons.add("No critical risk factors identified. Default baseline priority assigned.")
        }

        return PriorityEvaluation(
            level = level,
            score = score,
            reasons = reasons
        )
    }
}
