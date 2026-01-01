package com.alius.gmrstock.domain.model

import kotlinx.datetime.Instant

data class TraceEvent(
    val date: Instant?,
    val type: TraceEventType,
    val title: String,
    val subtitle: String,
    val totalWeight: String,
    val bigBags: List<TraceBigBag>,
    val referenceId: String     // El ID del documento original por si necesitamos navegar a él
)

/**
 * Normalización de Big Bags para la vista de trazabilidad.
 */
data class TraceBigBag(
    val number: String,
    val weight: String
)

enum class TraceEventType {
    CREACION,
    VENTA,
    DEVOLUCION,
    REPROCESO
}