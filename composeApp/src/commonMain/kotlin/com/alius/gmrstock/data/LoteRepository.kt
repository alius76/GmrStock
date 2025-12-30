package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.MaterialGroup
import com.alius.gmrstock.domain.model.Vertisol
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate


interface LoteRepository {

    suspend fun listarLotes(data: String): List<LoteModel>
    suspend fun getLoteById(id: String): LoteModel?

    suspend fun listarGruposPorDescripcion(filter: String = ""): List<MaterialGroup>
    suspend fun getLoteByNumber(number: String): LoteModel?

    // 🔹 Consultas de lotes
    suspend fun listarLotesCreadosHoy(): List<LoteModel>
    suspend fun listarLotesPorFecha(fecha: LocalDate): List<LoteModel>
    suspend fun listarUltimosLotes(cantidad: Int): List<LoteModel>

    // 🔹 Actualizaciones
    suspend fun updateLoteRemark(loteId: String, newRemark: String): Boolean

    suspend fun updateLoteBooked(
        loteId: String,
        cliente: Cliente?,
        dateBooked: Instant?,
        bookedByUser: String? = null,
        bookedRemark: String? = null

    ): Boolean

    suspend fun listarLotesVertisol(): List<Vertisol>
    suspend fun listarLotesReservados(orderBy: String = "booked", direction: String = "ASCENDING"): List<LoteModel>
    suspend fun listarLotesPorDescripcion(descripcion: String): List<LoteModel>
}

expect fun getLoteRepository(databaseUrl: String): LoteRepository


