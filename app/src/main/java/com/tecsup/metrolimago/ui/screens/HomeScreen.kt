package com.tecsup.metrolimago.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
// --- NUEVOS IMPORTS ---
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Train
// --- FIN NUEVOS IMPORTS ---
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.maps.android.compose.*
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.data.database.TransportLine
import com.tecsup.metrolimago.viewmodel.MainViewModel

// (limaCenter y mapUiSettings sin cambios)
private val limaCenter = LatLng(-12.046374, -77.042793)
private val mapUiSettings = MapUiSettings(
    zoomControlsEnabled = false,
    myLocationButtonEnabled = true
)

/**
 * --- ¡ACTUALIZADO! ---
 * Añadimos el nuevo parámetro onNavigateToSettings
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToPlanner: () -> Unit,
    onLineClicked: (String) -> Unit,
    onStationClicked: (String) -> Unit,
    onViewAllLines: () -> Unit,
    onViewAllStations: () -> Unit,
    onNavigateToSettings: () -> Unit // <-- PARÁMETRO AÑADIDO
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
            onStationClicked = onStationClicked,
            onViewAllLines = onViewAllLines,
            onViewAllStations = onViewAllStations,
            onNavigateToSettings = onNavigateToSettings // <-- PASAMOS EL PARÁMETRO
        )
    } else {
        PermissionDeniedScreen(
            onGrantPermission = { permissionState.launchMultiplePermissionRequest() }
        )
    }
}

/**
 * --- ¡ACTUALIZADO! ---
 * Añadimos el parámetro para pasarlo al HomeSearchBar
 */
@Composable
fun HomeScreenContent(
    viewModel: MainViewModel,
    onNavigateToPlanner: () -> Unit,
    onLineClicked: (String) -> Unit,
    onStationClicked: (String) -> Unit,
    onViewAllLines: () -> Unit,
    onViewAllStations: () -> Unit,
    onNavigateToSettings: () -> Unit // <-- PARÁMETRO AÑADIDO
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

            // --- 2. EL BUSCADOR (Arriba) ---
            HomeSearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                onClicked = onNavigateToPlanner,
                onSettingsClicked = onNavigateToSettings // <-- CONECTADO
            )

            // --- 3. EL PANEL INFERIOR (Abajo) ---
            HomeBottomPanel(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                lines = uiState.allLines,
                popularStations = uiState.allStations.take(2),
                onLineClicked = onLineClicked,
                onStationClicked = onStationClicked,
                onViewAllLines = onViewAllLines,
                onViewAllStations = onViewAllStations
            )
        }
    }
}

/**
 * --- ¡ACTUALIZADO! ---
 * Añadimos onSettingsClicked y el IconButton de Perfil
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    modifier: Modifier = Modifier,
    onClicked: () -> Unit,
    onSettingsClicked: () -> Unit // <-- PARÁMETRO AÑADIDO
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
            Icon( // Icono de Búsqueda
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) { // Textos
                Text(
                    text = "Hola, Carlos",
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
            Spacer(modifier = Modifier.width(8.dp))

            // --- ¡BOTÓN DE AJUSTES AÑADIDO! ---
            IconButton(
                onClick = onSettingsClicked,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Ajustes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// --- El resto de los composables (HomeBottomPanel, SectionHeader, LineaCard,
//      EstacionCard, PermissionDeniedScreen) permanecen EXACTAMENTE IGUAL ---
// ... (puedes dejar los que ya tenías)

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
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            item {
                SectionHeader(
                    title = "Líneas",
                    onViewAllClicked = onViewAllLines
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
            item {
                Divider(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
            item {
                SectionHeader(
                    title = "Estaciones",
                    onViewAllClicked = onViewAllStations
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineaCard(
    line: TransportLine,
    onClicked: () -> Unit
) {
    Card(
        onClick = onClicked,
        modifier = Modifier
            .width(180.dp)
            .height(100.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
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
                    text = "Línea ${station.lineId}",
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