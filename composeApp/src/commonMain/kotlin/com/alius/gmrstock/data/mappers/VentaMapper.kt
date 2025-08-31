package com.alius.gmrstock.data.mappers

import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.domain.model.VentaBigbag
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.Instant
import kotlinx.serialization.json.contentOrNull

object VentaMapper {

    fun fromFirestore(fields: JsonObject): Venta {
        val ventaBigbags = fields["ventaBigbags"]?.jsonObject
            ?.get("arrayValue")?.jsonObject
            ?.get("values")?.jsonArray
            ?.mapNotNull { item ->
                try {
                    val f = item.jsonObject["mapValue"]?.jsonObject
                        ?.get("fields")?.jsonObject ?: return@mapNotNull null
                    VentaBigbag(
                        ventaBbNumber = f["ventaBbNumber"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                        ventaBbWeight = f["ventaBbWeight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                    )
                } catch (e: Exception) {
                    println("⚠️ Error parseando un bigbag: ${e.message}")
                    null
                }
            } ?: emptyList()

        // NUEVO: lectura de ventaPesoTotal
        val ventaPesoTotal = fields["ventaPesoTotal"]?.jsonObject
            ?.get("stringValue")?.jsonPrimitive?.contentOrNull

        return Venta(
            ventaCliente = fields["ventaCliente"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            ventaLote = fields["ventaLote"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            ventaMaterial = fields["ventaMaterial"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            ventaFecha = fields["ventaFecha"]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.contentOrNull?.let {
                Instant.parse(it)
            },
            ventaBigbags = ventaBigbags,
            ventaPesoTotal = ventaPesoTotal
        )
    }
}