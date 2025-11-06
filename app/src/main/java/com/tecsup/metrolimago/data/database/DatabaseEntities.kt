package com.tecsup.metrolimago.data.database // Asegúrate que coincida con tu paquete

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tecsup.metrolimago.ui.theme.Linea1Color

// --- 1. ENTIDADES (Las tablas de la Base de Datos) ---

@Entity(tableName = "transport_lines")
data class TransportLineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val status: String = "Operativo"
)

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val id: String,
    val lineId: String,
    val name: String,
    val order: Int,
    val schedule: String = "05:00 - 22:00",
    val status: String = "OPERATIVE",
    val latitude: Double,
    val longitude: Double,
    // --- ¡CAMBIO AÑADIDO! ---
    // Por defecto, ninguna estación es favorita.
    val isFavorite: Boolean = false
)

// --- 2. MODELOS DE DOMINIO (Los que usa la UI) ---

data class TransportLine(
    val id: String,
    val name: String,
    val color: Color,
    val status: String
)

data class Station(
    val id: String,
    val lineId: String,
    val name: String,
    val order: Int,
    val schedule: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    // --- ¡CAMBIO AÑADIDO! ---
    val isFavorite: Boolean
)

// --- 3. FUNCIONES DE MAPEO ---

fun TransportLineEntity.toDomainModel(): TransportLine {
    return TransportLine(
        id = this.id,
        name = this.name,
        color = Color(android.graphics.Color.parseColor(this.colorHex)),
        status = this.status
    )
}

fun StationEntity.toDomainModel(): Station {
    return Station(
        id = this.id,
        lineId = this.lineId,
        name = this.name,
        order = this.order,
        schedule = this.schedule,
        status = this.status,
        latitude = this.latitude,
        longitude = this.longitude,
        // --- ¡CAMBIO AÑADIDO! ---
        isFavorite = this.isFavorite
    )
}