package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.buildQueryUltimasVentas
import com.alius.gmrstock.data.firestore.buildQueryVentasDeHoy
import com.alius.gmrstock.data.firestore.buildQueryVentasPorClienteYFecha
import com.alius.gmrstock.data.mappers.VentaMapper
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Venta
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.days

class VentaRepositoryImpl(
    private val client: HttpClient,
    private val databaseUrl: String
) : VentaRepository {

    // -----------------------------------------------------
    // 🔹 Métodos existentes (ventas generales)
    // -----------------------------------------------------
    override suspend fun mostrarTodasLasVentas(): List<Venta> =
        obtenerVentasFiltradas()

    override suspend fun mostrarLasVentasDeHoy(): List<Venta> {
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val inicioDeHoy = hoy.atStartOfDayIn(TimeZone.currentSystemDefault())
        val inicioDeManana = inicioDeHoy.plus(1.days)
        return obtenerVentasFiltradas(inicio = inicioDeHoy, fin = inicioDeManana)
    }

    override suspend fun mostrarLasUltimasVentas(): List<Venta> =
        obtenerUltimasVentas(5)

    override suspend fun mostrarVentasPorCliente(cliente: String): List<Venta> {
        val inicio = Instant.parse("2025-01-01T00:00:00Z")
        val fin = Clock.System.now()
        return obtenerVentasFiltradas(cliente = cliente, inicio = inicio, fin = fin)
    }

    override suspend fun mostrarVentasDelMesPorCliente(cliente: String): List<Venta> {
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val inicioDelMes = LocalDateTime(ahora.year, ahora.monthNumber, 1, 0, 0)
            .toInstant(TimeZone.currentSystemDefault())
        val fin = Clock.System.now()
        return obtenerVentasFiltradas(cliente = cliente, inicio = inicioDelMes, fin = fin)
    }

    override suspend fun mostrarVentasDelMes(): List<Venta> {
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val inicioDelMes = LocalDateTime(ahora.year, ahora.monthNumber, 1, 0, 0)
            .toInstant(TimeZone.currentSystemDefault())
        val fin = Clock.System.now()
        return obtenerVentasFiltradas(inicio = inicioDelMes, fin = fin)
    }

    override suspend fun mostrarVentasDelAno(): List<Venta> {
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val inicioDelAno = LocalDateTime(ahora.year, 1, 1, 0, 0)
            .toInstant(TimeZone.currentSystemDefault())
        val finDelAno = Clock.System.now()
        return obtenerVentasFiltradas(inicio = inicioDelAno, fin = finDelAno)
    }

    // -----------------------------------------------------
    // 🔹 Core genérico: obtenerVentasFiltradas
    // -----------------------------------------------------
    private suspend fun obtenerVentasFiltradas(
        cliente: String = "",
        inicio: Instant? = null,
        fin: Instant? = null
    ): List<Venta> = withContext(Dispatchers.IO) {
        try {
            val query = when {
                cliente.isNotBlank() && inicio != null && fin != null ->
                    buildQueryVentasPorClienteYFecha(cliente, inicio, fin)
                inicio != null && fin != null ->
                    buildQueryVentasDeHoy(inicio, fin)
                else -> buildQueryUltimasVentas(50)
            }

            val response: HttpResponse = client.post(databaseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            val jsonArray = Json.parseToJsonElement(responseText).jsonArray

            jsonArray.mapNotNull { element ->
                try {
                    val doc = element.jsonObject["document"]?.jsonObject ?: return@mapNotNull null
                    val fields = doc["fields"]?.jsonObject ?: return@mapNotNull null
                    VentaMapper.fromFirestore(fields)
                } catch (e: Exception) {
                    println("⚠️ Error parseando venta: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("❌ Error en obtenerVentasFiltradas: ${e.message}")
            emptyList()
        }
    }

    private suspend fun obtenerUltimasVentas(limite: Int): List<Venta> = withContext(Dispatchers.IO) {
        try {
            val body = buildQueryUltimasVentas(limite)
            val response: HttpResponse = client.post(databaseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(body)
            }
            val responseText = response.bodyAsText()
            val jsonArray = Json.parseToJsonElement(responseText).jsonArray

            jsonArray.mapNotNull { element ->
                try {
                    val doc = element.jsonObject["document"]?.jsonObject ?: return@mapNotNull null
                    val fields = doc["fields"]?.jsonObject ?: return@mapNotNull null
                    VentaMapper.fromFirestore(fields)
                } catch (e: Exception) {
                    println("⚠️ Error parseando venta: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("❌ Error en obtenerUltimasVentas: ${e.message}")
            emptyList()
        }
    }

    // -----------------------------------------------------
    // 🔹 Nuevas funciones para Devoluciones
    // -----------------------------------------------------

    /**
     * Devuelve todas las ventas que corresponden a un lote concreto.
     */
    override suspend fun obtenerVentasPorLote(loteNumber: String): List<Venta> = withContext(Dispatchers.IO) {
        try {
            val todas = mostrarTodasLasVentas()
            todas.filter { it.ventaLote == loteNumber }
        } catch (e: Exception) {
            println("❌ Error en obtenerVentasPorLote: ${e.message}")
            emptyList()
        }
    }

    /**
     * Devuelve los BigBags vendidos a un cliente dentro de un lote,
     * quedándose solo con la última venta (por fecha) de cada BigBag.
     */
    override suspend fun obtenerUltimosBigBagsDeCliente(
        loteNumber: String,
        cliente: String
    ): List<BigBags> = withContext(Dispatchers.IO) {
        try {
            val ventas = mostrarTodasLasVentas()
                .filter { it.ventaLote == loteNumber && it.ventaCliente == cliente }

            val ultimaVentaPorBb = ventas
                .flatMap { venta ->
                    venta.ventaBigbags.map { bb ->
                        Triple(bb.ventaBbNumber, bb.ventaBbWeight, venta.ventaFecha)
                    }
                }
                .groupBy { it.first }
                .mapValues { (_, registros) ->
                    registros.maxByOrNull { it.third ?: Instant.DISTANT_PAST }
                }
                .values
                .filterNotNull()

            ultimaVentaPorBb.map {
                BigBags(
                    bbNumber = it.first,
                    bbWeight = it.second,
                    bbLocation = "",
                    bbStatus = "o",
                    bbRemark = ""
                )
            }
        } catch (e: Exception) {
            println("❌ Error en obtenerUltimosBigBagsDeCliente: ${e.message}")
            emptyList()
        }
    }

    /**
     * Devuelve el cliente y fecha de la última venta de un BigBag concreto.
     */
    override suspend fun obtenerUltimoClienteYFechaDeBigBag(
        loteNumber: String,
        bbNumber: String
    ): Pair<String, Instant>? {
        return try {
            val ventasDelLote = mostrarTodasLasVentas().filter { it.ventaLote == loteNumber }
            ventasDelLote
                .filter { venta -> venta.ventaBigbags.any { it.ventaBbNumber == bbNumber } }
                .maxByOrNull { it.ventaFecha ?: Instant.DISTANT_PAST }
                ?.let { venta -> Pair(venta.ventaCliente, venta.ventaFecha!!) }
        } catch (e: Exception) {
            println("⚠️ Error al obtener último cliente/fecha del BigBag $bbNumber: ${e.message}")
            null
        }
    }
}
