package com.alius.gmrstock.data

import com.alius.gmrstock.data.mappers.LoteDtoMapper
import com.alius.gmrstock.domain.model.LoteModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class LoteRepositoryImpl(
    private val client: HttpClient = HttpClient()
) : LoteRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun listarLotes(data: String): List<LoteModel> {
        return try {
            val url = "https://firestore.googleapis.com/v1/projects/gmrstock/databases/(default)/documents/lote"
            println("📡 Solicitando lotes desde: $url")

            val response: HttpResponse = client.get(url) {
                //parameter("orderBy", "fields.number.stringValue")
            }

            println("🌐 Código HTTP: ${response.status}")
            println("🔍 Content-Type: ${response.headers[HttpHeaders.ContentType]}")

            val jsonBody = response.bodyAsText()
            println("🧾 Respuesta JSON cruda:\n$jsonBody")

            if (jsonBody.isBlank()) {
                println("⚠️ El cuerpo de la respuesta está vacío.")
                return emptyList()
            }

            // 🔍 Paso nuevo: inspección manual del JSON
            val jsonElement: JsonElement = try {
                json.parseToJsonElement(jsonBody)
            } catch (e: Exception) {
                println("❌ Error al parsear JSON a JsonElement: ${e.message}")
                return emptyList()
            }

            println("🔬 JsonElement inspeccionado:\n$jsonElement")

            if (jsonElement !is JsonObject || !jsonElement.containsKey("documents")) {
                println("🚫 La clave 'documents' no está presente en el JSON. Firestore devolvió: $jsonElement")
                return emptyList()
            }

            // ✅ Ahora intentamos decodificar normalmente
            val parsed = try {
                json.decodeFromJsonElement(FirebaseListResponse.serializer(), jsonElement)
            } catch (e: Exception) {
                println("❌ Error al decodificar FirebaseListResponse: ${e.message}")
                return emptyList()
            }

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
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun agregarLoteConBigBags() {
        throw NotImplementedError("Firestore no está disponible en iOS (Kotlin/Native).")
    }
}

actual fun getLoteRepository(): LoteRepository = LoteRepositoryImpl()


