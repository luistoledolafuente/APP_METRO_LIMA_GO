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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Restaurant

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

import coil.compose.AsyncImage
import com.tecsup.metrolimago.BuildConfig
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tecsup.metrolimago.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// --- ¡NUEVAS LISTAS DE DATOS MOCK! ---
import com.tecsup.metrolimago.data.database.Station
import kotlin.math.abs

// Listas de datos estáticos para la simulación
private val mockBancos = listOf("BCP", "Interbank", "Scotiabank", "BBVA", "Banco de la Nación")
private val mockFarmacias = listOf("Inkafarma", "MiFarma", "Farmacia Universal", "Farmacia del Pueblo", "Boticas y Salud")
private val mockRestaurantes = listOf("Norky's", "Bembos", "Tambo", "KFC", "Pizza Hut", "McDonald's")
// --- FIN DE LISTAS MOCK ---


// (Centro de Lima - Fallback)
private val limaCenter = LatLng(-12.046374, -77.042793)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailScreen(
    viewModel: MainViewModel,
    stationId: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- LÓGICA DE DATOS ---
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
        zoomControlsEnabled = true,
        zoomGesturesEnabled = true,
        scrollGesturesEnabled = true
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(station?.name ?: "Cargando...") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = line?.color ?: MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    if (station != null) {
                        IconButton(onClick = {
                            viewModel.toggleFavorite(station)
                        }) {
                            Icon(
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

                // --- 1. Tarjeta del Mapa (ACTUALIZADA) ---
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    cameraPositionState = cameraPositionState,
                    uiSettings = mapUiSettings
                ) {
                    // Marcador de la Estación (Principal)
                    Marker(
                        state = MarkerState(position = stationLocation),
                        title = station.name
                    )

                    // --- ¡MARCADORES MOCK DINÁMICOS! ---

                    // 1. Obtenemos los nombres dinámicos
                    val (banco, farmacia, restaurante) = getMockServiceNames(station)

                    // 2. Creamos los pines con esos nombres
                    val mockServices = listOf(
                        Pair(LatLng(stationLocation.latitude + 0.0008, stationLocation.longitude + 0.0008), restaurante),
                        Pair(LatLng(stationLocation.latitude - 0.0005, stationLocation.longitude + 0.001), farmacia),
                        Pair(LatLng(stationLocation.latitude + 0.0002, stationLocation.longitude - 0.001), banco)
                    )

                    mockServices.forEach { (location, title) ->
                        Marker(
                            state = MarkerState(position = location),
                            title = title, // Título dinámico
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }
                    // --- FIN DE MARCADORES MOCK ---
                }

                // --- 2. IMAGEN AUTOMÁTICA DE STREET VIEW ---
                val apiKey = BuildConfig.MAPS_API_KEY
                val imageUrl = "https://maps.googleapis.com/maps/api/streetview?" +
                        "size=600x400" +
                        "&location=${station.latitude},${station.longitude}" +
                        "&pitch=-20" +
                        "&key=$apiKey"

                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Foto de la estación ${station.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_launcher_background),
                    error = painterResource(id = R.drawable.ic_launcher_background)
                )

                // --- Contenido Principal (Tarjetas) ---
                Column(modifier = Modifier.padding(16.dp)) {

                    // --- 3. Tarjeta de Información ---
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

                    // --- 4. Tarjeta de Tarifas ---
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tarifas y Pagos", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            PaymentInfo(lineId = line.id)
                        }
                    }

                    // --- 5. Tarjeta de Servicios Cercanos (ACTUALIZADA) ---
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Servicios Cercanos (Simulado)", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // ¡Llamamos al Composable pasándole la estación!
                            NearbyServices(station = station)
                        }
                    }
                }
            }
        }
    }
}

/**
 * ¡NUEVA FUNCIÓN HELPER!
 * Genera nombres de servicios mock "pseudo-aleatorios" pero consistentes
 * basados en el nombre de la estación.
 */
@Composable
private fun getMockServiceNames(station: Station): Triple<String, String, String> {
    // Usamos el 'hashCode' para obtener un número "único" de la estación
    val index = abs(station.name.hashCode())

    // Usamos el índice para seleccionar de forma consistente un item de cada lista
    val banco = mockBancos[index % mockBancos.size]
    val farmacia = mockFarmacias[index % mockFarmacias.size]
    val restaurante = mockRestaurantes[index % mockRestaurantes.size]

    return Triple(banco, farmacia, restaurante)
}


/**
 * Composable helper para Servicios (ACTUALIZADO)
 * Ahora recibe la estación para generar datos dinámicos.
 */
@Composable
private fun NearbyServices(station: Station) {

    // 1. Obtenemos los nombres dinámicos
    val (banco, farmacia, restaurante) = getMockServiceNames(station)

    // 2. Mostramos los nombres en la UI
    InfoRow(
        icon = Icons.Default.Restaurant,
        label = "Restaurantes",
        value = restaurante
    )
    InfoRow(
        icon = Icons.Default.LocalPharmacy,
        label = "Farmacias",
        value = farmacia
    )
    InfoRow(
        icon = Icons.Default.AccountBalance,
        label = "Bancos/Agentes",
        value = banco
    )
}


/**
 * Composable helper para Tarifas (Sin cambios)
 */
@Composable
private fun PaymentInfo(lineId: String) {
    val fare: String
    val paymentMethod: String

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