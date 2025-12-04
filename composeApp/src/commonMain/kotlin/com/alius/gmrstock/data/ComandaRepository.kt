package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.domain.model.Cliente
import kotlinx.datetime.Instant

interface ComandaRepository {

    suspend fun listarComandas(filter: String = ""): List<Comanda>

    suspend fun getComandaByNumber(number: String): Comanda?

    suspend fun addComanda(comanda: Comanda): Boolean

    suspend fun updateComandaRemark(comandaId: String, newRemark: String): Boolean

    suspend fun updateComandaBooked(
        comandaId: String,
        cliente: Cliente?,
        dateBooked: Instant?,
        bookedRemark: String? = null
    ): Boolean

    suspend fun updateComandaDate(
        comandaId: String,
        dateBooked: Instant
    ): Boolean

    suspend fun deleteComanda(comandaId: String): Boolean

    // 🆕 1. Consulta de Comandas Pendientes por Cliente
    suspend fun getPendingComandasByClient(clientName: String): List<Comanda>

    // 🆕 2. Asignación del Número de Lote a la Comanda
    suspend fun updateComandaLoteNumber(comandaId: String, loteNumber: String): Boolean
}

// Implementación expect
expect fun getComandaRepository(databaseUrl: String): ComandaRepository