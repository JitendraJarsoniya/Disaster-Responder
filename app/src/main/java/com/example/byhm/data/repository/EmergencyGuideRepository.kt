package com.example.byhm.data.repository

import android.content.Context
import com.example.byhm.data.model.EmergencyGuide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class EmergencyGuideRepository(private val context: Context) {

    private val gson = Gson()

    fun loadEmergencyGuides(): List<EmergencyGuide> {
        return try {
            val inputStream = context.assets.open("emergency_guides.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<EmergencyGuide>>() {}.type
            gson.fromJson<List<EmergencyGuide>>(reader, type) ?: getBundledFallbackGuides()
        } catch (e: Exception) {
            getBundledFallbackGuides()
        }
    }

    private fun getBundledFallbackGuides(): List<EmergencyGuide> {
        return listOf(
            EmergencyGuide(
                id = "fallback_flood",
                category = "Flood Safety",
                title = "Flood & Waterlogging Safety",
                emergencyWarning = "🚨 DANGER: Do not walk or drive through floodwaters. For emergency rescue call 112.",
                immediateActions = listOf(
                    "Move to upper floors or higher ground immediately.",
                    "Turn off main electrical switch if safe to do so."
                ),
                actionsToAvoid = listOf(
                    "Do not touch submerged electrical equipment.",
                    "Do not drive through moving water."
                ),
                evacuationAndShelter = listOf(
                    "Evacuate to designated relief shelters when instructed by authorities."
                ),
                afterEventPrecautions = listOf(
                    "Drink only boiled or bottled water."
                )
            ),
            EmergencyGuide(
                id = "fallback_fire",
                category = "Fire Safety",
                title = "Fire Safety Protocols",
                emergencyWarning = "🚨 DANGER: Get low below smoke and evacuate. Call 101 or 112.",
                immediateActions = listOf(
                    "Evacuate using stairs. Do not use lifts.",
                    "Crawl low under smoke."
                ),
                actionsToAvoid = listOf(
                    "Do not use elevators.",
                    "Do not re-enter a burning building."
                ),
                evacuationAndShelter = listOf(
                    "Assemble at designated open area outside."
                ),
                afterEventPrecautions = listOf(
                    "Seek medical care for smoke inhalation."
                )
            ),
            EmergencyGuide(
                id = "fallback_earthquake",
                category = "Earthquake Safety",
                title = "Earthquake Tremor Safety",
                emergencyWarning = "🚨 DANGER: Drop, Cover, and Hold On during tremors. Call 112 if trapped.",
                immediateActions = listOf(
                    "Drop under a sturdy table and hold on."
                ),
                actionsToAvoid = listOf(
                    "Do not run outside during shaking."
                ),
                evacuationAndShelter = listOf(
                    "Move to open ground once tremors stop."
                ),
                afterEventPrecautions = listOf(
                    "Inspect utility lines for leaks."
                )
            ),
            EmergencyGuide(
                id = "fallback_weather",
                category = "Severe Weather and Lightning",
                title = "Thunderstorm & Lightning Safety",
                emergencyWarning = "🚨 DANGER: Go indoors during thunderstorms. Call 112 for help.",
                immediateActions = listOf(
                    "Seek shelter in a sturdy building."
                ),
                actionsToAvoid = listOf(
                    "Do not stand under tall trees or metal poles."
                ),
                evacuationAndShelter = listOf(
                    "Stay in interior rooms away from windows."
                ),
                afterEventPrecautions = listOf(
                    "Watch out for fallen trees and power lines."
                )
            )
        )
    }
}
