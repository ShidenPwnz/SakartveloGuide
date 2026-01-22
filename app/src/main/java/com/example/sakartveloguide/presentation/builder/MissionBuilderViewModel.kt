package com.example.sakartveloguide.presentation.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.data.local.dao.LocationDao
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.domain.model.UserSession
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
    val isLoading: Boolean = true,
    val currentLanguage: String = "en"
)

@HiltViewModel
class MissionBuilderViewModel @Inject constructor(
    private val locationDao: LocationDao,
    private val preferenceManager: PreferenceManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _activeCategory = MutableStateFlow<String?>(null)
    private val _activeRegion = MutableStateFlow<String?>(null)
    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())

    // Group 1: Database Intel (Flow<Triple>)
    private val _inventoryData = combine(
        locationDao.getAllLocations(),
        locationDao.getAllCategories(),
        locationDao.getAllRegions()
    ) { all, cats, regs -> Triple(all, cats, regs) }

    // Group 2: User Filters (Flow<Pair>)
    private val _filterCriteria = combine(_activeCategory, _activeRegion) { cat, reg ->
        Pair(cat, reg)
    }

    // ARCHITECT'S FIX: Combines 4 Typed Flows
    val uiState: StateFlow<BuilderUiState> = combine(
        _inventoryData,
        _selectedIds,
        _filterCriteria,
        preferenceManager.userSession
    ) { inventory: Triple<List<LocationEntity>, List<String>, List<String>>,
        selected: Set<Int>,
        filters: Pair<String?, String?>,
        session: UserSession ->

        val (allLocs, cats, regs) = inventory
        val (activeCat, activeReg) = filters

        var filteredList = allLocs
        if (!activeCat.isNullOrEmpty()) {
            filteredList = filteredList.filter { it.type == activeCat }
        }
        if (!activeReg.isNullOrEmpty()) {
            filteredList = filteredList.filter { it.region == activeReg }
        }

        BuilderUiState(
            inventory = filteredList,
            categories = cats,
            regions = regs,
            selectedIds = selected,
            activeCategory = activeCat,
            activeRegion = activeReg,
            isLoading = false,
            currentLanguage = session.language.ifEmpty { "en" }
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