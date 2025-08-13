package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Venta

interface VentaRepository {
    suspend fun mostrarTodasLasVentas(): List<Venta>
}

expect fun getVentaRepository(databaseUrl: String): VentaRepository