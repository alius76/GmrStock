package com.alius.gmrstock.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alius.gmrstock.domain.model.Ratio
import kotlinx.datetime.*

data class RatioData(
    val day: Int,                  // día del mes (1..31)
    var totalWeight: Int           // kilos acumulados ese día
)

fun generateRatioDataFromCollection(ratios: List<Ratio>): List<RatioData> {
    if (ratios.isEmpty()) return emptyList()

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentMonth = now.monthNumber
    val currentYear = now.year

    val dailyMap = mutableMapOf<Int, Int>()

    ratios.forEach { ratio ->
        val date = Instant.fromEpochMilliseconds(ratio.ratioDate)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        if (date.monthNumber == currentMonth && date.year == currentYear) {
            val weight = ratio.ratioTotalWeight.toIntOrNull() ?: 0
            val prev = dailyMap[date.dayOfMonth] ?: 0
            dailyMap[date.dayOfMonth] = prev + weight

            // Print de depuración
            println("📅 Día ${date.dayOfMonth}: agregando $weight kg, total acumulado: ${dailyMap[date.dayOfMonth]}")
        }
    }

    val result = dailyMap.entries
        .sortedBy { it.key }
        .map { RatioData(day = it.key, totalWeight = it.value) }

    // Print final de la lista
    println("📊 Lista final de RatioData:")
    result.forEach { println("Día ${it.day}: ${it.totalWeight} kg") }

    return result
}

// Declaración expect: se implementará en Android e iOS
@Composable
expect fun RatioProductionCard(
    modifier: Modifier = Modifier,
    ratioDataList: List<RatioData>
)
