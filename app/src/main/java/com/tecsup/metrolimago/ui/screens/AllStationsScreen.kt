package com.tecsup.metrolimago.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.viewmodel.MainViewModel

/**
 * Pantalla: Muestra la lista completa de estaciones.
 * ¡ACTUALIZADA con el nuevo buscador y estilo de lista!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllStationsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onStationClicked: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- 1. ESTADO PARA EL BUSCADOR ---
    var searchText by remember { mutableStateOf("") }

    // --- 2. LÓGICA DE FILTRADO ---
    val filteredStations = uiState.allStations.filter { station ->
        station.name.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Todas las Estaciones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        // --- 3. LAYOUT DE LA PANTALLA ---
        // Columna que contiene el buscador y la lista
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- 4. BARRA DE BÚSQUEDA ---
            // Este es el nuevo estilo de la image_7b6e44.png
            StationSearchBar(
                searchText = searchText,
                onSearchChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Padding alrededor del buscador
            )

            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // --- 5. LISTA FILTRADA ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp), // Padding para la lista
                verticalArrangement = Arrangement.spacedBy(8.dp) // Espacio entre items
            ) {
                items(filteredStations) { station -> // Usamos la lista FILTRADA
                    // --- 6. NUEVO ESTILO DE ITEM DE LISTA ---
                    // Este es el estilo de la image_7b6e44.png
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
 * (¡NUEVO!) Composable para la barra de búsqueda.
 * (Copiado del estilo del BottomSheet de RoutePlanner)
 */
@Composable
fun StationSearchBar(
    searchText: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = searchText,
        onValueChange = onSearchChange,
        modifier = modifier,
        placeholder = { Text("Buscar estación...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Buscar")
        },
        shape = RoundedCornerShape(12.dp), // Esquinas redondeadas
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent, // Sin línea abajo
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        singleLine = true
    )
}


/**
 * (¡NUEVO!) Composable para el item de la lista de estaciones.
 * (Copiado del estilo del BottomSheet de RoutePlanner)
 */
@Composable
fun StationListItemClickable(
    station: Station,
    onStationClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)) // Opcional: redondear cada item
            .clickable(onClick = onStationClicked)
            .padding(vertical = 12.dp, horizontal = 8.dp), // Padding interno
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = "Estación",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = station.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Línea ${station.lineId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}