package com.example.byhm.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.byhm.data.model.Resource
import com.example.byhm.data.model.ResourceCategory
import com.example.byhm.data.repository.ResourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ResourceViewModel(
    private val repository: ResourceRepository = ResourceRepository()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ResourceCategory?>(null)
    val selectedCategory: StateFlow<ResourceCategory?> = _selectedCategory.asStateFlow()

    private val _filteredResources = MutableStateFlow<List<Resource>>(emptyList())
    val filteredResources: StateFlow<List<Resource>> = _filteredResources.asStateFlow()

    // Default reference location: New Delhi Center
    val userLat = 28.6139
    val userLng = 77.2090

    init {
        updateFilter()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilter()
    }

    fun setCategory(category: ResourceCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
        updateFilter()
    }

    fun getResourceById(id: String): Resource? {
        return repository.getResourceById(id)
    }

    fun getDistanceKm(resLat: Double, resLng: Double): Double {
        return repository.calculateDistanceKm(userLat, userLng, resLat, resLng)
    }

    private fun updateFilter() {
        _filteredResources.value = repository.filterResources(_searchQuery.value, _selectedCategory.value)
    }
}
