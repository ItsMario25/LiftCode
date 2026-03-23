// data/model/ErrorCode.kt
package com.example.panicelevators.data.model

enum class ErrorSeverity {
    BLOQUEO,    // Crítica - requiere acción inmediata
    ADVERTENCIA, // Alta - requiere atención
    INFO        // Media - informativo
}

enum class ErrorCategory {
    SEGURIDAD,
    ELECTRICO,
    MECANICO,
    TEMPERATURA,
    MANTENIMIENTO,
    ELECTRONICO,
    CONFIGURACION,
    CONTACTOS_SEGURIDAD,
    CONTACTOR
}

data class ErrorCodeUi(
    val code: String,
    val title: String,
    val description: String,
    val severity: ErrorSeverity,
    val category: ErrorCategory,
    val causes: List<String>,
    val actions: List<String>,
    val brand: String,
    val equipment: String
)