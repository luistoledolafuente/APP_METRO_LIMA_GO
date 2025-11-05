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
 * Contiene todos los datos que las pantallas necesitan para dibujarse.
 */
data class AppUiState(
    // Listas principales (cargadas al inicio)
    val allLines: List<TransportLine> = emptyList(),
    val allStations: List<Station> = emptyList(),

    // Estado para el Detalle de Línea
    val selectedLineStations: List<Station> = emptyList(),
    val isLineDetailLoading: Boolean = false,

    // Estado para el Planificador de Ruta
    val selectedOrigin: Station? = null,
    val selectedDestination: Station? = null,
    val calculatedRoute: RouteResult? = null,
    val routeErrorMessage: String? = null,

    // Estado de Carga General
    val isAppLoading: Boolean = true // Empezamos en 'cargando'
)

/**
 * ViewModel principal de la aplicación.
 * CORREGIDO para evitar bucles de recarga (flickering).
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // --- 1. INICIALIZACIÓN ---
    private val repository: OfflineRepository
    private val routeFinder: RouteFinder

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        // Obtenemos la instancia de la base de datos
        val database = AppDatabase.getDatabase(application)

        repository = OfflineRepository(database.lineDao(), database.stationDao())
        routeFinder = RouteFinder(repository)

        // Cargamos los datos iniciales (las listas de líneas y estaciones)
        loadInitialAppData()
    }

    // --- 2. CARGA DE DATOS ---

    /**
     * Carga las listas de líneas y estaciones (datos globales)
     * una sola vez cuando se inicia el ViewModel.
     */
    private fun loadInitialAppData() {
        viewModelScope.launch {
            // 'combine' nos permite "escuchar" a varios Flujos (Flows) a la vez
            combine(
                repository.getAllLines(),
                repository.getAllStations()
            ) { lines, stations ->
                // Creamos un estado temporal solo con estos datos
                Pair(lines, stations)
            }.collect { (lines, stations) ->
                // Actualizamos el estado de la UI con los nuevos datos
                _uiState.update { currentState ->
                    currentState.copy(
                        allLines = lines,
                        allStations = stations,
                        isAppLoading = false // ¡Ya cargamos!
                    )
                }
            }
        }
    }

    /**
     * ¡NUEVA FUNCIÓN!
     * Carga las estaciones para una línea específica.
     * Esto ahora se llama DESDE la pantalla (LineDetailScreen).
     */
    fun loadStationsForLine(lineId: String) {
        // 1. Ponemos el estado en "Cargando"
        _uiState.update { it.copy(isLineDetailLoading = true) }

        viewModelScope.launch {
            // 2. Usamos el Flow, pero solo tomamos el *primer* valor (la lista actual)
            val stations = repository.getStationsByLine(lineId).stateIn(viewModelScope).value

            // 3. Actualizamos el estado con la lista y quitamos "Cargando"
            _uiState.update {
                it.copy(
                    selectedLineStations = stations,
                    isLineDetailLoading = false
                )
            }
        }
    }

    /**
     * ¡NUEVA FUNCIÓN!
     * Limpia la lista de estaciones cuando salimos de la pantalla de detalle.
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

    /**
     * ¡NUEVA FUNCIÓN!
     * Limpia la selección de ruta al salir.
     */
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

    // Esta función ya no es necesaria, la lógica se movió a 'loadStationsForLine'
    /*
    fun getStationsForLine(lineId: String): StateFlow<List<Station>> {
        return repository.getStationsByLine(lineId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }
    */
}

/**
 * Factory (Fábrica) para poder crear el MainViewModel pasándole el 'Application'
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