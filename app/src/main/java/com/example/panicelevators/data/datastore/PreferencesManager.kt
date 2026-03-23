// data/datastore/PreferencesManager.kt
package com.example.panicelevators.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

class PreferencesManager(private val context: Context) {

    companion object {
        private val FAVORITES_KEY = stringSetPreferencesKey("favorites")
        // Lista ordenada de códigos recientes separados por coma
        private val RECENT_KEY = stringPreferencesKey("recent_errors")
        private const val MAX_RECENT = 8
    }

    // ─── Favoritos ────────────────────────────────────────────────────────────

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data
        .map { it[FAVORITES_KEY] ?: emptySet() }

    suspend fun addFavorite(code: String) {
        context.dataStore.edit { it[FAVORITES_KEY] = (it[FAVORITES_KEY] ?: emptySet()) + code }
    }

    suspend fun removeFavorite(code: String) {
        context.dataStore.edit { it[FAVORITES_KEY] = (it[FAVORITES_KEY] ?: emptySet()) - code }
    }

    suspend fun isFavorite(code: String): Boolean = getFavoritesSync().contains(code)

    suspend fun getFavoritesSync(): Set<String> =
        context.dataStore.data.map { it[FAVORITES_KEY] ?: emptySet() }.first()

    // ─── Historial reciente ───────────────────────────────────────────────────

    // Emite la lista ordenada de códigos recientes (más nuevo primero)
    val recentFlow: Flow<List<String>> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[RECENT_KEY] ?: ""
            if (raw.isBlank()) emptyList()
            else raw.split(",").filter { it.isNotBlank() }
        }

    // Agrega un código al inicio, elimina duplicados y recorta a MAX_RECENT
    suspend fun addRecent(code: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[RECENT_KEY] ?: "")
                .split(",")
                .filter { it.isNotBlank() }
                .toMutableList()

            current.remove(code)      // evita duplicados
            current.add(0, code)      // más nuevo al inicio
            prefs[RECENT_KEY] = current.take(MAX_RECENT).joinToString(",")
        }
    }
}