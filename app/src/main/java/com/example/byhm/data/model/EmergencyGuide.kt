package com.example.byhm.data.model

data class EmergencyGuide(
    val id: String,
    val category: String,
    val title: String,
    val emergencyWarning: String,
    val immediateActions: List<String>,
    val actionsToAvoid: List<String>,
    val evacuationAndShelter: List<String>,
    val afterEventPrecautions: List<String>
)
