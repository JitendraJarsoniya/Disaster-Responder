package com.example.byhm.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

data class FreshLocationResult(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestamp: Long,
    val addressName: String,
    val isHighAccuracy: Boolean,
    val provider: String = "GPS/Network"
)

class LocationHelper(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    suspend fun fetchFreshLocation(): Result<FreshLocationResult> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext Result.failure(SecurityException("Location permission denied by user."))
        }

        try {
            val cancellationTokenSource = CancellationTokenSource()

            // 1. Try fetching fresh high-accuracy location first
            var location: Location? = try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await()
            } catch (e: Exception) {
                null
            }

            // 2. Fallback to lastKnownLocation ONLY if fresh (< 60 seconds old and accuracy <= 100m)
            if (location == null) {
                val lastLoc = fusedLocationClient.lastLocation.await()
                if (lastLoc != null) {
                    val ageSeconds = (System.currentTimeMillis() - lastLoc.time) / 1000
                    if (ageSeconds < 60 && lastLoc.accuracy <= 100f) {
                        location = lastLoc
                    }
                }
            }

            if (location == null) {
                return@withContext Result.failure(Exception("GPS location unavailable. Please check device location settings."))
            }

            // 3. Validate coordinates bounds
            val lat = location.latitude
            val lng = location.longitude
            if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0 || (lat == 0.0 && lng == 0.0)) {
                return@withContext Result.failure(Exception("Invalid GPS coordinates received ($lat, $lng)."))
            }

            // 4. Reverse Geocode address
            val addressName = resolveAddress(lat, lng, location.accuracy)

            val freshResult = FreshLocationResult(
                latitude = lat,
                longitude = lng,
                accuracyMeters = location.accuracy,
                timestamp = location.time,
                addressName = addressName,
                isHighAccuracy = location.accuracy <= 50f,
                provider = location.provider ?: "GPS"
            )

            Log.d("LOCATION_HELPER", "Fresh GPS Location Acquired: $addressName ($lat, $lng ±${location.accuracy}m)")
            Result.success(freshResult)

        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun resolveAddress(lat: Double, lng: Double, accuracyMeters: Float = 0f): String {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    val state = addr.adminArea
                    return if (locality != null && state != null && !locality.equals(state, ignoreCase = true)) {
                        "$locality, $state, India"
                    } else if (locality != null) {
                        "$locality, India"
                    } else if (state != null) {
                        "$state, India"
                    } else {
                        "GPS Location: %.4f, %.4f (±%.0fm)".format(lat, lng, accuracyMeters)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    val state = addr.adminArea
                    return if (locality != null && state != null && !locality.equals(state, ignoreCase = true)) {
                        "$locality, $state, India"
                    } else if (locality != null) {
                        "$locality, India"
                    } else if (state != null) {
                        "$state, India"
                    } else {
                        "GPS Location: %.4f, %.4f (±%.0fm)".format(lat, lng, accuracyMeters)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("LOCATION_HELPER", "Reverse geocoding unavailable: ${e.localizedMessage}")
        }

        return "GPS Location: %.4f, %.4f (±%.0fm)".format(lat, lng, accuracyMeters)
    }
}
