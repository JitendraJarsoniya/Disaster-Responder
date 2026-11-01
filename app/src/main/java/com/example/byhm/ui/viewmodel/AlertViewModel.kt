package com.example.byhm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byhm.data.model.AlertItem
import com.example.byhm.data.repository.AlertRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AlertViewModel(
    private val repository: AlertRepository = AlertRepository()
) : ViewModel() {

    val alerts: StateFlow<List<AlertItem>> = repository.alerts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun publishAlert(alert: AlertItem) {
        repository.publishAlert(alert)
    }
}
