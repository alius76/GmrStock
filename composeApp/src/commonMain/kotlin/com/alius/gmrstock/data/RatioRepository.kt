package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Ratio

interface RatioRepository {
    // Trae todos los ratios del mes actual
    suspend fun listarRatiosDelMes(): List<Ratio>
    suspend fun listarRatiosDelDia(): List<Ratio>
    suspend fun listarRatiosDelAno(): List<Ratio>
}

// Función expect para obtener la implementación según plataforma
expect fun getRatioRepository(databaseUrl: String): RatioRepository


