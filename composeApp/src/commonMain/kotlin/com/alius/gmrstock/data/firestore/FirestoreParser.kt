package com.alius.gmrstock.data.firestore

import com.alius.gmrstock.data.FirebaseDocument
import com.alius.gmrstock.data.mappers.LoteDtoMapper
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.Material
import com.alius.gmrstock.domain.model.Reprocesar
import com.alius.gmrstock.domain.model.ReprocesarBigBag
import com.alius.gmrstock.domain.model.Trasvase
import com.alius.gmrstock.domain.model.TrasvaseBigBag
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.datetime.Instant
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.long

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
fun parseRunQueryResponseReprocesar(jsonBody: String): List<Reprocesar> {
    val rootElement = try {
        json.parseToJsonElement(jsonBody)
    } catch (_: Exception) {
        return emptyList()
    }

    val elements = rootElement.jsonArrayOrNull ?: return emptyList()
    val resultList = mutableListOf<Reprocesar>()

    for (element in elements) {
        try {
            val docElement = element.jsonObject["document"] ?: continue
            val fields = docElement.jsonObject["fields"]?.jsonObject ?: continue

            val reproBigBags = fields["bigBagsReprocesados"]
                ?.jsonObject
                ?.get("arrayValue")
                ?.jsonObject
                ?.get("values")
                ?.jsonArray
                ?.mapNotNull { bbJson ->
                    val bbFields = bbJson.jsonObject["mapValue"]?.jsonObject?.get("fields")?.jsonObject
                        ?: return@mapNotNull null
                    ReprocesarBigBag(
                        bbNumber = bbFields["bbNumber"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                        bbWeight = bbFields["bbWeight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                    )
                } ?: emptyList()

            val reproceso = Reprocesar(
                reprocesoNumber = fields["reprocesarLoteNumber"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                reprocesoDescription = fields["reprocesarDescription"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                reprocesoCreatedAt = fields["reprocesarCreatedAt"]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.content
                    ?.let { Instant.parse(it) },
                reprocesoLoteWeight = fields["reprocesarTotalWeight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                reprocesoTargetLoteNumber = fields["reprocesarLoteDestino"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                reprocesoDate = fields["reprocesarFechaReproceso"]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.content
                    ?.let { Instant.parse(it) },
                reprocesoBigBag = reproBigBags
            )

            resultList.add(reproceso)
        } catch (_: Exception) {
            // Ignorar documento si falla el parseo
        }
    }

    // 🔹 Ordenar por fecha de reproceso descendente
    return resultList.sortedByDescending { it.reprocesoDate }
}

fun parseRunQueryResponseComanda(jsonBody: String): List<Comanda> {

    val rootElement = try {
        json.parseToJsonElement(jsonBody)
    } catch (_: Exception) {
        return emptyList()
    }

    val elements = rootElement.jsonArrayOrNull ?: return emptyList()
    val resultList = mutableListOf<Comanda>()

    for (element in elements) {
        try {
            val docElement = element.jsonObject["document"] ?: continue
            val fields = docElement.jsonObject["fields"]?.jsonObject ?: continue

            // ----- CLIENTE -----
            val bookedClientMap = fields["bookedClientComanda"]?.jsonObject
                ?.get("mapValue")?.jsonObject
                ?.get("fields")?.jsonObject

            val bookedClient = bookedClientMap?.let { cliFields ->
                Cliente(
                    cliNombre = cliFields["cliNombre"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                    cliObservaciones = cliFields["cliObservaciones"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                )
            }

            // ----- CAMPOS PRINCIPALES -----
            val idComanda = docElement.jsonObject["name"]?.jsonPrimitive?.content?.substringAfterLast("/") ?: ""
            val number = fields["numberLoteComanda"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
            val description = fields["descriptionLoteComanda"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
            val date = fields["dateBookedComanda"]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.content
            val weight = fields["totalWeightComanda"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
            val remark = fields["remarkComanda"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
            val fueVendido = fields["fueVendidoComanda"]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.boolean ?: false

            // ------ NUEVO CAMPO ------
            val numeroDeComanda = fields["numeroDeComanda"]
                ?.jsonObject
                ?.get("integerValue")
                ?.jsonPrimitive
                ?.content
                ?.toLongOrNull() ?: 0L

            val parsedInstant = date?.let { Instant.parse(it) }

            val comanda = Comanda(
                idComanda = idComanda,
                numberLoteComanda = number,
                descriptionLoteComanda = description,
                dateBookedComanda = parsedInstant,
                totalWeightComanda = weight,
                bookedClientComanda = bookedClient,
                remarkComanda = remark,
                fueVendidoComanda = fueVendido,
                numeroDeComanda = numeroDeComanda
            )

            resultList.add(comanda)

        } catch (_: Exception) {
            continue
        }
    }

    return resultList
}




fun parseRunQueryResponseMaterial(responseText: String): List<Material> {
    return try {
        val json = Json.parseToJsonElement(responseText).jsonArray
        json.mapNotNull { element ->
            val fields = element.jsonObject["document"]?.jsonObject?.get("fields")?.jsonObject
            if (fields != null) {
                val id = element.jsonObject["document"]?.jsonObject?.get("name")?.toString()?.substringAfterLast("/") ?: ""
                val materialNombre = fields["materialNombre"]?.jsonObject?.get("stringValue")?.toString()?.replace("\"", "") ?: ""
                Material(id = id, materialNombre = materialNombre)
            } else null
        }
    } catch (e: Exception) {
        println("❌ Error parseando response Material: ${e.message}")
        emptyList()
    }
}


// -------------------- EXTENSION JSON --------------------

private val JsonElement.jsonArrayOrNull
    get() = try { this.jsonArray } catch (_: Exception) { null }
