package com.tecsup.metrolimago.ui.screens

// --- INICIO DE TODOS LOS IMPORTS NECESARIOS ---
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PersonPin
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
// ¡Este es el import que faltaba!
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.logic.RouteResult
import com.tecsup.metrolimago.logic.RouteSegment
import com.tecsup.metrolimago.viewmodel.MainViewModel
// --- FIN DE LOS IMPORTS ---

private val limaCenter = LatLng(-12.046374, -77.042793)
private val mapUiSettings = MapUiSettings(
    zoomControlsEnabled = true,
    myLocationButtonEnabled = true
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteResultScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val route = uiState.calculatedRoute
    val origin = uiState.selectedOrigin
    val destination = uiState.selectedDestination

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(limaCenter, 11f)
    }

    LaunchedEffect(route) {
        if (route != null && origin != null && destination != null) {
            val boundsBuilder = LatLngBounds.Builder()
            route.segments.forEach { segment ->
                segment.stationsInSegment.forEach { station ->
                    boundsBuilder.include(LatLng(station.latitude, station.longitude))
                }
            }
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150),
                durationMs = 1500
            )
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
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
                // ¡El error estaba aquí! Faltaba el import de MapProperties
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                if (route != null && origin != null && destination != null) {

                    Marker(
                        state = MarkerState(position = LatLng(origin.latitude, origin.longitude)),
                        title = "Origen: ${origin.name}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                    Marker(
                        state = MarkerState(position = LatLng(destination.latitude, destination.longitude)),
                        title = "Destino: ${destination.name}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )

                    route.segments.forEach { segment ->
                        val points = segment.stationsInSegment.map {
                            LatLng(it.latitude, it.longitude)
                        }

                        Polyline(
                            points = points,
                            color = segment.line.color,
                            width = 15f
                        )

                        if (segment.transferMessage != null) {
                            Marker(
                                state = MarkerState(position = LatLng(segment.endStation.latitude, segment.endStation.longitude)),
                                title = "Transbordo en ${segment.endStation.name}",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                shadowElevation = 8.dp
            ) {
                if (route == null || origin == null || destination == null) {
                    Text("Cargando ruta...", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            RouteSummaryCard(
                                route = route,
                                origin = origin,
                                destination = destination
                            )
                        }

                        route.segments.forEachIndexed { index, segment ->
                            item { SegmentHeader(segment, index == 0) }

                            items(segment.stationsInSegment.drop(1).dropLast(1)) { station ->
                                RouteStepItem(stationName = station.name)
                            }

                            item {
                                RouteStepItem(
                                    stationName = segment.endStation.name,
                                    isLastInSegment = true,
                                    transferMessage = segment.transferMessage
                                )
                            }
                        }

                        item { DestinationItem(destination = destination) }
                    }
                }
            }
        }
    }
}

// --- TODOS LOS COMPOSABLES HELPER (Con 1 corrección) ---

@Composable
fun RouteSummaryCard(route: RouteResult, origin: Station, destination: Station) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tiempo estimado: ${route.totalTimeEstimate} min",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Flag,
                    contentDescription = "Origen",
                    tint = Color(0xFF388E3C)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Origen: ${origin.name} (${origin.lineId})")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Destino",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Destino: ${destination.name} (${destination.lineId})")
            }
        }
    }
}

@Composable
fun SegmentHeader(segment: RouteSegment, isFirstStep: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = if(isFirstStep) Icons.Filled.Flag else Icons.Filled.PersonPin,
            contentDescription = "Inicio de segmento",
            tint = segment.line.color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = if (isFirstStep) "Inicia en ${segment.startStation.name}" else "Sube en ${segment.startStation.name}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Toma la ${segment.line.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = segment.line.color
            )
        }
    }
}

@Composable
fun RouteStepItem(
    stationName: String,
    isLastInSegment: Boolean = false,
    transferMessage: String? = null
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            HorizontalDivider(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f),
                color = Color.LightGray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = stationName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if(isLastInSegment) FontWeight.Bold else FontWeight.Normal
            )
            if (isLastInSegment && transferMessage != null) {
                Text(
                    text = transferMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun DestinationItem(destination: Station) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Destino",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Llegaste a ${destination.name}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}