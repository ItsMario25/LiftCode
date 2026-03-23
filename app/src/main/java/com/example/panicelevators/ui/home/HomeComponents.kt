// ui/home/HomeComponents.kt - Eliminar BrandFilterChip y EquipmentFilterChip
// Mantener solo los necesarios:

package com.example.panicelevators.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panicelevators.data.model.ErrorCodeUi
import com.example.panicelevators.data.model.ErrorSeverity

/**
 * Tarjeta de error reciente
 */
@Composable
fun RecentErrorCard(
    error: ErrorCodeUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when (error.severity) {
                ErrorSeverity.BLOQUEO -> MaterialTheme.colorScheme.errorContainer
                ErrorSeverity.ADVERTENCIA -> MaterialTheme.colorScheme.tertiaryContainer
                ErrorSeverity.INFO -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = error.code,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = error.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${error.brand} - ${error.equipment}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            SuggestionChip(
                onClick = { },
                label = {
                    Text(
                        when (error.severity) {
                            ErrorSeverity.BLOQUEO -> "Bloqueo"
                            ErrorSeverity.ADVERTENCIA -> "Advertencia"
                            ErrorSeverity.INFO -> "Info"
                        }
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = when (error.severity) {
                        ErrorSeverity.BLOQUEO -> MaterialTheme.colorScheme.error
                        ErrorSeverity.ADVERTENCIA -> MaterialTheme.colorScheme.tertiary
                        ErrorSeverity.INFO -> MaterialTheme.colorScheme.secondary
                    },
                    labelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * Estado vacío para la lista de recientes
 */
@Composable
fun EmptyRecentErrors(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "No hay códigos consultados recientemente",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Indicador de carga
 */
@Composable
fun RecentErrorsLoading(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cargando...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}