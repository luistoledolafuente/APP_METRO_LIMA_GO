package com.tecsup.metrolimago.ui.screens // Asegúrate que coincida con tu paquete

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
 * SIMPLIFICADO para funcionar con la Bottom Nav Bar.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToPlanner: () -> Unit,
    onLineClicked: (String) -> Unit, // Lo mantenemos por si hacemos click en el Polyline
    onStationClicked: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    // --- PARÁMETROS ELIMINADOS (ya no los necesitamos) ---
    onViewAllLines: () -> Unit,
    onViewAllStations: () -> Unit
) {
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Arreglo: Usamos launchMultiplePermissionRequest
    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    if (permissionState.allPermissionsGranted) {
        HomeScreenContent(
            viewModel = viewModel,
            onNavigateToPlanner = onNavigateToPlanner,
            onStationClicked = onStationClicked,
            onNavigateToSettings = onNavigateToSettings
        )
    } else {
        PermissionDeniedScreen(
            onGrantPermission = { permissionState.launchMultiplePermissionRequest() }
        )
    }
}

/**
 * --- ¡ACTUALIZADO! ---
 * Ya no necesita el HomeBottomPanel
 */
@Composable
fun HomeScreenContent(
    viewModel: MainViewModel,
    onNavigateToPlanner: () -> Unit,
    onStationClicked: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(limaCenter, 11f)
    }

    // El Scaffold se movió a MainActivity, aquí solo usamos un Box
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = mapUiSettings
        ) {
            uiState.allStations.forEach { station ->
                // NOTA: Hacemos los marcadores clickables
                Marker(
                    state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                    title = station.name,
                    snippet = "Clic para ver detalle",
                    onInfoWindowClick = {
                        // Al hacer clic en el popup del marcador, navegamos
                        onStationClicked(station.id)
                    }
                )
            }
        }

        // --- 2. EL BUSCADOR (Arriba) ---
        // (Usamos el buscador de tu compañero)
        HomeSearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            onClicked = onNavigateToPlanner,
            onSettingsClicked = onNavigateToSettings
        )

        // --- 3. EL PANEL INFERIOR (HomeBottomPanel) FUE ELIMINADO ---
        // Sus funciones (Ver Líneas, Ver Estaciones)
        // ahora están en la Bottom Navigation Bar.
    }
}


/**
 * (El HomeSearchBar de tu compañero se queda igual)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    modifier: Modifier = Modifier,
    onClicked: () -> Unit,
    onSettingsClicked: () -> Unit
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

/**
 * (PermissionDeniedScreen de tu compañero se queda igual)
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

