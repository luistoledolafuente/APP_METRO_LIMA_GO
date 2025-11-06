package com.tecsup.metrolimago.ui.screens

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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.tecsup.metrolimago.logic.RouteSegment
import com.tecsup.metrolimago.viewmodel.MainViewModel

/**
 * Pantalla que muestra el resultado de la ruta calculada.
 * ¡ACTUALIZADA con lista de paraderos expandible!
 */
@Composable
fun RouteResultScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val route = uiState.calculatedRoute

    // --- ARREGLO DE SEGURIDAD (Sin cambios) ---
    // Esto evita el crash si el usuario presiona "atrás"
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

        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true)
            ) {
                // Marcador de Origen (Nulo-seguro y sin 'itit')
                validRoute.segments.firstOrNull()?.startStation?.let {
                    Marker(
                        state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                        title = "Origen: ${it.name}"
                    )
                }

                // Marcador de Destino (Nulo-seguro y sin 'itit')
                validRoute.segments.lastOrNull()?.endStation?.let {
                    Marker(
                        state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                        title = "Destino: ${it.name}"
                    )
                }

                // Polilíneas
                validRoute.segments.forEach { segment ->
                    Polyline(
                        points = segment.stationsInSegment.map { LatLng(it.latitude, it.longitude) },
                        color = segment.line.color,
                        width = 12f
                    )
                }
            }

            // Botón de Regresar
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
            }

            // Panel Inferior
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

                    // Tarjeta de Resumen (Nulo-seguro)
                    RouteSummaryCard(
                        time = validRoute.totalTimeEstimate,
                        origin = validRoute.segments.firstOrNull()?.startStation?.name ?: "N/A",
                        destination = validRoute.segments.lastOrNull()?.endStation?.name ?: "N/A"
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Lista de detalles
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        itemsIndexed(validRoute.segments) { index, segment ->
                            // --- ¡ESTE ES EL ÚNICO CAMBIO! ---
                            // Ahora usamos el 'RouteSegmentItem' actualizado
                            RouteSegmentItem(segment = segment)

                            segment.transferMessage?.let {
                                TransferItem(message = it)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- RouteSummaryCard (Sin cambios) ---
@Composable
fun RouteSummaryCard(
    time: Int,
    origin: String,
    destination: String
) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = "Tiempo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
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
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Destino",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}


// --- ¡¡¡ESTE ES EL COMPOSABLE ACTUALIZADO!!! ---
/**
 * Un item en la lista de pasos (ej. "Línea 1, 7 estaciones...")
 * AHORA ES EXPANDIBLE para mostrar los paraderos.
 */
@Composable
fun RouteSegmentItem(
    segment: RouteSegment
) {
    // 1. Estado para saber si la lista está expandida o no
    var isExpanded by remember { mutableStateOf(false) }

    // 2. Contamos las estaciones. El dato viene de RouteFinder
    val stationCount = segment.stationsInSegment.size

    // 3. Columna principal que contiene todo
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Fila principal (Icono, Nombre de línea, etc.)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box( // Icono
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(segment.line.color), // Usa el color de la línea
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

            Column(modifier = Modifier.weight(1f)) { // Textos
                Text(
                    text = segment.line.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    // Texto actualizado: "7 estaciones" en lugar de "6 paradas"
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
        } // Fin de Row principal

        // --- 4. PARTE NUEVA: El botón "Ver/Ocultar paraderos" ---
        if (stationCount > 1) { // Solo mostrar si hay más de 1 estación
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isExpanded) "Ocultar paraderos" else "Ver $stationCount paraderos",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium, // Un poco más grande
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 56.dp) // Alineado con el texto (40dp icono + 16dp spacer)
                    .clickable { isExpanded = !isExpanded } // Cambia el estado al hacer clic
            )

            // --- 5. PARTE NUEVA: La lista expandible de paraderos ---
            AnimatedVisibility(visible = isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 56.dp) // Alineado
                ) {
                    // Muestra cada estación en la lista del segmento
                    segment.stationsInSegment.forEach { station ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            // Viñeta (bullet point)
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


// --- TransferItem (Sin cambios) ---
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
            imageVector = Icons.Outlined.DirectionsWalk,
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