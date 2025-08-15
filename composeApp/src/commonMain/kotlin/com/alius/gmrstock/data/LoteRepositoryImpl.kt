package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.parseRunQueryResponse
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.MaterialGroup
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.http.*
import kotlinx.coroutines.IO
import kotlinx.datetime.*

class LoteRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : LoteRepository {

    // ✅ Función privada para traer TODOS los lotes desde Firestore
    private suspend fun obtenerLotes(): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            val body = """
            {
              "structuredQuery": {
                "from": [{ "collectionId": "lote" }]
              }
            }
            """.trimIndent()

            println("🌐 POST $baseUrl")
            println("📤 Body: $body")

            val response: HttpResponse = client.post(baseUrl) {
                headers {
                    append("Content-Type", "application/json")
                }
                setBody(body)
            }

            parseRunQueryResponse(response.bodyAsText())
        } catch (e: Exception) {
            println("❌ Error en obtenerLotes: ${e.message}")
            emptyList()
        }
    }

    override suspend fun listarLotes(data: String): List<LoteModel> {
        return try {
            obtenerLotes().filter {
                it.number.contains(data, ignoreCase = true) ||
                        it.description.contains(data, ignoreCase = true)
            }
        } catch (e: Exception) {
            println("❌ Error general en listarLotes: ${e.message}")
            emptyList()
        }
    }

    override suspend fun listarGruposPorDescripcion(filter: String): List<MaterialGroup> {
        val lotes = listarLotes(filter)
        return agruparPorMaterial(lotes)
    }

    override suspend fun getLoteByNumber(number: String): LoteModel? {
        return obtenerLotes().firstOrNull { it.number == number }
    }

    override suspend fun listarLotesCreadosHoy(): List<LoteModel> {
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return listarLotesPorFecha(hoy)
    }

    override suspend fun listarLotesPorFecha(fecha: LocalDate): List<LoteModel> {
        return obtenerLotes().filter { lote ->
            lote.createdAt?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == fecha
        }
    }

    override suspend fun listarUltimosLotes(cantidad: Int): List<LoteModel> {
        return obtenerLotes()
            .sortedByDescending { it.date ?: Instant.DISTANT_PAST }
            .take(cantidad)
    }
}
