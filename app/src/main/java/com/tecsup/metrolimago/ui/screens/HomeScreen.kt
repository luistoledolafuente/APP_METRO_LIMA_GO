package com.tecsup.metrolimago.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.tecsup.metrolimago.data.database.TransportLine
import com.tecsup.metrolimago.viewmodel.MainViewModel

// Coordenadas de Lima, para centrar el mapa
private val limaCenter = LatLng(-12.046374, -77.042793)

// --- 1. MEJORA DE UI/UX ---
// Habilitamos el botón de "Mi Ubicación" que provee Google Maps
private val mapUiSettings = MapUiSettings(
    zoomControlsEnabled = false,
    myLocationButtonEnabled = true // <-- ¡AQUÍ ESTÁ EL CAMBIO!
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToPlanner: () -> Unit,
    onLineClicked: (String) -> Unit
) {
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    if (permissionState.allPermissionsGranted) {
        HomeScreenContent(
            viewModel = viewModel,
            onNavigateToPlanner = onNavigateToPlanner,
            onLineClicked = onLineClicked
        )
    } else {
        PermissionDeniedScreen(
            onGrantPermission = { permissionState.launchMultiplePermissionRequest() }
        )
    }
}

/**
 * El contenido real de la pantalla (el mapa)
 */
@Composable
fun HomeScreenContent(
    viewModel: MainViewModel,
    onNavigateToPlanner: () -> Unit,
    onLineClicked: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(limaCenter, 11f)
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- 1. EL MAPA (Al fondo) ---
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = mapUiSettings // <-- Aplicamos la UI con el botón
            ) {
                uiState.allStations.forEach { station ->
                    Marker(
                        state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                        title = station.name,
                        snippet = "Línea: ${station.lineId}"
                    )
                }
            }

            // --- 2. EL BUSCADOR (Arriba) ---
            SearchBarUI(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                onClicked = onNavigateToPlanner
            )

            // --- 3. EL PANEL INFERIOR (Abajo) ---
            BottomPanel(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                lines = uiState.allLines,
                onLineClicked = onLineClicked,
                onNavigateToPlanner = onNavigateToPlanner // Le pasamos el navegador
            )
        }
    }
}

/**
 * Un "falso" buscador que flota arriba del mapa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarUI(
    modifier: Modifier = Modifier,
    onClicked: () -> Unit
) {
    Card(
        onClick = onClicked,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "¿A dónde quieres ir?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * --- 2. MEJORA DE UI/UX ---
 * El panel inferior ahora es una columna que contiene
 * el botón "A Dónde Vas" y la lista de líneas.
 */
@Composable
fun BottomPanel(
    modifier: Modifier = Modifier,
    lines: List<TransportLine>,
    onLineClicked: (String) -> Unit,
    onNavigateToPlanner: () -> Unit // Nueva acción
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 16.dp)
        ) {

            // --- NUEVO BOTÓN "A DÓNDE VAS" ---
            Button(
                onClick = onNavigateToPlanner,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("¿A Dónde Vas?", style = MaterialTheme.typography.bodyLarge)
            }
            // --- FIN DEL NUEVO BOTÓN ---

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Líneas Disponibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // --- Scroll Horizontal de Líneas ---
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lines) { line ->
                    LineChip(
                        line = line,
                        onClicked = { onLineClicked(line.id) }
                    )
                }
            }
        }
    }
}

/**
 * Un "Chip" o tarjeta pequeña que representa una línea.
 * (Sin cambios)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineChip(
    line: TransportLine,
    onClicked: () -> Unit
) {
    Card(
        onClick = onClicked,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(line.color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = line.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Pantalla que se muestra si el usuario denegó el permiso de GPS.
 * (Sin cambios)
 */
@Composable
fun PermissionDeniedScreen(
    onGrantPermission: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Permiso de Ubicación Requerido",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        // ... (resto del contenido sin cambios)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Para mostrar el mapa y tu ubicación, necesitamos que nos des permiso.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onGrantPermission) {
            Text("Conceder Permiso")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(this)
            }
        }) {
            Text("Abrir Configuración")
        }
    }
}

