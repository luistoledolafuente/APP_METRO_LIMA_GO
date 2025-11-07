package com.tecsup.metrolimago.logic

import com.tecsup.metrolimago.data.OfflineRepository
import com.tecsup.metrolimago.data.database.Station
import com.tecsup.metrolimago.data.database.TransportLine

// --- Modelos de Datos para el Resultado de la Ruta ---

/**
 * Representa una ruta completa, que puede tener varios segmentos.
 * Ej: [Segmento Línea 1, Segmento Corredor Azul]
 *
 * --- ¡ACTUALIZADO CON COSTO TOTAL! ---
 */
data class RouteResult(
    val segments: List<RouteSegment>,
    val totalTimeEstimate: Int, // en minutos
    val totalCost: Double // ¡NUEVO CAMPO!
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

    // SIMULACIÓN: Definimos conexiones.
    private val transferPoints = mapOf(
        "Ayacucho" to listOf("L1", "CA"),
        "Gamarra" to listOf("L1", "L2"),
        "Miguel Grau" to listOf("L1", "METRO"),
        "Estación Central" to listOf("METRO", "L1")
    )

    // --- ¡NUEVAS FUNCIONES DE CÁLCULO! ---

    /**
     * Devuelve el precio (Double) de la tarifa para una línea específica.
     */
    private fun getFareForLine(lineId: String): Double {
        return when (lineId) {
            "L1" -> 1.50
            "L2" -> 1.50 // Asumimos 1.50
            "CA" -> 2.35
            "METRO" -> 3.20
            else -> 0.0 // Tarifa desconocida
        }
    }

    /**
     * Calcula el costo total de la ruta sumando la tarifa de cada segmento.
     * Asume que cada transbordo (cada nuevo segmento) requiere un nuevo pago.
     */
    private fun calculateTotalCost(segments: List<RouteSegment>): Double {
        return segments.sumOf { segment ->
            getFareForLine(segment.line.id)
        }
    }

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
            val segments = listOf(segment) // Lista de segmentos

            return RouteResult(
                segments = segments,
                totalTimeEstimate = calculateTime(segment.stationsInSegment.size - 1, 0),
                totalCost = calculateTotalCost(segments) // ¡Coste añadido!
            )
        }

        // --- Caso 2: Diferente Línea (Requiere Transbordo) ---
        val originLineStations = repository.getStationsByLineList(origin.lineId)
        val destinationLineStations = repository.getStationsByLineList(destination.lineId)

        val originConnections = originLineStations.filter { station ->
            transferPoints.containsKey(station.name) &&
                    transferPoints[station.name]!!.contains(destination.lineId)
        }

        if (originConnections.isEmpty()) {
            return null
        }

        val transferStationOnOriginLine = originConnections.first()
        val transferStationOnDestinationLine = destinationLineStations.find {
            it.name == transferStationOnOriginLine.name
        } ?: return null

        val segment1 = createSegment(origin, transferStationOnOriginLine) ?: return null
        val segment2 = createSegment(transferStationOnDestinationLine, destination) ?: return null

        val line2 = repository.getLineById(segment2.line.id) ?: return null

        val segment1WithTransferMessage = segment1.copy(
            transferMessage = "Transbordo en ${transferStationOnOriginLine.name} a la ${line2.name}"
        )

        val segments = listOf(segment1WithTransferMessage, segment2)
        val totalTime = calculateTime(
            (segment1.stationsInSegment.size -1) + (segment2.stationsInSegment.size - 1),
            1 // 1 transbordo
        )

        return RouteResult(
            segments = segments,
            totalTimeEstimate = totalTime,
            totalCost = calculateTotalCost(segments) // ¡Coste añadido!
        )
    }

    /**
     * Helper para crear un segmento de ruta (viaje dentro de una misma línea).
     */
    private suspend fun createSegment(start: Station, end: Station): RouteSegment? {
        val line = repository.getLineById(start.lineId) ?: return null
        val lineStations = repository.getStationsByLineList(start.lineId)

        val startIndex = lineStations.indexOfFirst { it.id == start.id }
        val endIndex = lineStations.indexOfFirst { it.id == end.id }

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