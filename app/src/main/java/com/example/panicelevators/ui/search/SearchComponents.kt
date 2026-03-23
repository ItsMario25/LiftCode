// ui/search/SearchComponents.kt
package com.example.panicelevators.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panicelevators.data.model.ErrorCategory
import com.example.panicelevators.data.model.ErrorCodeUi
import com.example.panicelevators.data.model.ErrorSeverity

/**
 * Campo de búsqueda
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Buscar por código, título o descripción...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

/**
 * Botones de filtro por categoría
 */
@Composable
fun CategoryFilterChips(
    selectedCategory: ErrorCategory?,
    onCategorySelected: (ErrorCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        ErrorCategory.SEGURIDAD to "Seguridad",
        ErrorCategory.ELECTRICO to "Eléctrico",
        ErrorCategory.MECANICO to "Mecánico",
        ErrorCategory.CONTACTOR to "Control"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (category, displayName) ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = {
                    if (selectedCategory == category) {
                        onCategorySelected(null)
                    } else {
                        onCategorySelected(category)
                    }
                },
                label = { Text(displayName) }
            )
        }
    }
}

/**
 * Switch para mostrar solo bloqueos
 */
@Composable
fun OnlyBlockingSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Mostrar solo bloqueos",
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Tarjeta de resultado de búsqueda
 * ✅ Usa exactamente el mismo patrón que RecentErrorCard:
 *    - Card con containerColor según severidad
 *    - Textos sin color explícito — Card propaga contentColor automáticamente
 *    - SuggestionChip con labelColor = onPrimary igual que HomeComponents
 */
@Composable
fun SearchResultCard(
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
            Column(modifier = Modifier.weight(1f)) {
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

            // ✅ SuggestionChip con labelColor explícito — igual que RecentErrorCard
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
 * Estado vacío para resultados de búsqueda
 */
@Composable
fun EmptySearchResults(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "No se encontraron resultados",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Indicador de carga
 */
@Composable
fun SearchLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Contenedor de resultados
 */
@Composable
fun SearchResultsList(
    results: List<ErrorCodeUi>,
    isLoading: Boolean,
    onErrorClick: (ErrorCodeUi) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> SearchLoadingIndicator(modifier = modifier)
        results.isEmpty() -> EmptySearchResults(modifier = modifier)
        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { error ->
                    SearchResultCard(
                        error = error,
                        onClick = { onErrorClick(error) }
                    )
                }
            }
        }
    }
}