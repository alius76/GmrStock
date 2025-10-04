package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.LoteModel

interface HistorialRepository {

    /**
     * Trae los lotes de la colección 'historial' que fueron borrados/creados
     * durante el día de hoy (createdAt = hoy).
     */
    suspend fun listarLotesHistorialDeHoy(): List<LoteModel>

}

// Función expect para la inyección de dependencias
expect fun getHistorialRepository(databaseUrl: String): HistorialRepository
