package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.MaterialGroup
import kotlin.math.round

fun Double.formatWeight(): String {
    return if (this == this.toInt().toDouble()) {
        this.toInt().toString()
    } else {
        val rounded = kotlin.math.round(this * 100) / 100
        rounded.toString()
    }
}

fun agruparPorMaterial(lotes: List<LoteModel>): List<MaterialGroup> {
    println("🧐 Agrupando ${lotes.size} lotes por descripción")
    val grupos = lotes.groupBy { it.description }
        .map { (descripcion, lotesConDescripcion) ->
            val totalWeightDouble = lotesConDescripcion.sumOf {
                it.totalWeight.replace(",", ".").toDoubleOrNull() ?: 0.0
            }
            val totalWeightStr = totalWeightDouble.formatWeight()

            val totalBigBags = lotesConDescripcion.sumOf {
                it.count.toIntOrNull() ?: 0
            }

            println("📦 Grupo '$descripcion': ${lotesConDescripcion.size} lotes, peso total: $totalWeightStr, totalBigBags: $totalBigBags")

            MaterialGroup(
                description = descripcion,
                totalWeight = totalWeightStr,
                totalLotes = lotesConDescripcion.size,
                totalBigBags = totalBigBags,
                loteNumbers = lotesConDescripcion.map { it.number }
            )
        }
        .sortedBy { it.description }

    println("✅ Se generaron ${grupos.size} grupos de materiales")
    return grupos
}