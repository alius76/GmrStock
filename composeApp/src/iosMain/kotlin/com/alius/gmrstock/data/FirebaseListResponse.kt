package com.alius.gmrstock.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Cliente

@Serializable
data class FirebaseListResponse(
    val documents: List<FirebaseDocument> = emptyList()
)

@Serializable
data class FirebaseDocument(
    val name: String,
    val fields: Map<String, JsonElement>
) {
    fun toLoteDto(): LoteDto {
        val f = fields

        fun parseTimestampToMillis(key: String): Long? {
            val timestampStr = f[key]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.content
            return try {
                timestampStr?.let {
                    kotlinx.datetime.Instant.parse(it).toEpochMilliseconds()
                }
            } catch (e: Exception) {
                println("⚠️ Error al parsear timestamp '$key': ${e.message}")
                null
            }
        }

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
            bigBag = emptyList(),
            booked = null,
            dateBooked = parseTimestampToMillis("dateBooked"),
            remark = f["remark"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
        )
    }


}
