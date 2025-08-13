package com.alius.gmrstock.data.firebase

import com.alius.gmrstock.domain.model.Venta

interface VentaRepository {
    suspend fun mostrarTodasLasVentas(): List<Venta>
}


expect fun getVentaRepository(config: FirebaseDbConfig): VentaRepository