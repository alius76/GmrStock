package com.alius.gmrstock.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alius.gmrstock.domain.model.Ratio
import kotlinx.datetime.*

data class RatioData(
    val day: Int,                  // día del mes (1..31) o mes (1..12) si es anual
    var totalWeight: Int           // kilos acumulados ese día o mes
)

/** GENERACIÓN DE DATOS DIARIOS */
fun generateRatioDataFromCollection(ratios: List<Ratio>): List<RatioData> {
    if (ratios.isEmpty()) return emptyList()

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentMonth = now.monthNumber
    val currentYear = now.year

    val dailyMap = mutableMapOf<Int, Int>()

    ratios.forEach { ratio ->
        // Validamos que la fecha sea positiva
        if (ratio.ratioDate <= 0L) return@forEach

        val date = try {
            Instant.fromEpochMilliseconds(ratio.ratioDate)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            println("⚠️ Fecha inválida en ratio: ${ratio.ratioDate}")
            return@forEach
        }

        if (date.year == currentYear && date.monthNumber == currentMonth) {
            val weight = ratio.ratioTotalWeight.toIntOrNull() ?: 0
            dailyMap[date.dayOfMonth] = (dailyMap[date.dayOfMonth] ?: 0) + weight
            println("📅 Día ${date.dayOfMonth}: agregando $weight kg, total acumulado: ${dailyMap[date.dayOfMonth]}")
        }
    }

    val result = dailyMap.entries
        .sortedBy { it.key }
        .map { RatioData(day = it.key, totalWeight = it.value) }

    println("📊 Lista final de RatioData diaria (solo días con datos):")
    result.forEach { println("Día ${it.day}: ${it.totalWeight} kg") }

    return result
}

/** GENERACIÓN DE DATOS MENSUALES (ANUAL) */
fun generateRatioDataByMonth(ratios: List<Ratio>): List<RatioData> {
    if (ratios.isEmpty()) return emptyList()

    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val monthlyMap = mutableMapOf<Int, Int>() // key = mes con datos

    ratios.forEach { ratio ->
        if (ratio.ratioDate <= 0L) return@forEach

        val date = try {
            Instant.fromEpochMilliseconds(ratio.ratioDate)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            println("⚠️ Fecha inválida en ratio: ${ratio.ratioDate}")
            return@forEach
        }

        if (date.year == currentYear) {
            val weight = ratio.ratioTotalWeight.toIntOrNull() ?: 0
            monthlyMap[date.monthNumber] = (monthlyMap[date.monthNumber] ?: 0) + weight
            println("📅 Mes ${date.monthNumber}: agregando $weight kg, total acumulado: ${monthlyMap[date.monthNumber]}")
        }
    }

    val result = monthlyMap.entries
        .sortedBy { it.key }
        .map { RatioData(day = it.key, totalWeight = it.value) }

    println("📊 Lista final de RatioData anual (solo meses con datos):")
    result.forEach { println("Mes ${it.day}: ${it.totalWeight} kg") }

    return result
}


// Declaración expect: se implementará en Android e iOS
@Composable
expect fun RatioProductionCard(
    modifier: Modifier = Modifier,
    ratioDataList: List<RatioData>,
    isAnnual: Boolean = false
)
