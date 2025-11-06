package com.tecsup.metrolimago.viewmodel // Asegúrate que coincida con tu paquete

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.metrolimago.data.OfflineRepository
import com.tecsup.metrolimago.data.database.AppDatabase
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.data.database.TransportLine
import com.tecsup.metrolimago.logic.RouteFinder
import com.tecsup.metrolimago.logic.RouteResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la UI (User Interface).
 * (¡ACTUALIZADO CON FAVORITOS!)
 */
data class AppUiState(
    // Listas principales (cargadas al inicio)
    val allLines: List<TransportLine> = emptyList(),
    val allStations: List<Station> = emptyList(),

    // --- ¡CAMBIO AÑADIDO! ---
    val favoriteStations: List<Station> = emptyList(),

    // Estado para el Detalle de Línea
    val selectedLineStations: List<Station> = emptyList(),
    val isLineDetailLoading: Boolean = false,

    // Estado para el Planificador de Ruta
    val selectedOrigin: Station? = null,
    val selectedDestination: Station? = null,
    val calculatedRoute: RouteResult? = null,
    val routeErrorMessage: String? = null,

    // Estado de Carga General
    val isAppLoading: Boolean = true
)

/**
 * ViewModel principal de la aplicación.
 * (¡ACTUALIZADO CON LÓGICA DE FAVORITOS!)
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // --- 1. INICIALIZACIÓN ---
    private val repository: OfflineRepository
    private val routeFinder: RouteFinder

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = OfflineRepository(database.lineDao(), database.stationDao())
        routeFinder = RouteFinder(repository)

        loadInitialAppData()
    }

    // --- 2. CARGA DE DATOS ---

    /**
     * Carga los datos globales (líneas, estaciones Y FAVORITOS)
     * una sola vez cuando se inicia el ViewModel.
     */
    private fun loadInitialAppData() {
        viewModelScope.launch {
            // --- ¡CAMBIO AÑADIDO! ---
            // Ahora 'combine' también escucha a 'getFavoriteStations'
            combine(
                repository.getAllLines(),
                repository.getAllStations(),
                repository.getFavoriteStations() // <-- La nueva fuente de datos
            ) { lines, stations, favorites ->
                // Creamos un objeto temporal
                Triple(lines, stations, favorites)
            }.collect { (lines, stations, favorites) ->
                // Actualizamos el estado de la UI con los nuevos datos
                _uiState.update { currentState ->
                    currentState.copy(
                        allLines = lines,
                        allStations = stations,
                        favoriteStations = favorites, // <-- Actualizamos favoritos
                        isAppLoading = false
                    )
                }
            }
        }
    }

    /**
     * Carga las estaciones para una línea específica.
     * (Sin cambios)
     */
    fun loadStationsForLine(lineId: String) {
        _uiState.update { it.copy(isLineDetailLoading = true) }

        viewModelScope.launch {
            // Usamos 'stateIn' para convertir el Flow frío en caliente y tomar el valor actual
            val stations = repository.getStationsByLine(lineId).stateIn(viewModelScope).value

            _uiState.update {
                it.copy(
                    selectedLineStations = stations,
                    isLineDetailLoading = false
                )
            }
        }
    }

    /**
     * Limpia la lista de estaciones cuando salimos de la pantalla de detalle.
     * (Sin cambios)
     */
    fun clearSelectedLine() {
        _uiState.update {
            it.copy(
                selectedLineStations = emptyList(),
                isLineDetailLoading = false
            )
        }
    }


    // --- 3. ACCIONES DE LA UI (Planificador de Ruta) ---
    // (Sin cambios en esta sección)

    fun onOriginStationSelected(station: Station) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedOrigin = station,
                calculatedRoute = null,
                routeErrorMessage = null
            )
        }
    }

    fun onDestinationStationSelected(station: Station) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedDestination = station,
                calculatedRoute = null,
                routeErrorMessage = null
            )
        }
    }

    fun calculateRoute() {
        val origin = _uiState.value.selectedOrigin
        val destination = _uiState.value.selectedDestination

        if (origin == null || destination == null) {
            _uiState.update { it.copy(routeErrorMessage = "Selecciona origen y destino") }
            return
        }
        if (origin.id == destination.id) {
            _uiState.update { it.copy(routeErrorMessage = "El origen y destino no pueden ser iguales") }
            return
        }

        viewModelScope.launch {
            try {
                val route = routeFinder.findRoute(origin.id, destination.id)

                if (route != null) {
                    _uiState.update {
                        it.copy(calculatedRoute = route, routeErrorMessage = null)
                    }
                } else {
                    _uiState.update {
                        it.copy(routeErrorMessage = "No se encontró una ruta directa o con 1 transbordo.", calculatedRoute = null)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(routeErrorMessage = e.message, calculatedRoute = null)
                }
            }
        }
    }

    fun clearRouteSearch() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedOrigin = null,
                selectedDestination = null,
                calculatedRoute = null,
                routeErrorMessage = null
            )
        }
    }

    // --- 4. ¡NUEVA SECCIÓN DE ACCIONES! (Favoritos) ---

    /**
     * ¡NUEVA FUNCIÓN!
     * Cambia el estado de favorito de una estación.
     * Es llamada desde la UI (ej. StationDetailScreen).
     */
    fun toggleFavorite(station: Station) {
        viewModelScope.launch {
            // Llamamos al repositorio para que actualice la base de datos.
            // El 'Flow' en loadInitialAppData se encargará de
            // actualizar automáticamente la UI (las listas 'allStations' y 'favoriteStations').
            repository.updateFavoriteStatus(station.id, !station.isFavorite)
        }
    }
}

/**
 * Factory (Fábrica) para el ViewModel
 * (Sin cambios)
 */
class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}