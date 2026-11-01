package com.example.byhm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.byhm.DisasterResponderApplication
import com.example.byhm.data.model.IncidentCategory
import com.example.byhm.data.model.IncidentReport
import com.example.byhm.data.model.IncidentStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IncidentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as DisasterResponderApplication).incidentRepository

    val incidents: StateFlow<List<IncidentReport>> = repository.incidents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun submitReport(
        reporterId: String,
        reporterName: String,
        category: IncidentCategory,
        description: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        photoUrl: String? = null
    ) {
        viewModelScope.launch {
            repository.submitReport(
                reporterId = reporterId,
                reporterName = reporterName,
                category = category,
                description = description,
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                photoUrl = photoUrl
            )
        }
    }

    fun updateStatus(
        incident: IncidentReport,
        newStatus: IncidentStatus,
        updatedBy: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.updateIncidentStatus(
                incident = incident,
                newStatus = newStatus,
                updatedBy = updatedBy,
                notes = notes
            )
        }
    }
}
