package com.tecsup.metrolimago.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
// --- NUEVOS IMPORTS ---
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.SwapVert
// --- FIN NUEVOS IMPORTS ---
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.viewmodel.MainViewModel

// Coordenadas de Lima, para centrar el mapa
private val limaCenter = LatLng(-12.046374, -77.042793)

/**
 * Pantalla de Planificación de Ruta.
 * REDISEÑADA para coincidir con la nueva UI.
 */
@Composable
fun RoutePlannerScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navega al resultado cuando la ruta esté calculada
    LaunchedEffect(key1 = uiState.calculatedRoute) {
        if (uiState.calculatedRoute != null) {
            onNavigateToResult()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(limaCenter, 11f)
    }

    // Actualiza la cámara cuando se seleccionan estaciones
    LaunchedEffect(uiState.selectedOrigin, uiState.selectedDestination) {
        val origin = uiState.selectedOrigin
        val destination = uiState.selectedDestination

        if (origin != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(origin.latitude, origin.longitude), 14f
            )
        }
        // (Aquí podríamos agregar lógica para hacer zoom y que entren ambas)
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // --- 1. EL MAPA (Ocupa la mitad superior) ---
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f), // Ocupa el 50% superior
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            // Marcador para el Origen
            uiState.selectedOrigin?.let {
                Marker(
                    state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                    title = "Origen: ${it.name}"
                )
            }
            // Marcador para el Destino
            uiState.selectedDestination?.let {
                Marker(
                    state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                    title = "Destino: ${it.name}"
                )
            }
        }

        // --- Botón de Regresar (Flotante) ---
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Regresar"
            )
        }

        // --- 2. EL PANEL INFERIOR (Ocupa la mitad inferior) ---
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.55f), // 55% para que se solape un poco
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Selecciona tu viaje",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // --- Selectores de Estación ---
                Box {
                    Column {
                        // Selector de Origen
                        StationSelector(
                            label = "Origen",
                            icon = Icons.Outlined.MyLocation,
                            allStations = uiState.allStations,
                            selectedStation = uiState.selectedOrigin,
                            onStationSelected = { station ->
                                viewModel.onOriginStationSelected(station)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selector de Destino
                        StationSelector(
                            label = "Destino",
                            icon = Icons.Outlined.LocationOn,
                            allStations = uiState.allStations,
                            selectedStation = uiState.selectedDestination,
                            onStationSelected = { station ->
                                viewModel.onDestinationStationSelected(station)
                            }
                        )
                    }

                    // --- Botón de Intercambiar (Swap) ---
                    // (Añadido basado en el diseño)
                    IconButton(
                        onClick = {
                            val origin = uiState.selectedOrigin
                            val dest = uiState.selectedDestination
                            if (origin != null) viewModel.onDestinationStationSelected(origin)
                            if (dest != null) viewModel.onOriginStationSelected(dest)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapVert,
                            contentDescription = "Intercambiar"
                        )
                    }
                }


                Spacer(modifier = Modifier.height(24.dp))

                // --- Mensajes de Error ---
                uiState.routeErrorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // --- Botón de Búsqueda ---
                Button(
                    onClick = {
                        viewModel.calculateRoute()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState.selectedOrigin != null && uiState.selectedDestination != null
                ) {
                    Text(text = "Buscar Ruta", fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * NUEVO: Composable reutilizable para un selector de estación.
 * Reemplaza al antiguo StationSelector.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSelector(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    allStations: List<Station>,
    selectedStation: Station?,
    onStationSelected: (Station) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(), // Ancla el menú a este Card
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedStation?.name ?: label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedStation != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selectedStation != null) FontWeight.Medium else FontWeight.Normal
                )
            }
        }

        // Menú desplegable (ocupa toda la pantalla)
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            // Barra de búsqueda (simulada) y botón de cerrar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Selecciona una estación",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = false }) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            // Lista de estaciones
            LazyColumn(modifier = Modifier.fillMaxHeight(0.7f)) {
                items(allStations) { station ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(station.name)
                                Text(
                                    text = "Línea ${station.lineId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onStationSelected(station)
                            expanded = false
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}