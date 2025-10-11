package com.alius.gmrstock.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alius.gmrstock.domain.model.Venta
import kotlinx.datetime.*

data class VentaData(
    val day: Int,                  // día del mes (1..31) o mes (1..12) si es anual
    var totalWeight: Int           // kilos acumulados ese día o mes
)

/** GENERACIÓN DE DATOS DIARIOS */
fun generateVentaDataFromCollection(ventas: List<Venta>): List<VentaData> {
    if (ventas.isEmpty()) return emptyList()

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentMonth = now.monthNumber
    val currentYear = now.year

    val dailyMap = mutableMapOf<Int, Int>()

    ventas.forEach { venta ->
        val date = venta.ventaFecha?.toLocalDateTime(TimeZone.currentSystemDefault()) ?: return@forEach
        if (date.year == currentYear && date.monthNumber == currentMonth) {
            val weight = venta.ventaPesoTotal?.toIntOrNull() ?: 0
            dailyMap[date.dayOfMonth] = (dailyMap[date.dayOfMonth] ?: 0) + weight
            println("📅 Día ${date.dayOfMonth}: agregando $weight kg, total acumulado: ${dailyMap[date.dayOfMonth]}")
        }
    }

    // Solo devolver los días con datos
    val result = dailyMap.entries
        .sortedBy { it.key }
        .map { VentaData(day = it.key, totalWeight = it.value) }

    println("📊 Lista final de VentaData diaria (solo días con datos):")
    result.forEach { println("Día ${it.day}: ${it.totalWeight} kg") }

    return result
}

/** GENERACIÓN DE DATOS MENSUALES (ANUAL) */
fun generateVentaDataByMonth(ventas: List<Venta>): List<VentaData> {
    if (ventas.isEmpty()) return emptyList()

    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val monthlyMap = mutableMapOf<Int, Int>() // key = mes con datos

    ventas.forEach { venta ->
        val date = venta.ventaFecha?.toLocalDateTime(TimeZone.currentSystemDefault()) ?: return@forEach
        if (date.year == currentYear) {
            val weight = venta.ventaPesoTotal?.toIntOrNull() ?: 0
            monthlyMap[date.monthNumber] = (monthlyMap[date.monthNumber] ?: 0) + weight
            println("📅 Mes ${date.monthNumber}: agregando $weight kg, total acumulado: ${monthlyMap[date.monthNumber]}")
        }
    }

    // Solo devolver los meses con datos
    val result = monthlyMap.entries
        .sortedBy { it.key }
        .map { VentaData(day = it.key, totalWeight = it.value) }

    println("📊 Lista final de VentaData anual (solo meses con datos):")
    result.forEach { println("Mes ${it.day}: ${it.totalWeight} kg") }

    return result
}

// Declaración expect: se implementará en Android e iOS
@Composable
expect fun VentaChartCard(
    modifier: Modifier = Modifier,
    ventaDataList: List<VentaData>,
    isAnnual: Boolean = false
)
