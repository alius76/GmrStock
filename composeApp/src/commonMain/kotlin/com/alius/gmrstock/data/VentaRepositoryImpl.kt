package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.buildQueryUltimasVentas
import com.alius.gmrstock.data.firestore.buildQueryVentasDeHoy
import com.alius.gmrstock.data.firestore.buildQueryVentasPorClienteYFecha
import com.alius.gmrstock.data.mappers.VentaMapper
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

    /**
     * Función privada genérica para obtener ventas filtradas por cliente y rango de fechas.
     * Si cliente es "" trae todos los clientes.
     * Si inicio/fin son null, se traen todas las ventas (limite opcional aplicado en Firestore).
     */
    private suspend fun obtenerVentasFiltradas(
        cliente: String = "",
        inicio: Instant? = null,
        fin: Instant? = null
    ): List<Venta> = withContext(Dispatchers.IO) {
        try {
            // Generamos la query según si hay cliente o rango de fechas
            val query = if (cliente.isNotBlank() && inicio != null && fin != null) {
                buildQueryVentasPorClienteYFecha(cliente, inicio, fin)
            } else if (inicio != null && fin != null) {
                buildQueryVentasDeHoy(inicio, fin) // puedes crear una query genérica por fecha si quieres
            } else {
                buildQueryUltimasVentas(50) // límite por defecto si no hay filtro
            }

            println("🌐 POST $databaseUrl (ventas filtradas)")
            println("📤 Body: $query")

            val response: HttpResponse = client.post(databaseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            println("📦 Response ventas filtradas:\n$responseText")

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


    /**
     * Método específico para obtener las últimas N ventas.
     */
    private suspend fun obtenerUltimasVentas(limite: Int): List<Venta> = withContext(Dispatchers.IO) {
        try {
            val body = buildQueryUltimasVentas(limite)

            println("🌐 POST $databaseUrl (ultimas $limite ventas)")
            println("📤 Body: $body")

            val response: HttpResponse = client.post(databaseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(body)
            }

            val responseText = response.bodyAsText()
            println("📦 Response ultimas ventas:\n$responseText")

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
}
