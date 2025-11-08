package com.alius.gmrstock.data.mappers

import com.alius.gmrstock.domain.model.Devolucion
import com.alius.gmrstock.domain.model.DevolucionBigbag
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.Instant
import kotlinx.serialization.json.contentOrNull

object DevolucionMapper {

    private fun JsonObject.getStringOrEmpty(key: String): String =
        this[key]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""

    fun fromFirestore(fields: JsonObject): Devolucion {
        val devolucionBigbags = fields["devolucionBigbags"]?.jsonObject
            ?.get("arrayValue")?.jsonObject
            ?.get("values")?.jsonArray
            ?.mapNotNull { item ->
                try {
                    val f = item.jsonObject["mapValue"]?.jsonObject
                        ?.get("fields")?.jsonObject ?: return@mapNotNull null
                    DevolucionBigbag(
                        devolucionBbNumber = f.getStringOrEmpty("devolucionBbNumber"),
                        devolucionBbWeight = f.getStringOrEmpty("devolucionBbWeight")
                    )
                } catch (e: Exception) {
                    println("⚠️ Error parseando un BigBag de devolución: ${e.message}")
                    null
                }
            } ?: emptyList()

        val devolucionFecha = fields["devolucionFecha"]?.jsonObject
            ?.get("timestampValue")?.jsonPrimitive?.contentOrNull?.let { Instant.parse(it) }

        val devolucionPesoTotal = fields["devolucionPesoTotal"]?.jsonObject
            ?.get("stringValue")?.jsonPrimitive?.contentOrNull

        return Devolucion(
            devolucionCliente = fields.getStringOrEmpty("devolucionCliente"),
            devolucionLote = fields.getStringOrEmpty("devolucionLote"),
            devolucionMaterial = fields.getStringOrEmpty("devolucionMaterial"),
            devolucionFecha = devolucionFecha,
            devolucionPesoTotal = devolucionPesoTotal,
            devolucionBigbags = devolucionBigbags
        )
    }
}
