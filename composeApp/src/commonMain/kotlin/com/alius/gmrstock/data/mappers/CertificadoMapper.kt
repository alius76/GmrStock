package com.alius.gmrstock.data.mappers

import com.alius.gmrstock.domain.model.Certificado
import com.alius.gmrstock.domain.model.Propiedad
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*

object CertificadoMapper {

    fun fromFirestore(fields: JsonObject): Certificado {
        val loteNumber = fields["loteNumber"]
            ?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()

        val fecha = fields["fecha"]
            ?.jsonObject?.get("timestampValue")
            ?.jsonPrimitive?.content
            ?.let { Instant.parse(it) }

        val status = fields["status"]
            ?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "c"

        val propiedadesArray = fields["propiedades"]
            ?.jsonObject?.get("arrayValue")
            ?.jsonObject?.get("values")
            ?.jsonArray

        val propiedades = propiedadesArray?.mapNotNull { element ->
            try {
                val propFields = element.jsonObject["mapValue"]
                    ?.jsonObject?.get("fields")
                    ?.jsonObject

                propFields?.let {
                    Propiedad(
                        nombre = it["nombre"]?.jsonObject
                            ?.get("stringValue")?.jsonPrimitive?.content.orEmpty(),
                        valor = it["valor"]?.jsonObject
                            ?.get("stringValue")?.jsonPrimitive?.content.orEmpty(),
                        warning = it["warning"]?.jsonObject
                            ?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: false
                    )
                }
            } catch (e: Exception) {
                println("⚠️ Error parseando propiedad: ${e.message}")
                null
            }
        } ?: emptyList()

        return Certificado(
            loteNumber = loteNumber,
            fecha = fecha,
            status = status,
            propiedades = propiedades
        )
    }
}
