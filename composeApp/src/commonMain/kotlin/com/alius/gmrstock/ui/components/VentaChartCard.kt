package com.alius.gmrstock.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.toLocalDateTime

/**
 * Espera la implementación de VentaChartCard en cada plataforma.
 */
@Composable
expect fun VentaChartCard(
    modifier: Modifier = Modifier,
    ventaDataList: List<VentaData>
)

/**
 * Modelo de datos para graficar ventas diarias.
 */
data class VentaData(
    val day: Int,        // día del mes (1..31)
    var totalWeight: Int // kilos acumulados ese día
)

/**
 * Genera la lista de VentaData a partir de un listado de ventas,
 * solo considerando el mes actual.
 */
fun generateVentaDataFromCollection(ventas: List<com.alius.gmrstock.domain.model.Venta>): List<VentaData> {
    if (ventas.isEmpty()) return emptyList()

    val now = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val currentMonth = now.monthNumber
    val currentYear = now.year

    val dailyMap = mutableMapOf<Int, Int>()

    ventas.forEach { venta ->
        val date = venta.ventaFecha?.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()) ?: return@forEach
        if (date.monthNumber == currentMonth && date.year == currentYear) {
            val weight = venta.ventaPesoTotal?.toIntOrNull() ?: 0
            val prev = dailyMap[date.dayOfMonth] ?: 0
            dailyMap[date.dayOfMonth] = prev + weight
        }
    }

    return dailyMap.entries
        .sortedBy { it.key }
        .map { VentaData(day = it.key, totalWeight = it.value) }
}
