package com.alius.gmrstock.data

import com.alius.gmrstock.data.mappers.LoteDtoMapper
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.MaterialGroup
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

class LoteRepositoryImpl(
    private val client: HttpClient
) : LoteRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun listarLotes(data: String): List<LoteModel> {
        return try {
            val url = "https://firestore.googleapis.com/v1/projects/gmrstock/databases/(default)/documents:runQuery"
            println("🌐 Ejecutando structuredQuery con filtro: '$data'")

            val queryJson = if (data.isNotBlank()) {
                """
            {
                "structuredQuery": {
                    "from": [{ "collectionId": "lote" }],
                    "where": {
                        "compositeFilter": {
                            "op": "AND",
                            "filters": [
                                {
                                    "fieldFilter": {
                                        "field": { "fieldPath": "number" },
                                        "op": "GREATER_THAN_OR_EQUAL",
                                        "value": { "stringValue": "$data" }
                                    }
                                },
                                {
                                    "fieldFilter": {
                                        "field": { "fieldPath": "number" },
                                        "op": "LESS_THAN",
                                        "value": { "stringValue": "${data}\uf8ff" }
                                    }
                                }
                            ]
                        }
                    },
                    "orderBy": [
                        {
                            "field": { "fieldPath": "number" },
                            "direction": "ASCENDING"
                        }
                    ]
                }
            }
            """.trimIndent()
            } else {
                """
            {
                "structuredQuery": {
                    "from": [{ "collectionId": "lote" }],
                    "orderBy": [
                        {
                            "field": { "fieldPath": "number" },
                            "direction": "ASCENDING"
                        }
                    ]
                }
            }
            """.trimIndent()
            }

            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(queryJson)
            }

            val lotes = parseRunQueryResponse(response.bodyAsText())
            println("✅ Parseados ${lotes.size} lotes de Firestore")
            lotes
        } catch (e: Exception) {
            println("❌ Error general en listarLotes: ${e.message}")
            emptyList()
        }
    }

    override suspend fun agregarLoteConBigBags() {
        // Implementa tu lógica aquí
    }

    override suspend fun listarGruposPorDescripcion(filter: String): List<MaterialGroup> {
        println("📝 listando grupos por descripción con filtro: '$filter'")
        val lotes = listarLotes(filter)
        println("🗂️ Agrupando ${lotes.size} lotes para formar grupos de materiales")
        val grupos = agruparPorMaterial(lotes)
        println("📊 Resultado agrupación: ${grupos.size} grupos encontrados")
        return grupos
    }

    override suspend fun getLoteByNumber(number: String): LoteModel? {
        val url = "https://firestore.googleapis.com/v1/projects/gmrstock/databases/(default)/documents:runQuery"

        val queryJson = """
        {
            "structuredQuery": {
                "from": [{"collectionId": "lote"}],
                "where": {
                    "fieldFilter": {
                        "field": {"fieldPath": "number"},
                        "op": "EQUAL",
                        "value": {"stringValue": "$number"}
                    }
                },
                "limit": 1
            }
        }
    """.trimIndent()

        return try {
            println("🔍 Buscando lote con número: $number")
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(queryJson)
            }

            parseRunQueryResponse(response.bodyAsText()).firstOrNull()
        } catch (e: Exception) {
            println("❌ Error en getLoteByNumber: ${e.message}")
            null
        }
    }

    private fun parseRunQueryResponse(jsonBody: String): List<LoteModel> {
        println("📥 Respuesta JSON cruda recibida (primeros 500 chars):\n${jsonBody.take(500)}")

        val parsedList = json.decodeFromString<List<Map<String, JsonElement>>>(jsonBody)

        return parsedList.mapNotNull { obj ->
            try {
                val doc = obj["document"] ?: return@mapNotNull null
                val parsedDoc = json.decodeFromJsonElement<FirebaseDocument>(doc)
                val dto = parsedDoc.toLoteDto()
                LoteDtoMapper.fromDto(dto)
            } catch (e: Exception) {
                println("❌ Error parseando documento: ${e.message}")
                null
            }
        }
    }

}