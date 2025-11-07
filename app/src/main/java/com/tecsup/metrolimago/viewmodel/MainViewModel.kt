package com.tecsup.metrolimago.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.metrolimago.data.OfflineRepository
import com.tecsup.metrolimago.data.database.AppDatabase
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.data.database.TransportLine
import com.tecsup.metrolimago.data.database.FavoriteStationEntity
import com.tecsup.metrolimago.data.database.FavoriteRouteEntity // <--- AGREGA ESTA
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

data class AppUiState(
    val allLines: List<TransportLine> = emptyList(),
    val allStations: List<Station> = emptyList(),
    val selectedLineStations: List<Station> = emptyList(),
    val isLineDetailLoading: Boolean = false,
    val selectedOrigin: Station? = null,
    val selectedDestination: Station? = null,
    val calculatedRoute: RouteResult? = null,
    val routeErrorMessage: String? = null,
    val isAppLoading: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository: OfflineRepository = OfflineRepository(database.lineDao(), database.stationDao())
    private val routeFinder: RouteFinder = RouteFinder(repository)
    private val favoriteStationDao = database.favoriteStationDao()
    private val favoriteRouteDao = database.favoriteRouteDao() // <--- AGREGADO

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // FAVORITOS - estaciones
    private val _favoriteStations = MutableStateFlow<List<String>>(emptyList())
    val favoriteStations: StateFlow<List<String>> = _favoriteStations

    // FAVORITOS - rutas
    private val _favoriteRoutes = MutableStateFlow<List<FavoriteRouteEntity>>(emptyList())
    val favoriteRoutes: StateFlow<List<FavoriteRouteEntity>> = _favoriteRoutes

    init {
        loadInitialAppData()
        loadFavorites()
        loadFavoriteRoutes()
    }

    // --- 2. CARGA DE DATOS ---
    private fun loadInitialAppData() {
        viewModelScope.launch {
            combine(
                repository.getAllLines(),
                repository.getAllStations()
            ) { lines, stations ->
                Pair(lines, stations)
            }.collect { (lines, stations) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        allLines = lines,
                        allStations = stations,
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

    // --- FAVORITOS ESTACIONES ---
    fun loadFavorites() {
        viewModelScope.launch {
            _favoriteStations.value = favoriteStationDao.getFavorites().map { it.stationId }
        }
    }

    fun addFavorite(stationId: String) {
        viewModelScope.launch {
            favoriteStationDao.insertFavorite(FavoriteStationEntity(stationId))
            loadFavorites()
        }
    }

    fun deleteFavorite(stationId: String) {
        viewModelScope.launch {
            favoriteStationDao.deleteFavorite(stationId)
            loadFavorites()
        }
    }

    // --- FAVORITOS RUTAS ---
    fun loadFavoriteRoutes() {
        viewModelScope.launch {
            _favoriteRoutes.value = favoriteRouteDao.getAll()
        }
    }

    fun addFavoriteRoute(origin: String, destination: String) {
        viewModelScope.launch {
            favoriteRouteDao.insert(FavoriteRouteEntity(origin = origin, destination = destination))
            loadFavoriteRoutes()
        }
    }

    fun removeFavoriteRoute(route: FavoriteRouteEntity) {
        viewModelScope.launch {
            favoriteRouteDao.delete(route)
            loadFavoriteRoutes()
        }
    }

    // --- 3. ACCIONES DE LA UI (Planificador de Ruta) ---
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
                    _uiState.update { it.copy(calculatedRoute = route, routeErrorMessage = null) }
                } else {
                    _uiState.update { it.copy(routeErrorMessage = "No se encontró una ruta directa o con 1 transbordo.", calculatedRoute = null) }
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
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
