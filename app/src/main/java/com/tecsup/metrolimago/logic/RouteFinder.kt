package com.tecsup.metrolimago.logic

import com.tecsup.metrolimago.data.OfflineRepository
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.data.database.TransportLine

// --- Modelos de Datos para el Resultado de la Ruta ---

/**
 * Representa una ruta completa, que puede tener varios segmentos.
 * Ej: [Segmento Línea 1, Segmento Corredor Azul]
 */
data class RouteResult(
    val segments: List<RouteSegment>,
    val totalTimeEstimate: Int // en minutos
)

/**
 * Un solo tramo de la ruta en una misma línea.
 */
data class RouteSegment(
    val line: TransportLine,
    val startStation: Station,
    val endStation: Station,
    val stationsInSegment: List<Station>, // Lista de paradas en este tramo
    val transferMessage: String? = null // Mensaje (ej: "Transbordo en Gamarra")
)

/**
 * Este buscador es el núcleo de la Req 3.
 * Utiliza el repositorio para obtener los datos y calcular la ruta.
 */
class RouteFinder(private val repository: OfflineRepository) {

    // SIMULACIÓN: Definimos conexiones. En un sistema real, esto vendría de la DB.
    // Usamos el NOMBRE de la estación para la conexión.
    // OJO: Los nombres deben coincidir EXACTAMENTE con los de AppDatabase.kt
    private val transferPoints = mapOf(
        "Ayacucho" to listOf("L1", "CA"),      // Conexión Línea 1 y Corredor Azul
        "Gamarra" to listOf("L1", "L2"),     // Conexión Línea 1 y Línea 2

        // --- ¡NUEVA CONEXIÓN! ---
        // (La Estación Central del Metro se conecta con la Estación Miguel Grau de L1)
        // (Es una simulación, en la vida real es Grau)
        "Miguel Grau" to listOf("L1", "METRO"),
        "Estación Central" to listOf("METRO", "L1")
    )

    // Simplificación: 3 minutos por estación, 10 minutos por transbordo
    private fun calculateTime(stations: Int, transfers: Int): Int {
        return (stations * 3) + (transfers * 10)
    }

    /**
     * Encuentra la ruta óptima (simplificada) entre dos estaciones.
     */
    suspend fun findRoute(originStationId: String, destinationStationId: String): RouteResult? {
        val origin = repository.getStationById(originStationId) ?: return null
        val destination = repository.getStationById(destinationStationId) ?: return null

        // --- Caso 1: Misma Línea (El más fácil) ---
        if (origin.lineId == destination.lineId) {
            val segment = createSegment(origin, destination) ?: return null
            return RouteResult(
                segments = listOf(segment),
                totalTimeEstimate = calculateTime(segment.stationsInSegment.size - 1, 0)
            )
        }

        // --- Caso 2: Diferente Línea (Requiere Transbordo) ---
        // Algoritmo simplificado: buscar un solo transbordo.

        val originLineStations = repository.getStationsByLineList(origin.lineId)
        val destinationLineStations = repository.getStationsByLineList(destination.lineId)

        // Encontrar puntos de conexión en la línea de ORIGEN que conecten con la línea de DESTINO
        val originConnections = originLineStations.filter { station ->
            transferPoints.containsKey(station.name) &&
                    transferPoints[station.name]!!.contains(destination.lineId)
        }

        if (originConnections.isEmpty()) {
            // No se encontró ruta con 1 transbordo
            return null
        }

        // Por ahora, tomamos la primera conexión encontrada (simplificación)
        val transferStationOnOriginLine = originConnections.first()

        // Buscar la estación correspondiente en la línea de DESTINO
        // (La estación con el mismo nombre en la otra línea)
        val transferStationOnDestinationLine = destinationLineStations.find {
            it.name == transferStationOnOriginLine.name
        } ?: return null // Error de datos, la conexión no existe en la otra línea

        // Crear los dos segmentos de la ruta
        val segment1 = createSegment(origin, transferStationOnOriginLine) ?: return null
        val segment2 = createSegment(transferStationOnDestinationLine, destination) ?: return null

        val line2 = repository.getLineById(segment2.line.id) ?: return null

        // Agregar mensaje de transbordo al primer segmento
        val segment1WithTransferMessage = segment1.copy(
            transferMessage = "Transbordo en ${transferStationOnOriginLine.name} a la ${line2.name}"
        )

        val segments = listOf(segment1WithTransferMessage, segment2)

        // Calculamos tiempo total (estaciones en seg 1 + estaciones in seg 2 + 1 transbordo)
        val totalTime = calculateTime(
            (segment1.stationsInSegment.size -1) + (segment2.stationsInSegment.size - 1),
            1 // 1 transbordo
        )

        return RouteResult(segments = segments, totalTimeEstimate = totalTime)
    }

    /**
     * Helper para crear un segmento de ruta (viaje dentro de una misma línea).
     */
    private suspend fun createSegment(start: Station, end: Station): RouteSegment? {
        val line = repository.getLineById(start.lineId) ?: return null
        val lineStations = repository.getStationsByLineList(start.lineId)

        val startIndex = lineStations.indexOfFirst { it.id == start.id }
        val endIndex = lineStations.indexOfFirst { it.id == end.id }

        // Maneja si el viaje es "hacia adelante" (index 1 -> 5)
        // o "hacia atrás" (index 5 -> 1)
        val stationsInSegment = if (startIndex < endIndex) {
            lineStations.subList(startIndex, endIndex + 1)
        } else {
            lineStations.subList(endIndex, startIndex + 1).reversed()
        }

        return RouteSegment(
            line = line,
            startStation = start,
            endStation = end,
            stationsInSegment = stationsInSegment
        )
    }
}