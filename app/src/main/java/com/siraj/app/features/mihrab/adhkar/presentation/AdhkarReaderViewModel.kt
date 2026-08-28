package com.siraj.app.features.mihrab.adhkar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.adhkar.AdhkarRepositoryImpl
import com.siraj.app.domain.models.adhkar.AdhkarSettings
import com.siraj.app.domain.models.adhkar.DhikrItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DhikrState(
    val item: DhikrItem,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false
)

class AdhkarReaderViewModel : ViewModel() {
    private val repository = AdhkarRepositoryImpl()

    private val _adhkarStates = MutableStateFlow<Resource<List<DhikrState>>>(Resource.Loading)
    val adhkarStates = _adhkarStates.asStateFlow()

    private val _settings = MutableStateFlow(AdhkarSettings())
    val settings = _settings.asStateFlow()

    private var currentCategoryId: String = ""

    init {
        viewModelScope.launch {
            repository.getSettings().collect {
                _settings.value = it
            }
        }
    }

    fun loadAdhkar(categoryId: String) {
        currentCategoryId = categoryId
        viewModelScope.launch {
            _adhkarStates.value = Resource.Loading
            when (val result = repository.getAdhkarByCategory(categoryId)) {
                is Resource.Success -> {
                    val states = result.data.map { DhikrState(item = it) }
                    _adhkarStates.value = Resource.Success(states)
                }
                is Resource.Error -> {
                    _adhkarStates.value = Resource.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun incrementCount(dhikrId: String) {
        val currentStates = _adhkarStates.value
        if (currentStates is Resource.Success) {
            val updatedList = currentStates.data.map { state ->
                if (state.item.id == dhikrId && !state.isCompleted) {
                    val newCount = state.currentCount + 1
                    val isCompleted = newCount >= state.item.requiredCount
                    state.copy(currentCount = newCount, isCompleted = isCompleted)
                } else {
                    state
                }
            }
            _adhkarStates.value = Resource.Success(updatedList)
        }
    }

    fun resetProgress() {
        val currentStates = _adhkarStates.value
        if (currentStates is Resource.Success) {
            val updatedList = currentStates.data.map { state ->
                state.copy(currentCount = 0, isCompleted = false)
            }
            _adhkarStates.value = Resource.Success(updatedList)
        }
    }

    fun toggleQuietMode() {
        viewModelScope.launch {
            repository.updateSettings(_settings.value.copy(quietMode = !_settings.value.quietMode))
        }
    }
}
