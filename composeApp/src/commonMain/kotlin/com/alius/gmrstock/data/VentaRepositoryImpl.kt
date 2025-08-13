package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.domain.model.VentaBigbag
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import kotlinx.datetime.Instant

class VentaRepositoryImpl(
    private val client: HttpClient,
    private val databaseUrl: String
) : VentaRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun mostrarTodasLasVentas(): List<Venta> = withContext(Dispatchers.IO) {
        try {
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

            return@withContext jsonArray.mapNotNull { element ->
                try {
                    val doc = element.jsonObject["document"]?.jsonObject ?: return@mapNotNull null
                    val fields = doc["fields"]?.jsonObject ?: return@mapNotNull null

                    // Mapeo de los bigbags
                    val ventaBigbags = fields["ventaBigbags"]?.jsonObject
                        ?.get("arrayValue")?.jsonObject
                        ?.get("values")?.jsonArray
                        ?.mapNotNull { item ->
                            try {
                                val f = item.jsonObject["mapValue"]?.jsonObject
                                    ?.get("fields")?.jsonObject ?: return@mapNotNull null
                                VentaBigbag(
                                    ventaBbNumber = f["ventaBbNumber"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                                    ventaBbWeight = f["ventaBbWeight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                                )
                            } catch (e: Exception) {
                                println("⚠️ Error parseando un bigbag: ${e.message}")
                                null
                            }
                        } ?: emptyList()

                    Venta(
                        ventaCliente = fields["ventaCliente"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                        ventaLote = fields["ventaLote"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                        ventaMaterial = fields["ventaMaterial"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                        ventaFecha = fields["ventaFecha"]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.contentOrNull?.let {
                            Instant.parse(it)
                        },
                        ventaBigbags = ventaBigbags
                    )
                } catch (e: Exception) {
                    println("⚠️ Error parseando venta: ${e.message}")
                    null
                }
            }

        } catch (e: Exception) {
            println("❌ Error en mostrarTodasLasVentas: ${e.message}")
            emptyList()
        }
    }
}
