package com.tecsup.metrolimago.ui.screens // Asegúrate que coincida con tu paquete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.metrolimago.viewmodel.MainViewModel

/**
 * ¡NUEVA PANTALLA!
 * Muestra la lista de estaciones favoritas (Req 6.b).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: MainViewModel,
    onStationClicked: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteStations = uiState.favoriteStations

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Estaciones Favoritas") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->

        // Si la lista está vacía, mostramos un mensaje
        if (favoriteStations.isEmpty() && !uiState.isAppLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no has añadido favoritos.\nPresiona la estrella (★) en el detalle de una estación para guardarla aquí.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Si hay favoritos, los mostramos en una lista
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteStations) { station ->
                    // Reutilizamos el Composable de tu compañero
                    // (Definido en AllStationsScreen.kt)
                    StationListItemClickable(
                        station = station,
                        onStationClicked = { onStationClicked(station.id) }
                    )
                }
            }
        }
    }
}