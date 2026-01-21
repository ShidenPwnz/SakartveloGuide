package com.example.sakartveloguide.presentation.builder

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.dao.LocationDao
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.ui.manager.HapticManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class BuilderUiState(
    val inventory: List<LocationEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val regions: List<String> = emptyList(),
    val selectedIds: Set<Int> = emptySet(),
    val activeCategory: String? = null,
    val activeRegion: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class MissionBuilderViewModel @Inject constructor(
    private val locationDao: LocationDao,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _activeCategory = MutableStateFlow<String?>(null)
    private val _activeRegion = MutableStateFlow<String?>(null)
    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())

    // ARCHITECT'S FIX: Combine internal filters first to stay under the 5-parameter limit
    private val filterState = combine(_activeCategory, _activeRegion) { cat, reg ->
        Pair(cat, reg)
    }

    val uiState: StateFlow<BuilderUiState> = combine(
        locationDao.getAllLocations(),
        locationDao.getAllCategories(),
        locationDao.getAllRegions(),
        _selectedIds,
        filterState
    ) // ... inside combine block ...
     { inventory, cats, regs, selected, filters ->
        val (activeCat, activeReg) = filters

        Log.d("BUILDER_DEBUG", "Total Inventory: ${inventory.size}")

        var filtered = inventory
        // ARCHITECT'S FIX: Ensure null filters return ALL items
        if (!activeCat.isNullOrEmpty()) filtered = filtered.filter { it.type == activeCat }
        if (!activeReg.isNullOrEmpty()) filtered = filtered.filter { it.region == activeReg }

        Log.d("BUILDER_DEBUG", "Filtered Count: ${filtered.size}")

        BuilderUiState(
            inventory = filtered,
            categories = cats,
            regions = regs,
            selectedIds = selected,
            activeCategory = activeCat,
            activeRegion = activeReg,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BuilderUiState()
    )

    fun toggleLocation(id: Int) {
        val current = _selectedIds.value
        _selectedIds.value = if (current.contains(id)) current - id else current + id
        hapticManager.tick()
    }

    fun setCategory(cat: String?) { _activeCategory.value = if (_activeCategory.value == cat) null else cat }
    fun setRegion(reg: String?) { _activeRegion.value = if (_activeRegion.value == reg) null else reg }
}