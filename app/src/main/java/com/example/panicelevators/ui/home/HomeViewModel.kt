// ui/home/HomeViewModel.kt
package com.example.panicelevators.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicelevators.data.model.ErrorCodeUi
import com.example.panicelevators.data.repository.RecentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val recentRepository: RecentRepository
) : ViewModel() {

    private val _recentErrors = MutableStateFlow<List<ErrorCodeUi>>(emptyList())
    val recentErrors: StateFlow<List<ErrorCodeUi>> = _recentErrors

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            _isLoading.value = true
            recentRepository.recentFlow.collectLatest { list ->
                _recentErrors.value = list
                _isLoading.value = false
            }
        }
    }
}