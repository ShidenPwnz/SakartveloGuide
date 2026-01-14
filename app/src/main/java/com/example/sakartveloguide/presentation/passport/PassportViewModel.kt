package com.example.sakartveloguide.presentation.passport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.entity.PassportEntity
import com.example.sakartveloguide.domain.repository.PassportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PassportViewModel @Inject constructor(
    private val repository: PassportRepository
) : ViewModel() {
    val stamps: StateFlow<List<PassportEntity>> = repository.getAllStamps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
