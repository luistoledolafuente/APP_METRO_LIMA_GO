package com.tecsup.metrolimago.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.tecsup.metrolimago.viewmodel.MainViewModel

// Centro de Lima (Fallback)
private val limaCenter = LatLng(-12.046374, -77.042793)

/**
 * Pantalla que muestra el detalle de una estación (Req 2).
 * CORREGIDA para animar la cámara a la ubicación correcta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailScreen(
    viewModel: MainViewModel,
    stationId: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val station = uiState.allStations.find { it.id == stationId }
    val line = uiState.allLines.find { it.id == station?.lineId }

    // --- LÓGICA DE MAPA CORREGIDA ---

    // 1. Ubicación por defecto (Lima)
    val stationLocation = station?.let { LatLng(it.latitude, it.longitude) } ?: limaCenter

    // 2. Estado de la cámara, inicializado en Lima
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(limaCenter, 12f)
    }

    // 3. ¡AQUÍ ESTÁ LA CORRECCIÓN DE UI!
    //    Este efecto "escucha" si 'stationLocation' cambia.
    //    Cuando 'station' carga, 'stationLocation' cambia y esto se ejecuta,
    //    animando la cámara a la posición correcta.
    LaunchedEffect(stationLocation) {
        if (stationLocation != limaCenter) { // Solo anima si NO es el fallback
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(stationLocation, 16f), // Zoom más cercano
                durationMs = 1500
            )
        }
    }

    // 4. UI del mapa: "Lite Mode" (no interactivo)
    val mapUiSettings = MapUiSettings(
        zoomControlsEnabled = false,
        zoomGesturesEnabled = false,
        scrollGesturesEnabled = false
    )
    // --- FIN DE LA LÓGICA DE MAPA ---

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(station?.name ?: "Cargando...") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = line?.color ?: MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (uiState.isAppLoading) { // Usamos el loading global
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {

                // --- 1. Tarjeta del Mapa (REAL) ---
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    cameraPositionState = cameraPositionState,
                    uiSettings = mapUiSettings
                ) {
                    // Ponemos un marcador en la estación
                    if (station != null) {
                        Marker(
                            state = MarkerState(position = stationLocation),
                            title = station.name
                        )
                    }
                }

                // --- 2. Tarjeta de Información (Contenido) ---
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Información", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        // Mostramos los datos solo si la estación no es nula
                        if (station != null && line != null) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                InfoRow(icon = Icons.Default.Info, label = "Estado", value = station.status)
                                InfoRow(icon = Icons.Default.Schedule, label = "Horario", value = station.schedule)
                                InfoRow(icon = Icons.Default.Business, label = "Línea", value = line.name)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Un Composable helper para mostrar una fila de información (ícono, label, valor)
 * (Sin cambios)
 */
@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}