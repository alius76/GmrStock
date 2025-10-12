package com.alius.gmrstock.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Cliente
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
                timestampStr?.let { Instant.parse(it).toEpochMilliseconds() }
            } catch (e: Exception) {
                println("⚠️ Error al parsear timestamp '$key': ${e.message}")
                null
            }
        }

        // Función auxiliar para obtener un String value, siguiendo la estructura de Firebase REST
        fun getStringValue(key: String): String? {
            // Firestore REST API envuelve los valores primitivos en un objeto con la clave de tipo
            return f[key]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content
        }

        // Parsear la lista de BigBags
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

        // Parsear booked como Cliente
        val bookedCliente = f["booked"]?.jsonObject
            ?.get("mapValue")
            ?.jsonObject
            ?.get("fields")
            ?.jsonObject
            ?.let { bf ->
                Cliente(
                    cliNombre = bf["cliNombre"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                    cliObservaciones = bf["cliObservaciones"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                )
            }

        // Extraer los nuevos campos de la reserva (con la función auxiliar)
        val bookedByUser = getStringValue("bookedByUser")
        val bookedRemark = getStringValue("bookedRemark")

        // Crear el DTO completo
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
            booked = bookedCliente,
            dateBooked = parseTimestampToMillis("dateBooked"),
            remark = f["remark"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            createdAt = parseTimestampToMillis("createdAt"),
            certificateOk = f["certificateOk"]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false,
            // ------------------------------------
            // ⬇️ Asignación de los nuevos campos ⬇️
            // ------------------------------------
            bookedByUser = bookedByUser,
            bookedRemark = bookedRemark
            // ------------------------------------
        )
    }
}