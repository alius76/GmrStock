package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Trasvase
import com.alius.gmrstock.domain.model.TrasvaseBigBag
import com.alius.gmrstock.data.firestore.parseRunQueryResponseTrasvase
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class TrasvaseRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : TrasvaseRepository {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Función genérica para ejecutar queries en Firestore para trasvases
     */
    private suspend fun ejecutarQuery(query: String): List<Trasvase> = withContext(Dispatchers.IO) {
        try {
            println("🌐 POST $baseUrl")
            println("📤 Body: $query")

            val response: HttpResponse = client.post(baseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            parseRunQueryResponseTrasvase(responseText)
        } catch (e: Exception) {
            println("❌ Error en ejecutarQuery TrasvaseRepository: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getTrasvaseByLote(trasvaseNumber: String): Trasvase? {
        val query = buildQueryTrasvasePorNumero(trasvaseNumber)
        val results = ejecutarQuery(query)
        return results.firstOrNull()
    }

    override suspend fun getTrasvaseBigBagsByLote(trasvaseNumber: String): List<TrasvaseBigBag> {
        val trasvase = getTrasvaseByLote(trasvaseNumber)
        return trasvase?.trasvaseBigBag ?: emptyList()
    }

    /**
     * 🔹 Nueva función: obtener todos los trasvases de un lote
     */
    override suspend fun getTrasvasesByLote(trasvaseNumber: String): List<Trasvase> {
        val query = buildQueryTrasvasePorNumero(trasvaseNumber)
        val results = ejecutarQuery(query)
        println("📅 getTrasvasesByLote($trasvaseNumber) -> ${results.size} trasvases encontrados")
        results.forEachIndexed { index, t ->
            println("   Trasvase ${index + 1}: fecha=${t.trasvaseDate}, bigbags=${t.trasvaseBigBag.map { it.bbTrasNumber }}")
        }
        return results
    }

    // 🔹 Función auxiliar para construir query Firestore
    private fun buildQueryTrasvasePorNumero(trasvaseNumber: String): String {
        return """
        {
          "structuredQuery": {
            "from": [{ "collectionId": "trasvase" }],
            "where": {
              "fieldFilter": {
                "field": { "fieldPath": "trasvaseNumber" },
                "op": "EQUAL",
                "value": { "stringValue": "$trasvaseNumber" }
              }
            }
          }
        }
        """.trimIndent()
    }
}
