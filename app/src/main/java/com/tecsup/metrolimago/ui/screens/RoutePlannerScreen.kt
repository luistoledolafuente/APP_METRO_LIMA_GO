package com.tecsup.metrolimago.ui.screens

// --- IMPORTS ---
// (Añadí los imports que faltaban para el nuevo diseño)
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.viewmodel.MainViewModel

/**
 * Pantalla para planificar una ruta.
 * ¡CORREGIDA para usar los nombres correctos del MainViewModel!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val allStations = uiState.allStations // Todas las estaciones disponibles

    // --- ARREGLO 1: Usar los nombres correctos del ViewModel ---
    val selectedOrigin = uiState.selectedOrigin
    // --- ARREGLO 2: Usar los nombres correctos del ViewModel ---
    val selectedDestination = uiState.selectedDestination

    // Lógica original para navegar automáticamente
    LaunchedEffect(key1 = uiState.calculatedRoute) {
        if (uiState.calculatedRoute != null && uiState.calculatedRoute!!.segments.isNotEmpty()) {
            onNavigateToResult()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Planificar Ruta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Color de la AppBar
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Selecciona tu viaje",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Start
            )

            // Tarjeta de Origen
            StationSelectorCard(
                label = "Estación de Origen",
                selectedStation = selectedOrigin,
                // --- ARREGLO 3: Usar el nombre correcto de la función ---
                onStationSelected = { station -> viewModel.onOriginStationSelected(station) },
                allStations = allStations,
                isOrigin = true // Indica que es el selector de origen
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de Intercambio (Swap)
            IconButton(
                // --- ARREGLO 4: Implementar la lógica de swap manualmente ---
                onClick = {
                    val origin = uiState.selectedOrigin
                    val dest = uiState.selectedDestination
                    if (origin != null) viewModel.onDestinationStationSelected(origin)
                    if (dest != null) viewModel.onOriginStationSelected(dest)
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.SwapVert, "Intercambiar Origen/Destino", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tarjeta de Destino
            StationSelectorCard(
                label = "Estación de Destino",
                selectedStation = selectedDestination,
                // --- ARREGLO 5: Usar el nombre correcto de la función ---
                onStationSelected = { station -> viewModel.onDestinationStationSelected(station) },
                allStations = allStations,
                isOrigin = false // Indica que es el selector de destino
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mensaje de error (si existe)
            uiState.routeErrorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón "Calcular Ruta"
            Button(
                onClick = {
                    if (selectedOrigin != null && selectedDestination != null) {
                        // --- ARREGLO 6: Usar el nombre correcto de la función ---
                        viewModel.calculateRoute()
                        // (La navegación se dispara sola gracias al LaunchedEffect)
                    }
                },
                enabled = selectedOrigin != null && selectedDestination != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = "Calcular",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Calcular Ruta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}


// --- COMPOSABLES DE LA NUEVA UI ---
// (Estos composables están correctos, no necesitan cambios)

/**
 * Tarjeta de selección de estación (Origen o Destino)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSelectorCard(
    label: String,
    selectedStation: Station?,
    onStationSelected: (Station) -> Unit,
    allStations: List<Station>,
    isOrigin: Boolean
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Card(
        onClick = { showBottomSheet = true },
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isOrigin) Color(0xFF008D41) else MaterialTheme.colorScheme.error) // Verde para origen, Rojo para destino
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedStation?.name ?: "Seleccionar estación",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Icono de flecha
                    contentDescription = "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(270f) // Gira la flecha para que apunte hacia abajo
                )
            }
        }
    }

    if (showBottomSheet) {
        StationSelectionBottomSheet(
            allStations = allStations,
            onStationSelected = { station ->
                onStationSelected(station)
                showBottomSheet = false
            },
            onDismiss = { showBottomSheet = false }
        )
    }
}

/**
 * BottomSheet para la selección de estaciones.
 */
@Composable
fun StationSelectionBottomSheet(
    allStations: List<Station>,
    onStationSelected: (Station) -> Unit,
    onDismiss: () -> Unit
) {
    // Simulación de un Bottom Sheet como un Diálogo Flotante
    // (En una app real, esto usaría ModalBottomSheet de Material3)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // Fondo oscuro
            .clickable { onDismiss() }, // Permite cerrar al tocar fuera
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f) // Ocupa el 70% de la altura
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .clickable(enabled = false) { /* Evita que el clic se propague al fondo */ },
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Handle del BottomSheet
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        .align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "Seleccionar Estación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(allStations) { station ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStationSelected(station) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Estación",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}