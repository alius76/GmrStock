package com.alius.gmrstock.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import com.alius.gmrstock.domain.model.BigBags
import kotlinx.datetime.Instant

@Serializable
data class FirebaseDocument(
    val name: String,
    val fields: Map<String, JsonElement>
) {
    fun toLoteDto(): LoteDto {
        val f = fields

        // Función para parsear timestamps ISO 8601 a milisegundos Unix
        fun parseTimestampToMillis(key: String): Long? {
            val timestampStr = f[key]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.content
            return try {
                timestampStr?.let {
                    Instant.parse(it).toEpochMilliseconds()
                }
            } catch (e: Exception) {
                println("⚠️ Error al parsear timestamp '$key': ${e.message}")
                null
            }
        }

        // Parsear la lista de BigBags del campo "bigBag"
        val bigBagList = f["bigBag"]
            ?.jsonObject
            ?.get("arrayValue")
            ?.jsonObject
            ?.get("values")
            ?.jsonArray
            ?.mapNotNull { bbJson ->
                val bbFields = bbJson.jsonObject["mapValue"]
                    ?.jsonObject
                    ?.get("fields")
                    ?.jsonObject ?: return@mapNotNull null

                BigBags(
                    bbNumber = bbFields["bbNumber"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                    bbWeight = bbFields["bbWeight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                    bbLocation = bbFields["bbLocation"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                    bbStatus = bbFields["bbStatus"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                    bbRemark = bbFields["bbRemark"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                )
            } ?: emptyList()

        // Parsear el campo booked si existe
        val bookedEmail = f["booked"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content

        // Finalmente crear el DTO completo
        return LoteDto(
            id = name.substringAfterLast("/"),
            number = f["number"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            description = f["description"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            date = parseTimestampToMillis("date"),
            location = f["location"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            count = f["count"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            weight = f["weight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            status = f["status"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            totalWeight = f["totalWeight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            qrCode = f["qrCode"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content,
            bigBag = bigBagList,
            booked = bookedEmail,
            dateBooked = parseTimestampToMillis("dateBooked"),
            remark = f["remark"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            createdAt = parseTimestampToMillis("createdAt") // ⬅ nuevo campo mapeado
        )
    }
}


