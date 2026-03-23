// ui/home/HomeViewModelFactory.kt
package com.example.panicelevators.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.panicelevators.data.repository.RecentRepository

class HomeViewModelFactory(
    private val recentRepository: RecentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(recentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}