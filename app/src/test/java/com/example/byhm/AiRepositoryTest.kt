package com.example.byhm

import com.example.byhm.data.repository.AiRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AiRepositoryTest {

    private lateinit var repository: AiRepository

    @Before
    fun setUp() {
        repository = AiRepository()
    }

    @Test
    fun testGreetingsHandling() = runBlocking {
        val greetings = listOf("Hi", "Hello", "Hey", "Good morning", "Namaste", "Namaskar", "Hi!", "नमस्ते")

        for (greeting in greetings) {
            val response = repository.generateConversationalResponse(greeting, null)
            assertFalse(
                "Greeting '$greeting' should not trigger backend-unavailable error",
                response.contains("backend AI service is currently unavailable", ignoreCase = true)
            )
            assertFalse(
                "Greeting '$greeting' should not trigger offline note",
                response.contains("Python AI Backend abhi offline hai", ignoreCase = true)
            )
            assertTrue(
                "Greeting '$greeting' should receive a welcoming response",
                response.contains("Disaster Responder", ignoreCase = true) ||
                        response.contains("सहायक", ignoreCase = true) ||
                        response.contains("assistant", ignoreCase = true)
            )
        }
    }

    @Test
    fun testThanksHandling() = runBlocking {
        val thanksList = listOf("Thanks", "Thank you", "Dhanyawad", "Shukriya", "धन्यवाद")

        for (thanks in thanksList) {
            val response = repository.generateConversationalResponse(thanks, null)
            assertFalse(
                "Thanks '$thanks' should not trigger backend-unavailable error",
                response.contains("backend AI service is currently unavailable", ignoreCase = true)
            )
            assertTrue(
                "Thanks '$thanks' should receive an acknowledgement response",
                response.contains("welcome", ignoreCase = true) ||
                        response.contains("swagat", ignoreCase = true) ||
                        response.contains("स्वागत", ignoreCase = true)
            )
        }
    }

    @Test
    fun testFireSafetyGuidance() = runBlocking {
        val fireQuery = "Aag lag gayi hai main market mein"
        val response = repository.generateConversationalResponse(fireQuery, null)

        assertTrue(
            "Fire message should contain fire evacuation steps or emergency number 101/112",
            response.contains("Fire", ignoreCase = true) || response.contains("101") || response.contains("112")
        )
    }

    @Test
    fun testFloodSafetyGuidance() = runBlocking {
        val floodQuery = "Waterlogging and flood near main road"
        val response = repository.generateConversationalResponse(floodQuery, null)

        assertTrue(
            "Flood message should contain flood safety guidance or higher ground advice",
            response.contains("Flood", ignoreCase = true) || response.contains("higher ground", ignoreCase = true)
        )
    }
}
