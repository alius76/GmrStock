package com.alius.gmrstock.data.mappers

import com.alius.gmrstock.domain.model.Ratio
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.datetime.Instant
import kotlinx.serialization.json.jsonObject

object RatioMapper {

    fun fromFirestore(fields: JsonObject): Ratio {
        return Ratio(
            ratioId = fields["ratioId"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            ratioDate = fields["ratioDate"]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.contentOrNull
                ?.let { Instant.parse(it).toEpochMilliseconds() } ?: 0L,
            ratioTotalWeight = fields["ratioTotalWeight"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "0",
            // ✅ Añadimos el nuevo campo para que coincida con el modelo actualizado
            ratioLoteId = fields["ratioLoteId"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
        )
    }
}
