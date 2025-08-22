package com.alius.gmrstock.data.firestore

import com.alius.gmrstock.data.FirebaseDocument
import com.alius.gmrstock.data.mappers.LoteDtoMapper
import com.alius.gmrstock.data.mappers.ProcessMapper
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.Process
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.decodeFromJsonElement

private val json = Json { ignoreUnknownKeys = true }


fun parseRunQueryResponse(jsonBody: String): List<LoteModel> {
    println("📥 Respuesta JSON cruda recibida (primeros 500 chars):\n${jsonBody.take(500)}")

    val rootElement = try {
        json.parseToJsonElement(jsonBody)
    } catch (e: Exception) {
        println("❌ Error parseando JSON raíz: ${e.message}")
        return emptyList()
    }

    val resultList = mutableListOf<LoteModel>()

    // Determina si es un array raíz
    val elements = rootElement.jsonArrayOrNull ?: return emptyList()

    for (element in elements) {
        try {
            when {
                element.jsonObject.containsKey("document") -> {
                    // Caso individual
                    val docElement = element.jsonObject["document"]
                    docElement?.let {
                        parseAndAddDocument(it, resultList)
                    }
                }
                element.jsonObject.containsKey("documents") -> {
                    // Caso array de documentos
                    val docsArray = element.jsonObject["documents"]?.jsonArrayOrNull
                    docsArray?.forEach { doc ->
                        parseAndAddDocument(doc, resultList)
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error procesando elemento raíz: ${e.message}")
        }
    }

    return resultList
}

// Función auxiliar para parsear un documento y agregarlo a la lista
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

// Extensión para evitar excepción si no es array
private val JsonElement.jsonArrayOrNull
    get() = try { this.jsonArray } catch (_: Exception) { null }




