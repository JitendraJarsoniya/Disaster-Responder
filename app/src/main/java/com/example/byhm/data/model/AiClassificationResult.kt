package com.example.byhm.data.model

data class AiClassificationResult(
    val predictedCategory: IncidentCategory,
    val confidenceScore: Float,
    val summaryText: String,
    val detectedLocationKeyword: String?,
    val recommendedActions: List<String>,
    val modelVersion: String = "v1.2-student-prototype",
    val isSimulation: Boolean = true
)
