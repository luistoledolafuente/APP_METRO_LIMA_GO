package com.tecsup.metrolimago.data.database // Asegúrate que coincida con tu paquete

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para las Líneas de Transporte
 * (Sin cambios)
 */
@Dao
interface LineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lines: List<TransportLineEntity>)

    @Query("SELECT * FROM transport_lines")
    fun getAllLines(): Flow<List<TransportLineEntity>>

    @Query("SELECT * FROM transport_lines WHERE id = :lineId")
    suspend fun getLineById(lineId: String): TransportLineEntity?
}

/**
 * Data Access Object para las Estaciones
 * (Sin cambios)
 */
@Dao
interface StationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stations: List<StationEntity>)

    @Query("SELECT * FROM stations ORDER BY `order` ASC")
    fun getAllStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE lineId = :lineId ORDER BY `order` ASC")
    fun getStationsByLine(lineId: String): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE lineId = :lineId ORDER BY `order` ASC")
    suspend fun getStationsByLineList(lineId: String): List<StationEntity>

    @Query("SELECT * FROM stations ORDER BY `order` ASC")
    suspend fun getAllStationsList(): List<StationEntity>

    @Query("SELECT * FROM stations WHERE id = :stationId")
    suspend fun getStationById(stationId: String): StationEntity?

    @Query("UPDATE stations SET isFavorite = :isFavorite WHERE id = :stationId")
    suspend fun updateFavoriteStatus(stationId: String, isFavorite: Boolean)

    @Query("SELECT * FROM stations WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteStations(): Flow<List<StationEntity>>
}

// --- ¡NUEVO DAO AÑADIDO! ---
/**
 * Data Access Object para las Rutas Favoritas
 */
@Dao
interface FavoriteRouteDao {
    /**
     * Inserta una nueva ruta favorita.
     * 'OnConflictStrategy.IGNORE' evita que se inserte una ruta duplicada.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(route: FavoriteRouteEntity)

    /**
     * Borra una ruta favorita (basado en el objeto 'route' que nos da la UI).
     */
    @Delete
    suspend fun delete(route: FavoriteRouteEntity)

    /**
     * Obtiene un Flow de todas las rutas favoritas.
     */
    @Query("SELECT * FROM favorite_routes ORDER BY id DESC")
    fun getFavoriteRoutes(): Flow<List<FavoriteRouteEntity>>

    /**
     * (Función futura)
     * Comprueba si una ruta específica ya existe.
     */
    @Query("SELECT * FROM favorite_routes WHERE originStationId = :originId AND destinationStationId = :destinationId LIMIT 1")
    suspend fun findFavoriteRoute(originId: String, destinationId: String): FavoriteRouteEntity?
}