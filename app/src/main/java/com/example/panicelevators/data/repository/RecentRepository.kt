// data/repository/RecentRepository.kt
package com.example.panicelevators.data.repository

import com.example.panicelevators.data.datastore.PreferencesManager
import com.example.panicelevators.data.model.ErrorCodeUi
import com.example.panicelevators.data.model.ErrorMock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecentRepository(
    private val preferencesManager: PreferencesManager
) {

    // Flow de los últimos 8 errores como objetos ErrorCodeUi (más nuevo primero)
    val recentFlow: Flow<List<ErrorCodeUi>> = preferencesManager.recentFlow
        .map { codes ->
            codes.mapNotNull { code ->
                try {
                    ErrorMock.getErrorByCode(code)
                } catch (e: Exception) {
                    null
                }
            }
        }

    // Registrar un error como consultado
    suspend fun addRecent(code: String) {
        preferencesManager.addRecent(code)
    }
}