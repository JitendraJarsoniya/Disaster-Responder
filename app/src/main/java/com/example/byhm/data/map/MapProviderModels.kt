package com.example.byhm.data.map

import com.example.byhm.BuildConfig

enum class MapLayerType(
    val displayName: String,
    val isAvailable: Boolean,
    val note: String? = null
) {
    STANDARD("Standard (OSM HOT)", true, "Policy-compliant Humanitarian OpenStreetMap"),
    TOPOGRAPHIC("Terrain (OpenTopoMap)", true, "Topographic contour & elevation raster tiles"),
    SATELLITE("Satellite Imagery", BuildConfig.MAPTILER_API_KEY.isNotBlank(), if (BuildConfig.MAPTILER_API_KEY.isNotBlank()) "MapTiler Aerial Satellite v2" else "Requires MAPTILER_API_KEY in local.properties"),
    HYBRID("Hybrid Satellite", BuildConfig.MAPTILER_API_KEY.isNotBlank(), if (BuildConfig.MAPTILER_API_KEY.isNotBlank()) "MapTiler Satellite + Labels/Roads Overlay" else "Requires MAPTILER_API_KEY in local.properties")
}

data class MapMarkerData(
    val id: String,
    val title: String,
    val snippet: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val isAlert: Boolean,
    val statusOrSeverity: String,
    val addressOrArea: String,
    val phone: String? = null
)

interface MapProviderController {
    fun setCenter(latitude: Double, longitude: Double, zoomLevel: Double)
    fun setMarkers(markers: List<MapMarkerData>)
    fun setTileLayer(layerType: MapLayerType)
    fun centerOnLocation(latitude: Double, longitude: Double)
    fun autoFitMarkers(markers: List<MapMarkerData>)
}
