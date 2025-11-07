package com.tecsup.metrolimago.ui.screens // Asegúrate que coincida con tu paquete

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.metrolimago.data.database.FavoriteRoute // <-- IMPORTAR
import com.tecsup.metrolimago.data.database.Station // <-- IMPORTAR
import com.tecsup.metrolimago.viewmodel.MainViewModel

/**
 * ¡PANTALLA ACTUALIZADA!
 * Muestra "Rutas Favoritas" Y "Estaciones Favoritas" (Req 6)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: MainViewModel,
    onStationClicked: (String) -> Unit,
    onRouteClicked: () -> Unit // <-- 1. PARÁMETRO AÑADIDO
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteStations = uiState.favoriteStations
    val favoriteRoutes = uiState.favoriteRoutes // <-- 2. OBTENER RUTAS

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Favoritos") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- 3. ¡NUEVA SECCIÓN DE RUTAS FAVORITAS! ---
            item {
                Text(
                    "Rutas Favoritas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (favoriteRoutes.isEmpty() && !uiState.isAppLoading) {
                item {
                    Text(
                        text = "Aún no has guardado rutas. Presiona la estrella (★) en la pantalla de resultados de una ruta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            } else {
                items(favoriteRoutes) { route ->
                    FavoriteRouteItem(
                        route = route,
                        onClicked = {
                            viewModel.selectFavoriteRoute(route)
                            onRouteClicked() // Navega a la pantalla de resultados
                        },
                        onDelete = {
                            viewModel.deleteFavoriteRoute(route)
                        }
                    )
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            // --- 4. SECCIÓN DE ESTACIONES FAVORITAS ---
            item {
                Text(
                    "Estaciones Favoritas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (favoriteStations.isEmpty() && !uiState.isAppLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no has añadido estaciones. Presiona la estrella (★) en el detalle de una estación.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(favoriteStations) { station ->
                    // Reutilizamos el Composable de tu compañero (asumimos que está en AllStationsScreen.kt)
                    StationListItemClickable(
                        station = station,
                        onStationClicked = { onStationClicked(station.id) }
                    )
                }
            }
        }
    }
}

/**
 * ¡NUEVO COMPOSABLE!
 * Muestra un ítem de Ruta Favorita (De: ... A: ...)
 */
@Composable
fun FavoriteRouteItem(
    route: FavoriteRoute,
    onClicked: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClicked),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de flecha
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Ruta",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Textos (Origen y Destino)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "De: ${route.originName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "A: ${route.destinationName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Botón de borrar
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}