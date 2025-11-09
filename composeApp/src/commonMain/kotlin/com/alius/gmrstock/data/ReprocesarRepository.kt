package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Reprocesar

interface ReprocesarRepository {

    /**
     * Retorna la lista completa de reprocesos registrados
     * en la colección "reprocesar".
     */
    suspend fun listarReprocesos(): List<Reprocesar>

    /**
     * Retorna un reproceso específico filtrado por el número de lote reprocesado.
     * Retorna null si no se encuentra.
     */
    suspend fun getReprocesoByNumber(reprocesoNumber: String): Reprocesar?
}

/**
 * Función multiplataforma para obtener la instancia concreta del repositorio.
 */
expect fun getReprocesarRepository(databaseUrl: String): ReprocesarRepository
