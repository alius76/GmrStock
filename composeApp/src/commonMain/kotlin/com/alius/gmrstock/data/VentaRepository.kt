package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Venta
import kotlinx.datetime.Instant

interface VentaRepository {
    suspend fun mostrarTodasLasVentas(): List<Venta>
    suspend fun mostrarLasVentasDeHoy(): List<Venta>
    suspend fun mostrarLasUltimasVentas(): List<Venta>
    suspend fun mostrarVentasPorCliente(cliente: String): List<Venta>
    suspend fun mostrarVentasDelMesPorCliente(cliente: String): List<Venta>
    suspend fun mostrarVentasDelMes(): List<Venta>
    suspend fun mostrarVentasDelAno(): List<Venta>

    /**
     * 🔹 Devuelve todas las ventas asociadas a un número de lote.
     */
    suspend fun obtenerVentasPorLote(loteNumber: String): List<Venta>

    /**
     * 🔹 Devuelve la lista de BigBags correspondientes al último registro de venta
     * para un cliente específico dentro de un lote.
     */
    suspend fun obtenerUltimosBigBagsDeCliente(
        loteNumber: String,
        cliente: String
    ): List<BigBags>

    /**
     * 🔹 Devuelve el cliente y la fecha de la venta más reciente
     * de un BigBag dentro de un lote.
     */
    suspend fun obtenerUltimoClienteYFechaDeBigBag(
        loteNumber: String,
        bbNumber: String
    ): Pair<String, Instant>?
}

expect fun getVentaRepository(databaseUrl: String): VentaRepository

