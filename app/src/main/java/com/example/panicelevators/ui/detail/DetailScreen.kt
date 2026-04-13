// ui/detail/DetailScreen.kt
package com.example.panicelevators.ui.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.panicelevators.PanicElevatorsApplication
import com.example.panicelevators.data.model.ErrorMock
import com.example.panicelevators.data.model.ErrorSeverity
import com.example.panicelevators.data.repository.FavoritesRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    errorCode: String,
    navController: NavController,
    favoritesRepository: FavoritesRepository = (navController.context.applicationContext as PanicElevatorsApplication).favoritesRepository
) {
    val context = LocalContext.current

    val error = remember(errorCode) {
        try { ErrorMock.getErrorByCode(errorCode) } catch (e: Exception) { null }
    }

    // ✅ categoryName precalculado con remember
    val categoryName = remember(error?.category) {
        error?.category?.name
            ?.replace("_", " ")
            ?.lowercase()
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            ?: ""
    }

    var isFavorite by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(errorCode) {
        isFavorite = favoritesRepository.isFavorite(errorCode)
    }

    val severityContainerColor = when (error?.severity) {
        ErrorSeverity.BLOQUEO -> MaterialTheme.colorScheme.errorContainer
        ErrorSeverity.ADVERTENCIA -> MaterialTheme.colorScheme.tertiaryContainer
        ErrorSeverity.INFO -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    // ✅ Función que construye el texto y lanza el Sharesheet de Android
    fun shareError() {
        if (error == null) return

        val severityText = when (error.severity) {
            ErrorSeverity.BLOQUEO -> "🔴 BLOQUEO"
            ErrorSeverity.ADVERTENCIA -> "🟡 ADVERTENCIA"
            ErrorSeverity.INFO -> "🔵 INFORMATIVO"
        }

        val causesText = error.causes
            .mapIndexed { i, cause -> "  ${i + 1}. $cause" }
            .joinToString("\n")

        val actionsText = error.actions
            .mapIndexed { i, action -> "  ${i + 1}. $action" }
            .joinToString("\n")

        val shareText = """
🛗 LiftCode — Código de Error

Código: ${error.code}
Equipo: ${error.brand} — ${error.equipment}
Título: ${error.title}
Severidad: $severityText
Categoría: $categoryName

📋 Descripción:
${error.description}

⚠️ Posibles causas:
$causesText

✅ Acciones recomendadas:
$actionsText

Compartido desde LiftCode
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Compartir código ${error.code}"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = error?.code ?: "Detalle",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // ✅ Botón compartir — abre Android Sharesheet
                    IconButton(onClick = { shareError() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Compartir",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Botón favorito
                    IconButton(onClick = {
                        scope.launch {
                            isFavorite = favoritesRepository.toggleFavorite(errorCode)
                        }
                    }) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = severityContainerColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (error == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = "Código no encontrado",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "El código '$errorCode' no existe en la base de datos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Volver")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── Hero del código ───────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(severityContainerColor)
                        .padding(horizontal = 20.dp)
                        .padding(top = 8.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = error.code,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (error.severity) {
                                ErrorSeverity.BLOQUEO -> MaterialTheme.colorScheme.onErrorContainer
                                ErrorSeverity.ADVERTENCIA -> MaterialTheme.colorScheme.onTertiaryContainer
                                ErrorSeverity.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                        SeverityBadge(severity = error.severity)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = error.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // ── Contenido de tarjetas ─────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EquipmentInfoCard(
                        brand = error.brand,
                        equipment = error.equipment,
                        category = categoryName
                    )
                    DescriptionCard(description = error.description)
                    CausesCard(causes = error.causes)
                    ActionsCard(actions = error.actions)
                    SecurityNote(severity = error.severity)
                }
            }
        }
    }
}

// ─── SeverityBadge ────────────────────────────────────────────────────────────

@Composable
fun SeverityBadge(severity: ErrorSeverity) {
    val containerColor = when (severity) {
        ErrorSeverity.BLOQUEO -> MaterialTheme.colorScheme.error
        ErrorSeverity.ADVERTENCIA -> MaterialTheme.colorScheme.tertiary
        ErrorSeverity.INFO -> MaterialTheme.colorScheme.secondary
    }
    val labelColor = when (severity) {
        ErrorSeverity.BLOQUEO -> MaterialTheme.colorScheme.onError
        ErrorSeverity.ADVERTENCIA -> MaterialTheme.colorScheme.onTertiary
        ErrorSeverity.INFO -> MaterialTheme.colorScheme.onSecondary
    }
    val text = when (severity) {
        ErrorSeverity.BLOQUEO -> "BLOQUEO"
        ErrorSeverity.ADVERTENCIA -> "ADVERTENCIA"
        ErrorSeverity.INFO -> "INFORMATIVO"
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = labelColor
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = labelColor
        )
    )
}

// ─── EquipmentInfoCard ────────────────────────────────────────────────────────

@Composable
fun EquipmentInfoCard(brand: String, equipment: String, category: String) {
    DetailCard(
        icon = Icons.Default.Info,
        title = "Información del equipo",
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(label = "Marca", value = brand)
        InfoRow(label = "Equipo", value = equipment)
        InfoRow(label = "Categoría", value = category)
    }
}

// ─── DescriptionCard ──────────────────────────────────────────────────────────

@Composable
fun DescriptionCard(description: String) {
    DetailCard(
        icon = Icons.Default.Info,
        title = "Descripción",
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─── CausesCard ───────────────────────────────────────────────────────────────

@Composable
fun CausesCard(causes: List<String>) {
    if (causes.isEmpty()) return
    DetailCard(
        icon = Icons.Default.Warning,
        title = "Posibles causas",
        iconTint = MaterialTheme.colorScheme.error,
        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(4.dp))
        causes.forEach { cause ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.error)
                )
                Text(
                    text = cause,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ─── ActionsCard ──────────────────────────────────────────────────────────────

@Composable
fun ActionsCard(actions: List<String>) {
    if (actions.isEmpty()) return
    DetailCard(
        icon = Icons.Default.CheckCircle,
        title = "Acciones recomendadas",
        iconTint = MaterialTheme.colorScheme.primary,
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(4.dp))
        actions.forEachIndexed { index, action ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = action,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─── SecurityNote ─────────────────────────────────────────────────────────────

@Composable
fun SecurityNote(severity: ErrorSeverity) {
    data class SeverityStyle(
        val icon: ImageVector,
        val title: String,
        val message: String,
        val color: Color
    )

    val style = when (severity) {
        ErrorSeverity.BLOQUEO -> SeverityStyle(
            icon = Icons.Default.Warning,
            title = "¡ATENCIÓN!",
            message = "Este error requiere atención inmediata. No opere el equipo hasta resolver la causa.",
            color = MaterialTheme.colorScheme.error
        )
        ErrorSeverity.ADVERTENCIA -> SeverityStyle(
            icon = Icons.Default.Warning,
            title = "Precaución",
            message = "Se recomienda revisar el equipo a la brevedad para evitar fallos mayores.",
            color = MaterialTheme.colorScheme.tertiary
        )
        ErrorSeverity.INFO -> SeverityStyle(
            icon = Icons.Default.Info,
            title = "Información",
            message = "Error informativo. Monitoree el equipo y realice mantenimiento preventivo.",
            color = MaterialTheme.colorScheme.secondary
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = style.color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(style.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    style.icon,
                    contentDescription = null,
                    tint = style.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = style.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = style.color
                )
                Text(
                    text = style.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Helpers reutilizables ────────────────────────────────────────────────────

@Composable
private fun DetailCard(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = iconTint
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}