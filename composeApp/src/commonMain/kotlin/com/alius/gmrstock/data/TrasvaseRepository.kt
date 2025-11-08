package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Trasvase
import com.alius.gmrstock.domain.model.TrasvaseBigBag

interface TrasvaseRepository {

    /**
     * Retorna el objeto Trasvase completo para un número de lote.
     * Retorna null si no se encuentra.
     */
    suspend fun getTrasvaseByLote(trasvaseNumber: String): Trasvase?

    /**
     * Retorna solo la lista de TrasvaseBigBag asociada a un número de lote.
     * Retorna lista vacía si no se encuentra.
     */
    suspend fun getTrasvaseBigBagsByLote(trasvaseNumber: String): List<TrasvaseBigBag>

    /**
     * 🔹 Nueva función
     * Retorna todos los trasvases asociados a un número de lote.
     * Retorna lista vacía si no se encuentra.
     */
    suspend fun getTrasvasesByLote(trasvaseNumber: String): List<Trasvase>
}

/**
 * Función multiplataforma para obtener una instancia de TrasvaseRepository.
 */
expect fun getTrasvaseRepository(databaseUrl: String): TrasvaseRepository




