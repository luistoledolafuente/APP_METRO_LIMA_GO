package com.tecsup.metrolimago.data

import com.tecsup.metrolimago.data.database.LineDao
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.data.database.StationDao
import com.tecsup.metrolimago.data.database.TransportLine
import com.tecsup.metrolimago.data.database.toDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio que actúa como intermediario entre la Base de Datos (DAOs)
 * y el resto de la aplicación (el ViewModel).
 *
 * Oculta el origen de los datos (en este caso, Room).
 * Transforma los datos de 'Entidad' (DB) a 'Modelo' (UI).
 */
class OfflineRepository(
    private val lineDao: LineDao,
    private val stationDao: StationDao
) {

    // --- Funciones para la UI (usan Flow para actualizarse automáticamente) ---

    /**
     * Obtiene todas las líneas de transporte como un Flow.
     * Mapea el resultado de Entidad (DB) a Modelo (UI).
     */
    fun getAllLines(): Flow<List<TransportLine>> {
        return lineDao.getAllLines().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    /**
     * Obtiene todas las estaciones como un Flow.
     * Mapea el resultado de Entidad (DB) a Modelo (UI).
     */
    fun getAllStations(): Flow<List<Station>> {
        return stationDao.getAllStations().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    /**
     * Obtiene las estaciones de una línea específica como un Flow.
     * Mapea el resultado de Entidad (DB) a Modelo (UI).
     */
    fun getStationsByLine(lineId: String): Flow<List<Station>> {
        return stationDao.getStationsByLine(lineId).map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }


    // --- Funciones para la Lógica (suspend fun, para obtener datos una sola vez) ---

    /**
     * Obtiene una lista (no Flow) de todas las estaciones.
     * Usado por el RouteFinder.
     */
    suspend fun getAllStationsList(): List<Station> {
        return stationDao.getAllStationsList().map { it.toDomainModel() }
    }

    /**
     * Obtiene una lista (no Flow) de las estaciones de una línea.
     * Usado por el RouteFinder.
     */
    suspend fun getStationsByLineList(lineId: String): List<Station> {
        return stationDao.getStationsByLineList(lineId).map { it.toDomainModel() }
    }

    /**
     * Obtiene una sola estación por su ID.
     * Usado por el RouteFinder.
     */
    suspend fun getStationById(stationId: String): Station? {
        return stationDao.getStationById(stationId)?.toDomainModel()
    }

    /**
     * Obtiene una sola línea por su ID.
     * Usado por el RouteFinder.
     */
    suspend fun getLineById(lineId: String): TransportLine? {
        return lineDao.getLineById(lineId)?.toDomainModel()
    }
}

