package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.LoteModel

interface LoteRepository {
    suspend fun listarLotes(data: String): List<LoteModel>
    suspend fun agregarLoteConBigBags()
}

expect fun getLoteRepository(): LoteRepository