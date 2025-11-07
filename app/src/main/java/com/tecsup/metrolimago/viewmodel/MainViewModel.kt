package com.tecsup.metrolimago.viewmodel // Asegúrate que coincida con tu paquete

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.metrolimago.data.OfflineRepository
import com.tecsup.metrolimago.data.database.AppDatabase
import com.tecsup.metrolimago.data.database.FavoriteRoute
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.data.database.TransportLine
import com.tecsup.metrolimago.logic.RouteFinder
import com.tecsup.metrolimago.logic.RouteResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Enum para el Tema
enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

// --- ¡NUEVO! Enum para el Idioma ---
enum class LanguageSetting(val code: String) {
    SPANISH("es"),
    ENGLISH("en")
}

/**
 * Estado de la UI
 * (¡ACTUALIZADO CON IDIOMA!)
 */
data class AppUiState(
    // Listas principales
    val allLines: List<TransportLine> = emptyList(),
    val allStations: List<Station> = emptyList(),
    val favoriteStations: List<Station> = emptyList(),
    val favoriteRoutes: List<FavoriteRoute> = emptyList(),
    val isCurrentRouteFavorite: FavoriteRoute? = null,

    // Estados de pantalla
    val selectedLineStations: List<Station> = emptyList(),
    val isLineDetailLoading: Boolean = false,
    val selectedOrigin: Station? = null,
    val selectedDestination: Station? = null,
    val calculatedRoute: RouteResult? = null,
    val routeErrorMessage: String? = null,

    // Estados globales
    val isAppLoading: Boolean = true,
    val theme: ThemeSetting = ThemeSetting.SYSTEM,

    // --- ¡NUEVO! Estado para el Idioma ---
    val language: LanguageSetting = LanguageSetting.SPANISH // Por defecto
)

/**
 * ViewModel principal
 * (¡ACTUALIZADO CON LÓGICA DE IDIOMA!)
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OfflineRepository
    private val routeFinder: RouteFinder

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = OfflineRepository(
            database.lineDao(),
            database.stationDao(),
            database.favoriteRouteDao()
        )
        routeFinder = RouteFinder(repository)
        loadInitialAppData()

        // (En un futuro, aquí cargaríamos el idioma y tema guardados en DataStore)
    }

    // --- CARGA DE DATOS ---
    private fun loadInitialAppData() {
        viewModelScope.launch {
            combine(
                repository.getAllLines(),
                repository.getAllStations(),
                repository.getFavoriteStations(),
                repository.getFavoriteRoutes()
            ) { lines, stations, favStations, favRoutes ->
                Quadruple(lines, stations, favStations, favRoutes)
            }.collect { (lines, stations, favStations, favRoutes) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        allLines = lines,
                        allStations = stations,
                        favoriteStations = favStations,
                        favoriteRoutes = favRoutes,
                        isAppLoading = false
                    )
                }
            }
        }
    }

    // --- (Lógica de Líneas, Rutas, Favoritos, etc. SIN CAMBIOS) ---

    fun loadStationsForLine(lineId: String) {
        _uiState.update { it.copy(isLineDetailLoading = true) }
        viewModelScope.launch {
            val stations = repository.getStationsByLine(lineId).stateIn(viewModelScope).value
            _uiState.update {
                it.copy(selectedLineStations = stations, isLineDetailLoading = false)
            }
        }
    }

    fun clearSelectedLine() {
        _uiState.update {
            it.copy(selectedLineStations = emptyList(), isLineDetailLoading = false)
        }
    }

    fun onOriginStationSelected(station: Station) {
        _uiState.update { it.copy(selectedOrigin = station, calculatedRoute = null, routeErrorMessage = null, isCurrentRouteFavorite = null) }
    }

    fun onDestinationStationSelected(station: Station) {
        _uiState.update { it.copy(selectedDestination = station, calculatedRoute = null, routeErrorMessage = null, isCurrentRouteFavorite = null) }
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
                _uiState.update {
                    it.copy(calculatedRoute = route, routeErrorMessage = null)
                }
                checkIfCurrentRouteIsFavorite(origin.id, destination.id)
            } else {
                _uiState.update { it.copy(routeErrorMessage = "No se encontró una ruta.", calculatedRoute = null) }
            }
        }
    }

    fun clearRouteSearch() {
        _uiState.update { it.copy(selectedOrigin = null, selectedDestination = null, calculatedRoute = null, routeErrorMessage = null, isCurrentRouteFavorite = null) }
    }

    fun toggleFavorite(station: Station) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(station.id, !station.isFavorite)
        }
    }

    private fun checkIfCurrentRouteIsFavorite(originId: String, destinationId: String) {
        viewModelScope.launch {
            val favoriteRoute = repository.isRouteFavorite(originId, destinationId)
            _uiState.update { it.copy(isCurrentRouteFavorite = favoriteRoute) }
        }
    }

    fun toggleCurrentRouteFavorite() {
        val origin = _uiState.value.selectedOrigin
        val destination = _uiState.value.selectedDestination
        val existingFavorite = _uiState.value.isCurrentRouteFavorite

        if (origin != null && destination != null) {
            viewModelScope.launch {
                if (existingFavorite == null) {
                    repository.addFavoriteRoute(origin, destination)
                } else {
                    repository.deleteFavoriteRoute(existingFavorite)
                }
                checkIfCurrentRouteIsFavorite(origin.id, destination.id)
            }
        }
    }

    fun deleteFavoriteRoute(route: FavoriteRoute) {
        viewModelScope.launch {
            repository.deleteFavoriteRoute(route)
        }
    }

    fun selectFavoriteRoute(route: FavoriteRoute) {
        viewModelScope.launch {
            val origin = repository.getStationById(route.originStationId)
            val destination = repository.getStationById(route.destinationStationId)

            if (origin != null && destination != null) {
                _uiState.update {
                    it.copy(
                        selectedOrigin = origin,
                        selectedDestination = destination,
                        calculatedRoute = null,
                        routeErrorMessage = null
                    )
                }
                calculateRoute()
            }
        }
    }

    fun setTheme(theme: ThemeSetting) {
        _uiState.update { it.copy(theme = theme) }
    }

    // --- ¡NUEVA ACCIÓN DE IDIOMA! ---

    /**
     * Actualiza la preferencia de idioma de la app.
     */
    fun setLanguage(language: LanguageSetting) {
        _uiState.update { it.copy(language = language) }
    }
}

// (La Factory y el Quadruple se quedan igual)
class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
private data class Quadruple<T1, T2, T3, T4>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4
)