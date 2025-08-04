package com.alius.gmrstock.data

import com.alius.gmrstock.data.mappers.LoteDtoMapper
import com.alius.gmrstock.domain.model.LoteModel
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class LoteRepositoryImpl(
    private val client: HttpClient = HttpClient()
) : LoteRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun listarLotes(data: String): List<LoteModel> {
        return try {
            val url = "https://firestore.googleapis.com/v1/projects/gmrstock/databases/(default)/documents/lote"

            println("📡 Solicitando lotes desde: $url")

            val response: HttpResponse = client.get(url) {
                parameter("orderBy", "fields.number.stringValue")
            }

            val jsonBody = response.bodyAsText()
            println("🧾 Respuesta JSON cruda:\n$jsonBody")

            val parsed = json.decodeFromString(FirebaseListResponse.serializer(), jsonBody)
            println("✅ Documentos parseados: ${parsed.documents.size}")

            parsed.documents.mapNotNull { doc ->
                try {
                    println("📄 Documento Firestore:\n$doc")
                    val dto = doc.toLoteDto()
                    println("🔄 DTO mapeado: $dto")
                    LoteDtoMapper.fromDto(dto)
                } catch (e: Exception) {
                    println("❌ Error mapeando documento: ${e.message}")
                    null
                }
            }.filter { it.number.startsWith(data) }

        } catch (e: Exception) {
            println("❌ Error general en listarLotes: ${e.message}")
            emptyList()
        }
    }

    override suspend fun agregarLoteConBigBags() {
        throw NotImplementedError("Firestore no está disponible en iOS (Kotlin/Native).")
    }
}

actual fun getLoteRepository(): LoteRepository = LoteRepositoryImpl()
