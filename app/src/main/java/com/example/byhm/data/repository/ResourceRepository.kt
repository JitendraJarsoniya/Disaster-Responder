package com.example.byhm.data.repository

import com.example.byhm.data.model.Resource
import com.example.byhm.data.model.ResourceCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ResourceRepository {

    // Default: Clean production state starts with empty list until loaded from Firestore / local cache
    private val _resources = MutableStateFlow<List<Resource>>(emptyList())
    val resources: Flow<List<Resource>> = _resources.asStateFlow()

    fun setResources(newList: List<Resource>) {
        _resources.value = newList
    }

    fun loadDemoResourcesForTesting() {
        _resources.value = SampleData.sampleResources
    }

    fun getResourceById(id: String): Resource? {
        return _resources.value.find { it.id == id }
    }

    fun filterResources(query: String, category: ResourceCategory?): List<Resource> {
        return _resources.value.filter { resource ->
            val matchesQuery = query.isBlank() ||
                    resource.name.contains(query, ignoreCase = true) ||
                    resource.address.contains(query, ignoreCase = true)

            val matchesCategory = category == null || resource.category == category

            matchesQuery && matchesCategory
        }
    }

    // Haversine formula to compute distance in km between user and resource
    fun calculateDistanceKm(userLat: Double, userLng: Double, resLat: Double, resLng: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(resLat - userLat)
        val dLng = Math.toRadians(resLng - userLng)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(resLat)) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}
