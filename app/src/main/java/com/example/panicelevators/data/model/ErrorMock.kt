// data/model/ErrorMock.kt
package com.example.panicelevators.data.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object ErrorMock {

    private var allErrors: List<ErrorCodeUi> = emptyList()
    private var isLoaded = false

    /**
     * Carga los errores desde el archivo assets/ErrorCodes.json
     */
    suspend fun loadErrorsFromAssets(context: Context): Result<List<ErrorCodeUi>> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("ErrorCodes.json")
                    .bufferedReader()
                    .use { it.readText() }

                allErrors = parseErrorsFromJson(jsonString)
                isLoaded = true
                Result.success(allErrors)
            } catch (e: IOException) {
                Result.failure(Exception("Error al cargar archivo de errores: ${e.message}", e))
            } catch (e: Exception) {
                Result.failure(Exception("Error al parsear JSON: ${e.message}", e))
            }
        }
    }

    /**
     * Obtiene todos los errores (debe estar cargado previamente)
     */
    fun getAllErrors(): List<ErrorCodeUi> {
        if (!isLoaded) {
            throw IllegalStateException("ErrorMock no ha sido inicializado. Llama a loadErrorsFromAssets primero.")
        }
        return allErrors
    }

    /**
     * Obtiene errores por marca
     */
    fun getErrorsByBrand(brand: String): List<ErrorCodeUi> {
        return getAllErrors().filter { it.brand == brand }
    }

    /**
     * Obtiene errores por equipo
     */
    fun getErrorsByEquipment(equipment: String): List<ErrorCodeUi> {
        return getAllErrors().filter { it.equipment == equipment }
    }

    /**
     * Obtiene errores por marca y equipo
     */
    fun getErrorsByBrandAndEquipment(brand: String, equipment: String): List<ErrorCodeUi> {
        return getAllErrors().filter {
            it.brand == brand && it.equipment == equipment
        }
    }

    /**
     * Obtiene un error por su código
     */
    fun getErrorByCode(code: String): ErrorCodeUi? {
        return getAllErrors().find { it.code == code }
    }

    /**
     * Busca errores por texto
     */
    fun searchErrors(query: String): List<ErrorCodeUi> {
        return getAllErrors().filter { error ->
            error.code.contains(query, ignoreCase = true) ||
                    error.title.contains(query, ignoreCase = true) ||
                    error.description.contains(query, ignoreCase = true)
        }
    }

    /**
     * Parsea el JSON a lista de ErrorCodeUi
     */
    private fun parseErrorsFromJson(jsonString: String): List<ErrorCodeUi> {
        val jsonArray = JSONArray(jsonString)
        val errors = mutableListOf<ErrorCodeUi>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            errors.add(parseError(jsonObject))
        }

        return errors
    }

    /**
     * Parsea un objeto JSON a ErrorCodeUi
     */
    private fun parseError(json: JSONObject): ErrorCodeUi {
        return ErrorCodeUi(
            code = json.getString("code"),
            title = json.getString("title"),
            description = json.getString("description"),
            severity = mapSeverity(json.getString("severity")),
            category = mapCategory(json.getString("category")),
            causes = json.getJSONArray("causes").let { array ->
                (0 until array.length()).map { array.getString(it) }
            },
            actions = json.getJSONArray("actions").let { array ->
                (0 until array.length()).map { array.getString(it) }
            },
            brand = json.getString("brand"),
            equipment = json.getString("equipment")
        )
    }

    /**
     * Mapea string de severidad a Enum
     */
    private fun mapSeverity(severity: String): ErrorSeverity {
        return when (severity) {
            "Crítica" -> ErrorSeverity.BLOQUEO
            "Alta" -> ErrorSeverity.ADVERTENCIA
            "Media" -> ErrorSeverity.INFO
            else -> ErrorSeverity.INFO
        }
    }

    /**
     * Mapea string de categoría a Enum
     */
    private fun mapCategory(category: String): ErrorCategory {
        return when (category) {
            "Seguridad" -> ErrorCategory.SEGURIDAD
            "Eléctrico/Motor", "Eléctrico" -> ErrorCategory.ELECTRICO
            "Mecánico" -> ErrorCategory.MECANICO
            "Temperatura" -> ErrorCategory.TEMPERATURA
            "Mantenimiento" -> ErrorCategory.MANTENIMIENTO
            "Electrónico" -> ErrorCategory.ELECTRONICO
            "Configuración" -> ErrorCategory.CONFIGURACION
            "Contactos de seguridad" -> ErrorCategory.CONTACTOS_SEGURIDAD
            "Contactor" -> ErrorCategory.CONTACTOR
            else -> ErrorCategory.ELECTRICO
        }
    }
}