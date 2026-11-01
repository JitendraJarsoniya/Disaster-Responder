package com.example.byhm.data.model

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val role: UserRole = UserRole.RESIDENT,
    val organization: String = "",
    val isVerifiedResponder: Boolean = false,
    val preferredLanguage: String = "en"
)
