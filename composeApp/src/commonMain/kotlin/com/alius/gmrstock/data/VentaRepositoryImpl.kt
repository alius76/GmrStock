package com.alius.gmrstock.data

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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class VentaRepositoryImpl(
    private val client: HttpClient,
    private val databaseUrl: String
) : VentaRepository {

    override suspend fun mostrarTodasLasVentas(): List<Venta> = withContext(Dispatchers.IO) {
        obtenerVentas()
    }

    override suspend fun mostrarLasVentasDeHoy(): List<Venta> = withContext(Dispatchers.IO) {
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        obtenerVentas()
            .filter { venta ->
                venta.ventaFecha?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == hoy
            }
    }

    override suspend fun mostrarLasUltimasVentas(): List<Venta> = withContext(Dispatchers.IO) {
        obtenerVentas()
            .sortedByDescending { it.ventaFecha }
            .take(5)
    }

    override suspend fun mostrarVentasPorCliente(cliente: String): List<Venta> = withContext(Dispatchers.IO) {
        obtenerVentas()
            .filter { it.ventaCliente == cliente }
            .sortedByDescending { it.ventaFecha }
    }

    override suspend fun mostrarVentasDelMesPorCliente(cliente: String): List<Venta> = withContext(Dispatchers.IO) {
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val mesActual = ahora.month
        val anioActual = ahora.year

        obtenerVentas()
            .filter { venta ->
                venta.ventaCliente == cliente &&
                        venta.ventaFecha?.toLocalDateTime(TimeZone.currentSystemDefault())?.let {
                            it.month == mesActual && it.year == anioActual
                        } ?: false
            }
            .sortedByDescending { it.ventaFecha }
    }

    /**
     * Método privado reutilizable para obtener ventas desde Firestore
     */
    private suspend fun obtenerVentas(): List<Venta> {
        return try {
            val body = """
            {
              "structuredQuery": {
                "from": [{ "collectionId": "venta" }]
              }
            }
            """.trimIndent()

            println("🌐 POST $databaseUrl")
            println("📤 Body: $body")

            val response: HttpResponse = client.post(databaseUrl) {
                headers {
                    append("Content-Type", "application/json")
                }
                setBody(body)
            }

            val responseText = response.bodyAsText()
            println("📦 Response body:\n$responseText")

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
            println("❌ Error en obtenerVentas: ${e.message}")
            emptyList()
        }
    }
}
