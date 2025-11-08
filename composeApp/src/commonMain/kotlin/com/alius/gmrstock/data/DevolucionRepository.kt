package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Devolucion

interface DevolucionRepository {
    suspend fun obtenerTodasLasDevoluciones(): List<Devolucion>
    suspend fun agregarDevolucion(devolucion: Devolucion): Boolean
    suspend fun obtenerDevolucionesPorLote(loteNumber: String): List<Devolucion>
}

/**
 * Función de fábrica para obtener una instancia de DevolucionRepository.
 * @param databaseUrl URL base de la base de datos Firestore.
 */
expect fun getDevolucionRepository(databaseUrl: String): DevolucionRepository
