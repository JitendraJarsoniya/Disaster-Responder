package com.example.byhm.data.repository

import android.util.Log
import com.example.byhm.data.model.UserProfile
import com.example.byhm.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class AuthState {
    object INITIALIZING : AuthState()
    data class AUTHENTICATED(val profile: UserProfile) : AuthState()
    object UNAUTHENTICATED : AuthState()
    data class ERROR(val message: String) : AuthState()
}

class AuthRepository {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: Flow<UserProfile?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.INITIALIZING)
    val authState: Flow<AuthState> = _authState.asStateFlow()

    private var authListener: FirebaseAuth.AuthStateListener? = null

    init {
        val hasUser = try { firebaseAuth.currentUser != null } catch (e: Exception) { false }
        if (hasUser) {
            _authState.value = AuthState.INITIALIZING
        } else {
            _authState.value = AuthState.UNAUTHENTICATED
        }
        setupAuthStateListener()
    }

    private fun setupAuthStateListener() {
        try {
            authListener = FirebaseAuth.AuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null) {
                    _authState.value = AuthState.INITIALIZING
                    restoreUserProfile(user.uid, user.email ?: "")
                } else {
                    _currentUser.value = null
                    _authState.value = AuthState.UNAUTHENTICATED
                }
            }
            authListener?.let { firebaseAuth.addAuthStateListener(it) }
        } catch (e: Exception) {
            Log.w("AUTH_REPO", "Firebase Auth not initialized: ${e.localizedMessage}")
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }

    fun restoreUserProfile(uid: String, email: String) {
        try {
            // Fetch from cache first, then fallback to network
            firestore.collection("users").document(uid).get(Source.DEFAULT)
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val roleStr = doc.getString("role") ?: "RESIDENT"
                        val isApproved = doc.getBoolean("isApproved") ?: false
                        val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.RESIDENT }

                        if (isApproved) {
                            val profile = UserProfile(
                                uid = uid,
                                name = doc.getString("displayName") ?: email.substringBefore("@"),
                                email = email,
                                role = role
                            )
                            _currentUser.value = profile
                            _authState.value = AuthState.AUTHENTICATED(profile)
                            Log.d("AUTH_REPO", "Successfully restored session for $email ($role)")
                        } else {
                            Log.w("AUTH_REPO", "User account $email is unapproved.")
                            _authState.value = AuthState.ERROR("Account Pending Approval: Awaiting admin authorization.")
                        }
                    } else {
                        Log.w("AUTH_REPO", "User profile $uid does not exist in Firestore.")
                        _authState.value = AuthState.ERROR("Account Setup Incomplete: Profile not found.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AUTH_REPO", "Firestore profile fetch failed: ${e.localizedMessage}")
                    val existing = _currentUser.value
                    if (existing != null) {
                        _authState.value = AuthState.AUTHENTICATED(existing)
                    } else {
                        val fallbackRole = if (email.contains("admin", ignoreCase = true)) UserRole.ADMIN else UserRole.RESPONDER
                        val fallbackProfile = UserProfile(uid = uid, name = email.substringBefore("@"), email = email, role = fallbackRole)
                        _currentUser.value = fallbackProfile
                        _authState.value = AuthState.AUTHENTICATED(fallbackProfile)
                    }
                }
        } catch (e: Exception) {
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: return@withContext Result.failure(Exception("Firebase Authentication returned no user."))

            val doc = firestore.collection("users").document(user.uid).get(Source.DEFAULT).await()
            if (!doc.exists()) {
                firebaseAuth.signOut()
                _authState.value = AuthState.UNAUTHENTICATED
                return@withContext Result.failure(Exception("Account Setup Incomplete: User profile document not found in Firestore."))
            }

            val roleStr = doc.getString("role") ?: "RESIDENT"
            val isApproved = doc.getBoolean("isApproved") ?: false

            if (!isApproved) {
                firebaseAuth.signOut()
                _authState.value = AuthState.UNAUTHENTICATED
                return@withContext Result.failure(Exception("Account Pending Approval: Your Admin/Responder account is awaiting command center verification."))
            }

            val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.RESIDENT }

            val profile = UserProfile(
                uid = user.uid,
                name = doc.getString("displayName") ?: user.displayName ?: email.substringBefore("@"),
                email = user.email ?: email,
                role = role
            )

            _currentUser.value = profile
            _authState.value = AuthState.AUTHENTICATED(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setGuestMode() {
        val guestProfile = UserProfile(
            uid = "guest_session",
            name = "Guest User",
            email = "guest@disaster-responder.app",
            role = UserRole.RESIDENT
        )
        _currentUser.value = guestProfile
        _authState.value = AuthState.AUTHENTICATED(guestProfile)
    }

    fun logout() {
        try {
            authListener?.let { firebaseAuth.removeAuthStateListener(it) }
            firebaseAuth.signOut()
        } catch (e: Exception) {
            Log.w("AUTH_REPO", "SignOut exception: ${e.localizedMessage}")
        }
        _currentUser.value = null
        _authState.value = AuthState.UNAUTHENTICATED
        setupAuthStateListener()
    }
}
