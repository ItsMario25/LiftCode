// ui/favorites/FavoritesViewModel.kt
package com.example.panicelevators.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicelevators.data.model.ErrorCodeUi
import com.example.panicelevators.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoritesRepository
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<ErrorCodeUi>>(emptyList())
    val favorites: StateFlow<List<ErrorCodeUi>> = _favorites

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            // El Flow es reactivo: cualquier cambio en el repositorio
            // actualiza _favorites automáticamente. No se necesita
            // llamarlo de nuevo manualmente.
            repository.favoritesFlow.collectLatest { favoritesList ->
                _favorites.value = favoritesList
                _isLoading.value = false
            }
        }
    }

    // ✅ Fix #5: Ya no abre una segunda colección en paralelo.
    //           Solo activa el indicador de carga; loadFavorites()
    //           ya está escuchando el Flow y actualizará la lista solo.
    fun refreshFavorites() {
        _isLoading.value = true
    }
}