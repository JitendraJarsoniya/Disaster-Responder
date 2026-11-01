package com.example.byhm.data.repository

import com.example.byhm.data.model.AlertItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlertRepository {

    // Default clean state starts empty
    private val _alerts = MutableStateFlow<List<AlertItem>>(emptyList())
    val alerts: Flow<List<AlertItem>> = _alerts.asStateFlow()

    fun setAlerts(newList: List<AlertItem>) {
        _alerts.value = newList
    }

    fun loadDemoAlertsForTesting() {
        _alerts.value = SampleData.sampleAlerts
    }

    fun publishAlert(newAlert: AlertItem) {
        _alerts.value = listOf(newAlert) + _alerts.value
    }
}
