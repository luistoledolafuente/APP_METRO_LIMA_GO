package com.tecsup.metrolimago.data // Asegúrate que coincida con tu paquete

import com.tecsup.metrolimago.data.database.FavoriteRouteDao // <-- IMPORTAR DAO
import com.tecsup.metrolimago.data.database.FavoriteRouteEntity
import com.tecsup.metrolimago.data.database.FavoriteRoute
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
 * (¡ACTUALIZADO CON FavoriteRouteDao!)
 */
class OfflineRepository(
    private val lineDao: LineDao,
    private val stationDao: StationDao,
    // --- ¡CAMBIO 1: AÑADIR EL NUEVO DAO! ---
    private val favoriteRouteDao: FavoriteRouteDao
) {

    // --- Funciones de Líneas y Estaciones (Sin cambios) ---

    fun getAllLines(): Flow<List<TransportLine>> {
        return lineDao.getAllLines().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    fun getAllStations(): Flow<List<Station>> {
        return stationDao.getAllStations().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    fun getStationsByLine(lineId: String): Flow<List<Station>> {
        return stationDao.getStationsByLine(lineId).map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    fun getFavoriteStations(): Flow<List<Station>> {
        return stationDao.getFavoriteStations().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    suspend fun updateFavoriteStatus(stationId: String, isFavorite: Boolean) {
        stationDao.updateFavoriteStatus(stationId, isFavorite)
    }

    suspend fun getAllStationsList(): List<Station> {
        return stationDao.getAllStationsList().map { it.toDomainModel() }
    }

    suspend fun getStationsByLineList(lineId: String): List<Station> {
        return stationDao.getStationsByLineList(lineId).map { it.toDomainModel() }
    }

    suspend fun getStationById(stationId: String): Station? {
        return stationDao.getStationById(stationId)?.toDomainModel()
    }

    suspend fun getLineById(lineId: String): TransportLine? {
        return lineDao.getLineById(lineId)?.toDomainModel()
    }

    // --- ¡CAMBIO 2: NUEVAS FUNCIONES DE RUTAS FAVORITAS! ---

    /**
     * Obtiene un Flow de todas las rutas favoritas.
     */
    fun getFavoriteRoutes(): Flow<List<FavoriteRoute>> {
        return favoriteRouteDao.getFavoriteRoutes().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    /**
     * Comprueba si una ruta específica ya existe en favoritos.
     */
    suspend fun isRouteFavorite(originId: String, destinationId: String): FavoriteRoute? {
        return favoriteRouteDao.findFavoriteRoute(originId, destinationId)?.toDomainModel()
    }

    /**
     * Añade una nueva ruta favorita a la base de datos.
     */
    suspend fun addFavoriteRoute(origin: Station, destination: Station) {
        val newRoute = FavoriteRouteEntity(
            originStationId = origin.id,
            destinationStationId = destination.id,
            originName = origin.name,
            destinationName = destination.name
        )
        favoriteRouteDao.insert(newRoute)
    }

    /**
     * Borra una ruta favorita de la base de datos.
     */
    suspend fun deleteFavoriteRoute(route: FavoriteRoute) {
        // Tenemos que convertir el Modelo (UI) de vuelta a una Entidad (DB)
        // para que Room pueda borrarlo.
        val routeEntity = FavoriteRouteEntity(
            id = route.id,
            originStationId = route.originStationId,
            destinationStationId = route.destinationStationId,
            originName = route.originName,
            destinationName = route.destinationName
        )
        favoriteRouteDao.delete(routeEntity)
    }
}