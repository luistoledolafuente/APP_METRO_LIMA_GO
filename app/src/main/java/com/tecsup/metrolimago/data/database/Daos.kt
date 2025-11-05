package com.tecsup.metrolimago.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para las Líneas de Transporte
 */
@Dao
interface LineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lines: List<TransportLineEntity>)

    @Query("SELECT * FROM transport_lines")
    fun getAllLines(): Flow<List<TransportLineEntity>> // Flow para la UI (se actualiza sola)

    @Query("SELECT * FROM transport_lines WHERE id = :lineId")
    suspend fun getLineById(lineId: String): TransportLineEntity?
}

/**
 * Data Access Object para las Estaciones
 */
@Dao
interface StationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stations: List<StationEntity>)

    // Flow para la UI (se actualiza sola)
    @Query("SELECT * FROM stations ORDER BY `order` ASC")
    fun getAllStations(): Flow<List<StationEntity>>

    // Flow para la UI (se actualiza sola)
    @Query("SELECT * FROM stations WHERE lineId = :lineId ORDER BY `order` ASC")
    fun getStationsByLine(lineId: String): Flow<List<StationEntity>>

    // Función síncrona para el calculador de rutas
    @Query("SELECT * FROM stations WHERE lineId = :lineId ORDER BY `order` ASC")
    suspend fun getStationsByLineList(lineId: String): List<StationEntity>

    // Función síncrona para el calculador de rutas
    @Query("SELECT * FROM stations ORDER BY `order` ASC")
    suspend fun getAllStationsList(): List<StationEntity>

    @Query("SELECT * FROM stations WHERE id = :stationId")
    suspend fun getStationById(stationId: String): StationEntity?
}

