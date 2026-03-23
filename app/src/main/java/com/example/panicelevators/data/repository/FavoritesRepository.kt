// data/repository/FavoritesRepository.kt
package com.example.panicelevators.data.repository

import com.example.panicelevators.data.datastore.PreferencesManager
import com.example.panicelevators.data.model.ErrorCodeUi
import com.example.panicelevators.data.model.ErrorMock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepository(
    private val preferencesManager: PreferencesManager
) {

    // Flow de códigos favoritos (Set<String>)
    val favoritesCodesFlow: Flow<Set<String>> = preferencesManager.favoritesFlow

    // Flow de objetos ErrorCodeUi favoritos
    val favoritesFlow: Flow<List<ErrorCodeUi>> = favoritesCodesFlow.map { codes ->
        codes.mapNotNull { code ->
            try {
                ErrorMock.getErrorByCode(code)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Agregar a favoritos
    suspend fun addFavorite(code: String) {
        preferencesManager.addFavorite(code)
    }

    // Quitar de favoritos
    suspend fun removeFavorite(code: String) {
        preferencesManager.removeFavorite(code)
    }

    // Alternar favorito
    suspend fun toggleFavorite(code: String): Boolean {
        val isFav = preferencesManager.isFavorite(code)
        if (isFav) {
            removeFavorite(code)
            return false
        } else {
            addFavorite(code)
            return true
        }
    }

    // Verificar si es favorito
    suspend fun isFavorite(code: String): Boolean {
        return preferencesManager.isFavorite(code)
    }
}