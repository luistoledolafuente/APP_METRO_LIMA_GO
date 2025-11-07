package com.tecsup.metrolimago.ui.screens // Asegúrate que coincida con tu paquete

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
// --- 1. IMPORTAR ÍCONOS DE ESTRELLA ---
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
// --- FIN DE IMPORTS ---

// --- ¡NUEVOS IMPORTS AÑADIDOS! ---
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MonetizationOn
// --- FIN DE NUEVOS IMPORTS ---

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

// --- NUEVAS IMPORTACIONES PARA LA IMAGEN AUTOMÁTICA ---
import coil.compose.AsyncImage
import com.tecsup.metrolimago.BuildConfig
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tecsup.metrolimago.R
// --- FIN DE NUEVAS IMPORTACIONES ---

// (Centro de Lima - Fallback)
private val limaCenter = LatLng(-12.046374, -77.042793)

/**
 * Pantalla que muestra el detalle de una estación (Req 2).
 * ¡ACTUALIZADA CON BOTÓN DE FAVORITOS E IMAGEN AUTOMÁTICA!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailScreen(
    viewModel: MainViewModel,
    stationId: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- LÓGICA DE DATOS ---
    // Buscamos la estación en 'allStations' para tener el estado 'isFavorite' más reciente
    val station = uiState.allStations.find { it.id == stationId }
    val line = uiState.allLines.find { it.id == station?.lineId }

    // --- LÓGICA DE MAPA ---
    val stationLocation = station?.let { LatLng(it.latitude, it.longitude) } ?: limaCenter
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(limaCenter, 12f)
    }

    LaunchedEffect(stationLocation) {
        if (stationLocation != limaCenter) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(stationLocation, 16f),
                durationMs = 1500
            )
        }
    }

    val mapUiSettings = MapUiSettings(
        zoomControlsEnabled = false,
        zoomGesturesEnabled = false,
        scrollGesturesEnabled = false
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(station?.name ?: "Cargando...") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = line?.color ?: MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary // Color para la estrella
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                // --- Botón de Acción (Estrella) ---
                actions = {
                    // Solo mostramos el botón si la estación ha cargado
                    if (station != null) {
                        IconButton(onClick = {
                            // ¡Llamamos al ViewModel!
                            viewModel.toggleFavorite(station)
                        }) {
                            Icon(
                                // Cambia el ícono si es favorito o no
                                imageVector = if (station.isFavorite) {
                                    Icons.Filled.Star
                                } else {
                                    Icons.Outlined.StarBorder
                                },
                                contentDescription = "Marcar como Favorito"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        if (uiState.isAppLoading || station == null || line == null) {
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
                    Marker(
                        state = MarkerState(position = stationLocation),
                        title = station.name
                    )
                }

                // --- 2. IMAGEN AUTOMÁTICA DE STREET VIEW ---

                // Obtenemos la API Key desde el BuildConfig
                val apiKey = BuildConfig.MAPS_API_KEY

                // Construimos la URL de Street View
                val imageUrl = "https://maps.googleapis.com/maps/api/streetview?" +
                        "size=600x400" + // Tamaño de la imagen
                        "&location=${station.latitude},${station.longitude}" + // Coordenadas
                        "&pitch=-20" + // Un pequeño ángulo hacia abajo
                        "&key=$apiKey"

                // Usamos AsyncImage de Coil para cargar la URL
                AsyncImage(
                    model = imageUrl, // La URL que acabamos de construir
                    contentDescription = "Foto de la estación ${station.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop, // Rellena el espacio

                    // Imagen que se muestra mientras carga (fondo de tu ícono)
                    placeholder = painterResource(id = R.drawable.ic_launcher_background),

                    // Imagen que se muestra si falla (ej. sin internet o sin foto)
                    error = painterResource(id = R.drawable.ic_launcher_background)
                )

                // --- 3. Tarjeta de Información (Contenido) ---
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Información", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            InfoRow(icon = Icons.Default.Info, label = "Estado", value = station.status)
                            InfoRow(icon = Icons.Default.Schedule, label = "Horario", value = station.schedule)
                            InfoRow(icon = Icons.Default.Business, label = "Línea", value = line.name)
                        }
                    }

                    // --- 4. ¡NUEVA TARJETA DE TARIFAS! ---
                    Spacer(modifier = Modifier.height(16.dp)) // Espacio entre tarjetas
                    Text("Tarifas y Pagos", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // ¡Llamamos al nuevo Composable!
                            PaymentInfo(lineId = line.id)
                        }
                    }
                    // --- FIN DE LA NUEVA TARJETA ---
                }
            }
        }
    }
}

/**
 * ¡NUEVO COMPOSABLE!
 * Un Composable helper para mostrar la info de tarifa y pago
 * según el ID de la línea.
 */
@Composable
private fun PaymentInfo(lineId: String) {
    val fare: String
    val paymentMethod: String

    // Determinamos la información basada en el ID de la línea
    when (lineId) {
        "L1" -> {
            fare = "S/ 1.50 (Tarifa General)"
            paymentMethod = "Tarjeta recargable de Línea 1"
        }
        "L2" -> {
            fare = "S/ 1.50 (Tarifa General)"
            paymentMethod = "Tarjeta interoperable (futuro)"
        }
        "CA" -> {
            fare = "S/ 2.35 (Tarifa General)"
            paymentMethod = "Tarjeta Lima Pass o Metropolitano"
        }
        "METRO" -> {
            fare = "S/ 3.20 (Tarifa Troncal)"
            paymentMethod = "Tarjeta Lima Pass o Metropolitano"
        }
        else -> {
            fare = "No disponible"
            paymentMethod = "No disponible"
        }
    }

    // Usamos el InfoRow que ya existía
    InfoRow(
        icon = Icons.Default.MonetizationOn,
        label = "Tarifa",
        value = fare
    )
    InfoRow(
        icon = Icons.Default.CreditCard,
        label = "Método de Pago",
        value = paymentMethod
    )
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