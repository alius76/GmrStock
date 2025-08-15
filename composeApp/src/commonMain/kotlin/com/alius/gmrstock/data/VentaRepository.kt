package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Venta

interface VentaRepository {
    suspend fun mostrarTodasLasVentas(): List<Venta>
    suspend fun mostrarLasVentasDeHoy(): List<Venta>
    suspend fun mostrarLasUltimasVentas(): List<Venta>
    suspend fun mostrarVentasPorCliente(cliente: String): List<Venta>
    suspend fun mostrarVentasDelMesPorCliente(cliente: String): List<Venta>
    suspend fun mostrarVentasDelMes(): List<Venta>
}

expect fun getVentaRepository(databaseUrl: String): VentaRepository