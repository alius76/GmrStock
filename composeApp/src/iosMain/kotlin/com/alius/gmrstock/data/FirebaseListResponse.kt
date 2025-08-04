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

        fun parseTimestamp(key: String): Long? {
            val ts = f[key]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.content
            return ts?.let {
                try {
                    kotlinx.datetime.Instant.parse(it).toEpochMilliseconds()
                } catch (e: Exception) {
                    null
                }
            }
        }

        fun parseBigBags(): List<BigBags> {
            return f["bigBag"]
                ?.jsonObject?.get("arrayValue")
                ?.jsonObject?.get("values")
                ?.jsonArray?.mapNotNull { el ->
                    try {
                        val map = el.jsonObject["mapValue"]?.jsonObject?.get("fields")?.jsonObject
                        BigBags(
                            bbNumber = map?.get("bbNumber")?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                            bbWeight = map?.get("bbWeight")?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                            bbStatus = map?.get("bbStatus")?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                            bbLocation = map?.get("bbLocation")?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                            bbRemark = map?.get("bbRemark")?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                        )
                    } catch (e: Exception) {
                        println("⚠️ Error parseando BigBag: ${e.message}")
                        null
                    }
                } ?: emptyList()
        }

        return LoteDto(
            id = name.substringAfterLast("/"),
            number = f["number"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            description = f["description"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            date = parseTimestamp("date"),
            location = f["location"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            count = f["count"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            weight = f["weight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            status = f["status"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            totalWeight = f["totalWeight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            qrCode = f["qrCode"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content,
            bigBag = parseBigBags(),
            booked = f["booked"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content,
            dateBooked = parseTimestamp("dateBooked"),
            remark = f["remark"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
        )
    }


}
