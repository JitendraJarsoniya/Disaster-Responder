package com.example.byhm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.byhm.data.map.MapLayerType
import com.example.byhm.data.model.UserProfile
import com.example.byhm.data.repository.AppLanguage
import com.example.byhm.data.repository.AppPreferencesRepository
import com.example.byhm.data.repository.AppThemeMode
import com.example.byhm.data.repository.AuthRepository
import com.example.byhm.data.repository.AuthState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val preferencesRepository = AppPreferencesRepository(application)

    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.INITIALIZING
        )

    val themeMode: StateFlow<AppThemeMode> = preferencesRepository.themeMode
    val language: StateFlow<AppLanguage> = preferencesRepository.language
    val savedMapLayer: StateFlow<MapLayerType> = preferencesRepository.savedMapLayer

    fun signInWithEmail(email: String, pass: String, onResult: (Result<UserProfile>) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, pass)
            onResult(result)
        }
    }

    fun setGuestMode() {
        authRepository.setGuestMode()
    }

    fun logout() {
        authRepository.logout()
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferencesRepository.setThemeMode(mode)
    }

    fun setLanguage(lang: AppLanguage) {
        preferencesRepository.setLanguage(lang)
    }

    fun setSavedMapLayer(layer: MapLayerType) {
        preferencesRepository.setSavedMapLayer(layer)
    }
}
