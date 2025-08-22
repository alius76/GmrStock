package com.alius.gmrstock.data.mappers

import com.alius.gmrstock.domain.model.Process
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.datetime.Instant
import kotlinx.serialization.json.jsonObject

object ProcessMapper {

    fun fromFirestore(fields: JsonObject): Process {
        return Process(
            number = fields["number"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            description = fields["description"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
            date = fields["date"]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.contentOrNull?.let {
                Instant.parse(it)
            }
        )
    }
}


