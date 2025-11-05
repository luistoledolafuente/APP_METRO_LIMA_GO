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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.data.database.TransportLine
import com.tecsup.metrolimago.viewmodel.MainViewModel

private val limaCenter = LatLng(-12.046374, -77.042793)

private val mapUiSettings = MapUiSettings(
    zoomControlsEnabled = false,
    myLocationButtonEnabled = true
)

/**
 * --- ¡ARREGLADO! ---
 * Añadimos los 3 nuevos parámetros que vienen desde AppNavigation
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToPlanner: () -> Unit,
    onLineClicked: (String) -> Unit,
    onStationClicked: (String) -> Unit,  // <-- PARÁMETRO AÑADIDO
    onViewAllLines: () -> Unit,         // <-- PARÁMETRO AÑADIDO
    onViewAllStations: () -> Unit      // <-- PARÁMETRO AÑADIDO
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
            onLineClicked = onLineClicked,
            onStationClicked = onStationClicked,    // <-- PASAMOS EL PARÁMETRO
            onViewAllLines = onViewAllLines,       // <-- PASAMOS EL PARÁMETRO
            onViewAllStations = onViewAllStations  // <-- PASAMOS EL PARÁMETRO
        )
    } else {
        PermissionDeniedScreen(
            onGrantPermission = { permissionState.launchMultiplePermissionRequest() }
        )
    }
}

/**
 * --- ¡ARREGLADO! ---
 * Añadimos los parámetros para pasarlos al HomeBottomPanel
 */
@Composable
fun HomeScreenContent(
    viewModel: MainViewModel,
    onNavigateToPlanner: () -> Unit,
    onLineClicked: (String) -> Unit,
    onStationClicked: (String) -> Unit,
    onViewAllLines: () -> Unit,
    onViewAllStations: () -> Unit
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
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = mapUiSettings
            ) {
                uiState.allStations.forEach { station ->
                    Marker(
                        state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                        title = station.name,
                        snippet = "Línea: ${station.lineId}"
                    )
                }
            }

            HomeSearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                onClicked = onNavigateToPlanner
            )

            HomeBottomPanel(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                lines = uiState.allLines,
                popularStations = uiState.allStations.take(2),
                onLineClicked = onLineClicked,
                onStationClicked = onStationClicked,
                onViewAllLines = onViewAllLines,         // <-- CONECTADO
                onViewAllStations = onViewAllStations    // <-- CONECTADO
            )
        }
    }
}

// ... (El resto del archivo: HomeSearchBar, HomeBottomPanel, SectionHeader, LineaCard,
//      EstacionCard, PermissionDeniedScreen ... pégalos aquí tal como los tenías)
// --- COPIA Y PEGA EL RESTO DE TU HOMESCREEN.KT ORIGINAL AQUÍ ---
// (Los composables que ya te había dado: HomeSearchBar, HomeBottomPanel, etc.)

/**
 * NUEVO: El "falso" buscador que flota arriba del mapa.
 * (Sin cambios)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Hola, Carlos", // Basado en tu diseño
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "¿A dónde vamos?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * NUEVO: El panel inferior deslizable.
 * (Sin cambios)
 */
@Composable
fun HomeBottomPanel(
    modifier: Modifier = Modifier,
    lines: List<TransportLine>,
    popularStations: List<Station>,
    onLineClicked: (String) -> Unit,
    onStationClicked: (String) -> Unit,
    onViewAllLines: () -> Unit,
    onViewAllStations: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        // Usamos LazyColumn para el scroll si el contenido crece
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // Padding inferior
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Handle" del Bottom Sheet
            item {
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }

            // --- Sección de Líneas ---
            item {
                SectionHeader(
                    title = "Líneas",
                    onViewAllClicked = onViewAllLines // <-- CONECTADO
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lines) { line ->
                        LineaCard(
                            line = line,
                            onClicked = { onLineClicked(line.id) }
                        )
                    }
                }
            }

            // --- Separador ---
            item {
                Divider(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }

            // --- Sección de Estaciones ---
            item {
                SectionHeader(
                    title = "Estaciones",
                    onViewAllClicked = onViewAllStations // <-- CONECTADO
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    popularStations.forEach { station ->
                        EstacionCard(
                            station = station,
                            onClicked = { onStationClicked(station.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * NUEVO: Cabecera para las secciones "Líneas" y "Estaciones"
 * (Sin cambios)
 */
@Composable
fun SectionHeader(
    title: String,
    onViewAllClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ver todo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onViewAllClicked)
        )
    }
}

/**
 * NUEVO: Tarjeta horizontal para las líneas
 * (Sin cambios)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineaCard(
    line: TransportLine,
    onClicked: () -> Unit
) {
    Card(
        onClick = onClicked,
        modifier = Modifier
            .width(180.dp) // Ancho fijo para tarjetas horizontales
            .height(100.dp), // Altura fija
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween // Alinea contenido
        ) {
            Icon(
                imageVector = Icons.Outlined.Train,
                contentDescription = "Línea",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(line.color)
                    .padding(4.dp),
                tint = Color.White
            )
            Text(
                text = line.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * NUEVO: Tarjeta vertical para las estaciones
 * (Sin cambios)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstacionCard(
    station: Station,
    onClicked: () -> Unit
) {
    Card(
        onClick = onClicked,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Estación",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Línea ${station.lineId}", // Asumiendo que quieres mostrar la línea
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = "Ver detalle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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