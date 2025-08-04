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

            val response: HttpResponse = client.get(url) {
                parameter("orderBy", "fields.number.stringValue")
            }

            val jsonBody = response.bodyAsText()
            val parsed = json.decodeFromString(FirebaseListResponse.serializer(), jsonBody)

            parsed.documents.mapNotNull { doc ->
                try {
                    val dto = doc.toLoteDto()
                    LoteDtoMapper.fromDto(dto)
                } catch (e: Exception) {
                    null
                }
            }.filter { it.number.startsWith(data) }

        } catch (e: Exception) {
            emptyList()
        }
    }
    override suspend fun agregarLoteConBigBags() {
        throw NotImplementedError("Firestore no está disponible en iOS (Kotlin/Native).")
    }
}


actual fun getLoteRepository(): LoteRepository = LoteRepositoryImpl()