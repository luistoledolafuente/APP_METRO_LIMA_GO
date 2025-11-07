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

// --- ¡NUEVO! Enum para manejar el estado del Tema ---
enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

/**
 * Estado de la UI (User Interface).
 * (¡ACTUALIZADO CON ESTADO DE TEMA!)
 */
data class AppUiState(
    // Listas principales
    val allLines: List<TransportLine> = emptyList(),
    val allStations: List<Station> = emptyList(),
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
    val isAppLoading: Boolean = true,

    // --- ¡NUEVO! Estado para el Tema ---
    // Por defecto, usamos el del Sistema
    val theme: ThemeSetting = ThemeSetting.SYSTEM
)

/**
 * ViewModel principal de la aplicación.
 * (¡ACTUALIZADO CON LÓGICA DE TEMA!)
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

        // (En un futuro, aquí cargaríamos la preferencia de DataStore)
    }

    // --- 2. CARGA DE DATOS ---

    private fun loadInitialAppData() {
        viewModelScope.launch {
            combine(
                repository.getAllLines(),
                repository.getAllStations(),
                repository.getFavoriteStations()
            ) { lines, stations, favorites ->
                Triple(lines, stations, favorites)
            }.collect { (lines, stations, favorites) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        allLines = lines,
                        allStations = stations,
                        favoriteStations = favorites,
                        isAppLoading = false
                    )
                }
            }
        }
    }

    fun loadStationsForLine(lineId: String) {
        _uiState.update { it.copy(isLineDetailLoading = true) }
        viewModelScope.launch {
            val stations = repository.getStationsByLine(lineId).stateIn(viewModelScope).value
            _uiState.update {
                it.copy(
                    selectedLineStations = stations,
                    isLineDetailLoading = false
                )
            }
        }
    }

    fun clearSelectedLine() {
        _uiState.update {
            it.copy(
                selectedLineStations = emptyList(),
                isLineDetailLoading = false
            )
        }
    }


    // --- 3. ACCIONES DE RUTA ---

    fun onOriginStationSelected(station: Station) {
        _uiState.update { it.copy(selectedOrigin = station, calculatedRoute = null, routeErrorMessage = null) }
    }

    fun onDestinationStationSelected(station: Station) {
        _uiState.update { it.copy(selectedDestination = station, calculatedRoute = null, routeErrorMessage = null) }
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
            val route = routeFinder.findRoute(origin.id, destination.id)
            if (route != null) {
                _uiState.update { it.copy(calculatedRoute = route, routeErrorMessage = null) }
            } else {
                _uiState.update { it.copy(routeErrorMessage = "No se encontró una ruta.", calculatedRoute = null) }
            }
        }
    }

    fun clearRouteSearch() {
        _uiState.update { it.copy(selectedOrigin = null, selectedDestination = null, calculatedRoute = null, routeErrorMessage = null) }
    }

    // --- 4. ACCIONES DE FAVORITOS ---

    fun toggleFavorite(station: Station) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(station.id, !station.isFavorite)
        }
    }

    // --- 5. ¡NUEVA ACCIÓN DE TEMA! ---

    /**
     * Actualiza la preferencia de tema de la app.
     */
    fun setTheme(theme: ThemeSetting) {
        _uiState.update { it.copy(theme = theme) }
    }
}

/**
 * Factory (Fábrica) para el ViewModel
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