package com.alius.gmrstock.data

import com.alius.gmrstock.data.mappers.VentaMapper
import com.alius.gmrstock.domain.model.Venta
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class VentaRepositoryImpl(
    private val client: HttpClient,
    private val databaseUrl: String
) : VentaRepository {

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
                    VentaMapper.fromFirestore(fields)
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
