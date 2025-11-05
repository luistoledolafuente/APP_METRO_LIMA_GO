package com.tecsup.metrolimago.data.database

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tecsup.metrolimago.ui.theme.Linea1Color

// --- 1. ENTIDADES (Las tablas de la Base de Datos) ---

@Entity(tableName = "transport_lines")
data class TransportLineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String, // Guardamos el color como texto (ej: "#D32F2F")
    val status: String = "Operativo"
)

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val id: String,
    val lineId: String, // Clave foránea para TransportLineEntity
    val name: String,
    val order: Int, // Posición en la línea (0, 1, 2...)
    val schedule: String = "05:00 - 22:00",
    val status: String = "OPERATIVE",
    val latitude: Double,
    val longitude: Double
)


// --- 2. MODELOS DE DOMINIO (Los que usa la UI) ---
// Estos son los objetos que nuestras pantallas usarán.

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
    val longitude: Double
)

// --- 3. FUNCIONES DE MAPEO ---
// Convierten los datos de la DB (Entity) a los datos de la UI (Model)

fun TransportLineEntity.toDomainModel(): TransportLine {
    return TransportLine(
        id = this.id,
        name = this.name,
        // Convertimos el String Hex a un objeto Color
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
        longitude = this.longitude
    )
}

