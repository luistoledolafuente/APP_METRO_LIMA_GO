package com.tecsup.metrolimago.ui.screens

// --- INICIO DE IMPORTS FALTANTES ---
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// --- FIN DE IMPORTS FALTANTES ---
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.viewmodel.MainViewModel

/**
 * Pantalla REAL de Planificación de Ruta.
 * Permite al usuario elegir un origen y un destino.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState.calculatedRoute) {
        if (uiState.calculatedRoute != null) {
            onNavigateToResult()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Planificar Ruta") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top // Alineamos arriba
        ) {
            Text(
                "Selecciona tu viaje",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- Selector de Origen ---
            StationSelector(
                label = "Origen",
                allStations = uiState.allStations,
                selectedStation = uiState.selectedOrigin,
                onStationSelected = { station ->
                    viewModel.onOriginStationSelected(station)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Selector de Destino ---
            StationSelector(
                label = "Destino",
                allStations = uiState.allStations,
                selectedStation = uiState.selectedDestination,
                onStationSelected = { station ->
                    viewModel.onDestinationStationSelected(station)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Botón de Búsqueda ---
            Button(
                onClick = {
                    viewModel.calculateRoute()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedOrigin != null && uiState.selectedDestination != null
            ) {
                Text(text = "Buscar Ruta")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Mensajes de Error ---
            uiState.routeErrorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Composable reutilizable para un selector de estación (Dropdown).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSelector(
    label: String,
    allStations: List<Station>,
    selectedStation: Station?,
    onStationSelected: (Station) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedStation?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            allStations.forEach { station ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(station.name)
                            Text(
                                text = station.lineId,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onStationSelected(station)
                        expanded = false
                    }
                )
            }
        }
    }
}