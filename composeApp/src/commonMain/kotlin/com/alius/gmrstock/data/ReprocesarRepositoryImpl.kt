package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Reprocesar
import com.alius.gmrstock.data.firestore.parseRunQueryResponseReprocesar
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray

class ReprocesarRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : ReprocesarRepository {

    // 🔹 Función genérica para ejecutar queries en Firestore
    private suspend fun ejecutarQuery(query: String): List<Reprocesar> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [Firestore] POST $baseUrl")
            println("📤 Query: $query")

            val response: HttpResponse = client.post(baseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            println("📥 Respuesta JSON cruda completa (primeros 2000 chars):\n${responseText.take(2000)}")

            // Contar elementos del array raíz
            val rootArrayCount = try {
                val jsonElement = Json.parseToJsonElement(responseText)
                jsonElement.jsonArray.size
            } catch (_: Exception) { 0 }
            println("📌 Elementos en array raíz: $rootArrayCount")

            // Parsear la respuesta
            val parsedList = parseRunQueryResponseReprocesar(responseText)
            println("📦 Total reprocesos parseados: ${parsedList.size}")

            parsedList
        } catch (e: Exception) {
            println("❌ Error en ejecutarQuery (ReprocesarRepository): ${e.message}")
            emptyList()
        }
    }

    // 🔹 Listar todos los reprocesos (sin orderBy para depurar)
    override suspend fun listarReprocesos(): List<Reprocesar> {
        val query = """
        {
          "structuredQuery": {
            "from": [{ "collectionId": "reprocesar" }]
          }
        }
        """.trimIndent()

        val result = ejecutarQuery(query)
        println("✅ listarReprocesos finalizado. Total: ${result.size}")
        return result
    }

    // 🔹 Obtener un reproceso por número
    override suspend fun getReprocesoByNumber(reprocesoNumber: String): Reprocesar? {
        val query = """
        {
          "structuredQuery": {
            "from": [{ "collectionId": "reprocesar" }],
            "where": {
              "fieldFilter": {
                "field": { "fieldPath": "reprocesoNumber" },
                "op": "EQUAL",
                "value": { "stringValue": "$reprocesoNumber" }
              }
            }
          }
        }
        """.trimIndent()

        val result = ejecutarQuery(query)
        println("✅ getReprocesoByNumber finalizado. Total encontrados: ${result.size}")
        return result.firstOrNull()
    }
}
