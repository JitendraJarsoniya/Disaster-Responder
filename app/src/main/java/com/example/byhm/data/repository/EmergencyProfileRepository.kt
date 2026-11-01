package com.example.byhm.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

data class EmergencyProfileData(
    val fullName: String,
    val mobileNumber: String,
    val emergencyContact: String,
    val medicalNotes: String,
    val photoUris: List<String> = emptyList()
)

class EmergencyProfileRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("emergency_profile_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getProfile(): EmergencyProfileData? {
        val name = prefs.getString("full_name", null) ?: return null
        val phone = prefs.getString("mobile_number", "") ?: ""
        val contact = prefs.getString("emergency_contact", "") ?: ""
        val notes = prefs.getString("medical_notes", "") ?: ""
        val photoUrisJson = prefs.getString("photo_uris_json", "[]") ?: "[]"
        val photoUris = try {
            gson.fromJson(photoUrisJson, Array<String>::class.java)?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return EmergencyProfileData(name, phone, contact, notes, photoUris)
    }

    fun saveProfile(profile: EmergencyProfileData) {
        prefs.edit()
            .putString("full_name", profile.fullName)
            .putString("mobile_number", profile.mobileNumber)
            .putString("emergency_contact", profile.emergencyContact)
            .putString("medical_notes", profile.medicalNotes)
            .putString("photo_uris_json", gson.toJson(profile.photoUris))
            .apply()
    }

    fun deleteProfile() {
        prefs.edit().clear().apply()
    }
}
