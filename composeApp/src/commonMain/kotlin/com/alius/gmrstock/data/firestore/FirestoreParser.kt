package com.alius.gmrstock.data.firestore

import com.alius.gmrstock.data.FirebaseDocument
import com.alius.gmrstock.data.mappers.LoteDtoMapper
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.Trasvase
import com.alius.gmrstock.domain.model.TrasvaseBigBag
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.datetime.Instant

private val json = Json { ignoreUnknownKeys = true }


// -------------------- PARSER LOTES --------------------

fun parseRunQueryResponse(jsonBody: String): List<LoteModel> {
    println("📥 Respuesta JSON cruda recibida (primeros 500 chars):\n${jsonBody.take(500)}")

    val rootElement = try {
        json.parseToJsonElement(jsonBody)
    } catch (e: Exception) {
        println("❌ Error parseando JSON raíz: ${e.message}")
        return emptyList()
    }

    val resultList = mutableListOf<LoteModel>()
    val elements = rootElement.jsonArrayOrNull ?: return emptyList()

    for (element in elements) {
        try {
            when {
                element.jsonObject.containsKey("document") -> {
                    element.jsonObject["document"]?.let { parseAndAddDocument(it, resultList) }
                }
                element.jsonObject.containsKey("documents") -> {
                    val docsArray = element.jsonObject["documents"]?.jsonArrayOrNull
                    docsArray?.forEach { doc -> parseAndAddDocument(doc, resultList) }
                }
            }
        } catch (e: Exception) {
            println("❌ Error procesando elemento raíz: ${e.message}")
        }
    }

    return resultList
}

private fun parseAndAddDocument(docElement: JsonElement, resultList: MutableList<LoteModel>) {
    try {
        val parsedDoc = json.decodeFromJsonElement<FirebaseDocument>(docElement)
        val dto = parsedDoc.toLoteDto()
        val model = LoteDtoMapper.fromDto(dto)
        resultList.add(model)
    } catch (e: Exception) {
        println("❌ Error parseando documento individual: ${e.message}")
    }
}


fun parseRunQueryResponseTrasvase(jsonBody: String): List<Trasvase> {
    println("📥 Respuesta JSON cruda de Trasvase (primeros 500 chars):\n${jsonBody.take(500)}")

    val rootElement = try {
        json.parseToJsonElement(jsonBody)
    } catch (e: Exception) {
        println("❌ Error parseando JSON raíz de Trasvase: ${e.message}")
        return emptyList()
    }

    val elements = rootElement.jsonArrayOrNull ?: return emptyList()
    val resultList = mutableListOf<Trasvase>()

    for (element in elements) {
        try {
            val docElement = element.jsonObject["document"] ?: continue
            val fields = docElement.jsonObject["fields"]?.jsonObject ?: continue

            val trasvaseBigBags = fields["trasvaseBigBag"]
                ?.jsonObject
                ?.get("arrayValue")
                ?.jsonObject
                ?.get("values")
                ?.jsonArray
                ?.mapNotNull { bbJson ->
                    val bbFields = bbJson.jsonObject["mapValue"]?.jsonObject?.get("fields")?.jsonObject
                        ?: return@mapNotNull null
                    TrasvaseBigBag(
                        bbTrasNumber = bbFields["bbTrasNumber"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                        bbTrasWeight = bbFields["bbTrasWeight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                    )
                } ?: emptyList()

            val trasvase = Trasvase(
                trasvaseId = docElement.jsonObject["name"]?.jsonPrimitive?.content?.substringAfterLast("/") ?: "",
                trasvaseNumber = fields["trasvaseNumber"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                trasvaseDescription = fields["trasvaseDescription"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                trasvaseLocation = fields["trasvaseLocation"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                trasvaseCount = fields["trasvaseCount"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                trasvaseTotalWeight = fields["trasvaseTotalWeight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                trasvaseDate = fields["trasvaseDate"]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.content
                    ?.let { kotlinx.datetime.Instant.parse(it) },
                trasvaseBigBag = trasvaseBigBags
            )

            resultList.add(trasvase)
        } catch (e: Exception) {
            println("❌ Error procesando documento de Trasvase: ${e.message}")
        }
    }

    return resultList
}

// -------------------- EXTENSION JSON --------------------

private val JsonElement.jsonArrayOrNull
    get() = try { this.jsonArray } catch (_: Exception) { null }
