package com.siraj.app.features.mihrab.adhkar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.adhkar.AdhkarRepositoryImpl
import com.siraj.app.domain.models.adhkar.DhikrCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdhkarCategoriesViewModel : ViewModel() {
    private val repository = AdhkarRepositoryImpl()

    private val _categories = MutableStateFlow<Resource<List<DhikrCategory>>>(Resource.Loading)
    val categories = _categories.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categories.value = Resource.Loading
            _categories.value = repository.getCategories()
        }
    }
}
