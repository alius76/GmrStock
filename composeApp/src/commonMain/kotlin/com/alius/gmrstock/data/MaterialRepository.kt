package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Material

interface MaterialRepository {
    suspend fun getAllMaterialsOrderedByName(): List<Material>
}

// ✅ expect unificado
expect fun getMaterialRepository(databaseUrl: String): MaterialRepository

