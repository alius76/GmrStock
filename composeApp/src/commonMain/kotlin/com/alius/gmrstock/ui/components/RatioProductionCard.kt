package com.alius.gmrstock.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Data class común
data class RatioData(
    val listRatioDate: Long,       // timestamp epoch millis
    val listRatioTotalWeight: Int  // kilos producidos
)

// Función multiplataforma para generar 31 días de agosto
fun generateRatioData(): List<RatioData> {
    val data = mutableListOf<RatioData>()

    // 1 de agosto de 2025 a medianoche en UTC
    val baseTime = 1754073600000L // epoch millis de 2025-08-01 00:00:00 UTC

    for (i in 0 until 31) {
        val timestamp = baseTime + i * 24 * 60 * 60 * 1000L // sumar días en millis
        val weight = (500..15000).random()
        data.add(RatioData(timestamp, weight))
    }

    return data
}

// Declaración expect: se implementará en Android e iOS
@Composable
expect fun RatioProductionCard(modifier: Modifier = Modifier)
