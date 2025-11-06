package com.tecsup.metrolimago.data.database // Asegúrate que coincida con tu paquete

import androidx.room.Dao
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
 * (¡ACTUALIZADO CON FUNCIONES DE FAVORITOS!)
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

    // --- ¡NUEVA FUNCIÓN AÑADIDA! ---
    /**
     * Actualiza el estado de 'favorito' de una estación.
     */
    @Query("UPDATE stations SET isFavorite = :isFavorite WHERE id = :stationId")
    suspend fun updateFavoriteStatus(stationId: String, isFavorite: Boolean)

    // --- ¡NUEVA FUNCIÓN AÑADIDA! ---
    /**
     * Obtiene solo las estaciones marcadas como favoritas.
     */
    @Query("SELECT * FROM stations WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteStations(): Flow<List<StationEntity>>
}