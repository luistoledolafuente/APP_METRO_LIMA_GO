package com.tecsup.metrolimago.data.database // Asegúrate que coincida con tu paquete

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.ForeignKey // <-- IMPORTANTE
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
    val isFavorite: Boolean = false
)

// --- ¡NUEVA TABLA AÑADIDA! (Req 6.2) ---
@Entity(
    tableName = "favorite_routes",
    // Definimos las "claves foráneas" para que, si se borra una estación,
    // la ruta favorita asociada también se borre.
    foreignKeys = [
        ForeignKey(
            entity = StationEntity::class,
            parentColumns = ["id"],
            childColumns = ["originStationId"],
            onDelete = ForeignKey.CASCADE // Si se borra la estación, se borra la ruta
        ),
        ForeignKey(
            entity = StationEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinationStationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FavoriteRouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originStationId: String,
    val destinationStationId: String,
    // Guardamos los nombres para mostrarlos rápido en la lista
    val originName: String,
    val destinationName: String
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
    val isFavorite: Boolean
)

// --- ¡NUEVO MODELO AÑADIDO! ---
data class FavoriteRoute(
    val id: Int,
    val originStationId: String,
    val destinationStationId: String,
    val originName: String,
    val destinationName: String
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
        isFavorite = this.isFavorite
    )
}

// --- ¡NUEVO MAPPER AÑADIDO! ---
fun FavoriteRouteEntity.toDomainModel(): FavoriteRoute {
    return FavoriteRoute(
        id = this.id,
        originStationId = this.originStationId,
        destinationStationId = this.destinationStationId,
        originName = this.originName,
        destinationName = this.destinationName
    )
}