// PanicElevatorsApplication.kt
package com.example.panicelevators

import android.app.Application
import com.example.panicelevators.data.datastore.PreferencesManager
import com.example.panicelevators.data.repository.FavoritesRepository
import com.example.panicelevators.data.repository.RecentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PanicElevatorsApplication : Application() {

    lateinit var favoritesRepository: FavoritesRepository
        private set

    lateinit var recentRepository: RecentRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Un solo PreferencesManager compartido entre ambos repositorios
        val preferencesManager = PreferencesManager(this)
        favoritesRepository = FavoritesRepository(preferencesManager)
        recentRepository = RecentRepository(preferencesManager)

        CoroutineScope(Dispatchers.IO).launch {
            com.example.panicelevators.data.model.ErrorMock.loadErrorsFromAssets(this@PanicElevatorsApplication)
                .onSuccess { errors ->
                    android.util.Log.d("ErrorLift", "Cargados ${errors.size} códigos de error")
                }
                .onFailure { error ->
                    android.util.Log.e("ErrorLift", "Error al cargar códigos: ${error.message}")
                }
        }
    }
}