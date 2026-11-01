package com.example.byhm.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.byhm.data.map.MapLayerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode (Red & White)"),
    DARK("Dark Mode (Emergency Slate)")
}

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी (Hindi)")
}

class AppPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("disaster_responder_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(getSavedLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _savedMapLayer = MutableStateFlow(getSavedMapLayer())
    val savedMapLayer: StateFlow<MapLayerType> = _savedMapLayer.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    private fun getSavedThemeMode(): AppThemeMode {
        val name = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        return try {
            AppThemeMode.valueOf(name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString("app_language", lang.name).apply()
        _language.value = lang
    }

    private fun getSavedLanguage(): AppLanguage {
        val name = prefs.getString("app_language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name
        return try {
            AppLanguage.valueOf(name)
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }
    }

    fun setSavedMapLayer(layer: MapLayerType) {
        prefs.edit().putString("map_layer", layer.name).apply()
        _savedMapLayer.value = layer
    }

    private fun getSavedMapLayer(): MapLayerType {
        // Defaults to MapLayerType.HYBRID for new installations
        val name = prefs.getString("map_layer", MapLayerType.HYBRID.name) ?: MapLayerType.HYBRID.name
        return try {
            MapLayerType.valueOf(name)
        } catch (e: Exception) {
            MapLayerType.HYBRID
        }
    }
}
