package com.tecsup.metrolimago.ui.screens // Asegúrate que coincida con tu paquete

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.tecsup.metrolimago.logic.RouteSegment
import com.tecsup.metrolimago.viewmodel.MainViewModel

private val limaCenter = LatLng(-12.046374, -77.042793)
private val mapUiSettings = MapUiSettings(
    zoomControlsEnabled = false,
    myLocationButtonEnabled = true
)

/**
 * Pantalla que muestra el resultado de la ruta calculada.
 * (Corregido el error de 'item' anidado)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteResultScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val route = uiState.calculatedRoute
    val isFavorite = uiState.isCurrentRouteFavorite != null

    route?.let { validRoute ->

        val cameraPositionState = rememberCameraPositionState()

        LaunchedEffect(validRoute) {
            if (validRoute.segments.isNotEmpty() && validRoute.segments.all { it.stationsInSegment.isNotEmpty() }) {
                val boundsBuilder = LatLngBounds.Builder()
                validRoute.segments.flatMap { it.stationsInSegment }.forEach { station ->
                    boundsBuilder.include(LatLng(station.latitude, station.longitude))
                }
                try {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150)
                    )
                } catch (e: Exception) { /* Ignorar error de bounds */ }
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Tu Ruta") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.toggleCurrentRouteFavorite() }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Guardar Ruta Favorita"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = mapUiSettings,
                    properties = MapProperties(isMyLocationEnabled = true)
                ) {
                    validRoute.segments.firstOrNull()?.startStation?.let {
                        Marker(
                            state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                            title = "Origen: ${it.name}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
                    }
                    validRoute.segments.lastOrNull()?.endStation?.let {
                        Marker(
                            state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                            title = "Destino: ${it.name}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                    validRoute.segments.forEach { segment ->
                        Polyline(
                            points = segment.stationsInSegment.map { LatLng(it.latitude, it.longitude) },
                            color = segment.line.color,
                            width = 12f
                        )
                    }
                }

                // (El botón flotante de 'Atrás' fue reemplazado por la TopAppBar)

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxSize(0.6f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box( // Handle
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .width(40.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                .align(Alignment.CenterHorizontally)
                        )

                        RouteSummaryCard(
                            time = validRoute.totalTimeEstimate,
                            cost = validRoute.totalCost,
                            origin = validRoute.segments.firstOrNull()?.startStation?.name ?: "N/A",
                            destination = validRoute.segments.lastOrNull()?.endStation?.name ?: "N/A"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        // Lista de detalles
                        LazyColumn(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            itemsIndexed(validRoute.segments) { index, segment ->
                                // Composable 1
                                RouteSegmentItem(segment = segment)

                                // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
                                // Composable 2 (llamado directamente, sin 'item')
                                segment.transferMessage?.let {
                                    TransferItem(message = it)
                                }
                                // --- FIN DE LA CORRECCIÓN ---
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- (El resto de los Composables [RouteSummaryCard, RouteSegmentItem, TransferItem]
// ---  se quedan exactamente igual que en el archivo anterior) ---

@Composable
fun RouteSummaryCard(
    time: Int,
    cost: Double,
    origin: String,
    destination: String
) {
    val costString = String.format("S/ %.2f", cost)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = "Tiempo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$time min",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Desde $origin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.MonetizationOn,
                    contentDescription = "Costo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = costString,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Estimado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RouteSegmentItem(
    segment: RouteSegment
) {
    var isExpanded by remember { mutableStateOf(false) }
    val stationCount = segment.stationsInSegment.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(segment.line.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Train,
                    contentDescription = "Línea",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = segment.line.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (stationCount > 1) "$stationCount estaciones" else "Estación final",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Bajar en: ${segment.endStation.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (stationCount > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isExpanded) "Ocultar paraderos" else "Ver $stationCount paraderos",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 56.dp)
                    .clickable { isExpanded = !isExpanded }
            )
            AnimatedVisibility(visible = isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 56.dp)
                ) {
                    segment.stationsInSegment.forEach { station ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransferItem(
    message: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.DirectionsWalk,
            contentDescription = "Transbordo",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(
                text = "Transbordo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}