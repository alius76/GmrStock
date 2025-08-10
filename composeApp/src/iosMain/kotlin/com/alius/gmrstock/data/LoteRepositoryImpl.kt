package com.alius.gmrstock.data

import com.alius.gmrstock.data.mappers.LoteDtoMapper
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.MaterialGroup
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

    override suspend fun listarGruposPorDescripcion(filter: String): List<MaterialGroup> {
        val lotes = listarLotes(filter)
        return agruparPorMaterial(lotes)
    }

    // Aquí la función getLoteByNumber usando POST runQuery
    override suspend fun getLoteByNumber(number: String): LoteModel? {
        val url = "https://firestore.googleapis.com/v1/projects/gmrstock/databases/(default)/documents:runQuery"

        val queryJson = buildJsonObject {
            putJsonObject("structuredQuery") {
                putJsonArray("from") {
                    add(buildJsonObject { put("collectionId", "lote") })
                }
                putJsonObject("where") {
                    putJsonObject("fieldFilter") {
                        putJsonObject("field") { put("fieldPath", "number") }
                        put("op", "EQUAL")
                        putJsonObject("value") { put("stringValue", number) }
                    }
                }
                put("limit", 1)
            }
        }

        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                //autenticación de Firebase:
                // headers { append(HttpHeaders.Authorization, "Bearer $authToken") }
                setBody(queryJson) // Enviamos el JsonObject directamente
            }

            val body = response.bodyAsText()
            println("🧾 Respuesta runQuery:\n$body")

            val jsonArray = Json.parseToJsonElement(body).jsonArray
            val documentElement = jsonArray.firstOrNull()
                ?.jsonObject
                ?.get("document")
                ?: return null // No hay coincidencia

            val firebaseDocument = json.decodeFromJsonElement<FirebaseDocument>(documentElement)
            val dto = firebaseDocument.toLoteDto()
            LoteDtoMapper.fromDto(dto)

        } catch (e: Exception) {
            println("❌ Error en getLoteByNumber: ${e.message}")
            null
        }
    }

}

actual fun getLoteRepository(): LoteRepository = LoteRepositoryImpl()

