// ui/search/SearchScreen.kt
package com.example.panicelevators.ui.search

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.panicelevators.data.model.ErrorCategory
import com.example.panicelevators.data.model.ErrorCodeUi
import com.example.panicelevators.data.model.ErrorMock
import com.example.panicelevators.data.model.ErrorSeverity
import com.example.panicelevators.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    onErrorClick: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ErrorCategory?>(null) }
    var onlyBlocking by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<ErrorCodeUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // ── Voz ──────────────────────────────────────────────────────────────────

    var voiceState by remember { mutableStateOf<VoiceState>(VoiceState.Idle) }

    // Manager del SpeechRecognizer — se destruye cuando el Composable sale
    val voiceManager = remember {
        VoiceRecognitionManager(context).apply {
            setOnStateChange { state ->
                voiceState = state
                // Cuando llega un resultado lo insertamos en el campo de búsqueda
                if (state is VoiceState.Result) {
                    searchText = state.text
                    voiceState = VoiceState.Idle
                }
            }
        }
    }

    // Destruir el manager cuando el Composable sale del árbol
    DisposableEffect(Unit) {
        onDispose { voiceManager.destroy() }
    }

    // Mostrar error de voz en Snackbar
    LaunchedEffect(voiceState) {
        if (voiceState is VoiceState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = (voiceState as VoiceState.Error).message
                )
                voiceState = VoiceState.Idle
            }
        }
    }

    // Launcher para solicitar permiso de micrófono
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceManager.startListening()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Se necesita permiso de micrófono para la búsqueda por voz"
                )
            }
        }
    }

    // Función que verifica permiso y arranca/detiene el reconocimiento
    fun handleMicClick() {
        when (voiceState) {
            is VoiceState.Listening -> voiceManager.stopListening()
            is VoiceState.Processing -> { /* esperar */ }
            else -> {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    voiceManager.startListening()
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    // ── Búsqueda ──────────────────────────────────────────────────────────────

    LaunchedEffect(searchText, selectedCategory, onlyBlocking) {
        isLoading = true
        try {
            val allErrors = ErrorMock.getAllErrors()
            searchResults = allErrors.filter { error ->
                val matchesSearch = searchText.isEmpty() ||
                        error.code.contains(searchText, ignoreCase = true) ||
                        error.title.contains(searchText, ignoreCase = true) ||
                        error.description.contains(searchText, ignoreCase = true)

                val matchesCategory = selectedCategory == null || error.category == selectedCategory
                val matchesBlocking = !onlyBlocking || error.severity == ErrorSeverity.BLOQUEO

                matchesSearch && matchesCategory && matchesBlocking
            }
        } catch (e: IllegalStateException) {
            searchResults = emptyList()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Buscar código",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Favorites.route) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.About.route) },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Acerca De") },
                    label = { Text("Acerca De") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ✅ SearchTextField ahora recibe voiceState y onMicClick
                    SearchTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        voiceState = voiceState,
                        onMicClick = { handleMicClick() }
                    )

                    // Indicador de estado de voz debajo del campo
                    AnimatedVisibility(visible = voiceState is VoiceState.Listening) {
                        Text(
                            text = "🎤 Escuchando... habla el código de error",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CategoryFilterChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )

                    OnlyBlockingSwitch(
                        checked = onlyBlocking,
                        onCheckedChange = { onlyBlocking = it }
                    )
                }
            }

            HorizontalDivider()

            AnimatedVisibility(
                visible = !isLoading && searchResults.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resultados",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${searchResults.size}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                SearchResultsList(
                    results = searchResults,
                    isLoading = isLoading,
                    onErrorClick = { error -> onErrorClick(error.code) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}