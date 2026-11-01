package com.example.byhm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.byhm.data.model.EmergencyGuide
import com.example.byhm.data.repository.EmergencyGuideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmergencyGuideViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EmergencyGuideRepository(application)

    private val _allGuides = MutableStateFlow<List<EmergencyGuide>>(emptyList())
    
    private val _filteredGuides = MutableStateFlow<List<EmergencyGuide>>(emptyList())
    val filteredGuides: StateFlow<List<EmergencyGuide>> = _filteredGuides.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?> (null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        loadGuides()
    }

    fun loadGuides() {
        val guides = repository.loadEmergencyGuides()
        _allGuides.value = guides
        filterGuides()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterGuides()
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
        filterGuides()
    }

    private fun filterGuides() {
        val query = _searchQuery.value.trim().lowercase()
        val category = _selectedCategory.value

        _filteredGuides.value = _allGuides.value.filter { guide ->
            val matchesCategory = category == null || guide.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isEmpty() ||
                    guide.title.lowercase().contains(query) ||
                    guide.category.lowercase().contains(query) ||
                    guide.immediateActions.any { it.lowercase().contains(query) }

            matchesCategory && matchesQuery
        }
    }
}
