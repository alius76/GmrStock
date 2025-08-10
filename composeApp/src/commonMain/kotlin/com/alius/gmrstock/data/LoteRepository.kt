package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.MaterialGroup

interface LoteRepository {
    suspend fun listarLotes(data: String): List<LoteModel>
    suspend fun agregarLoteConBigBags()
    suspend fun listarGruposPorDescripcion(filter: String = ""): List<MaterialGroup>
    suspend fun getLoteByNumber(number: String): LoteModel?
}

expect fun getLoteRepository(): LoteRepository