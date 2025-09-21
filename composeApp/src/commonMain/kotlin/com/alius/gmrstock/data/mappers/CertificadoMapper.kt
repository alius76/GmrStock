package com.alius.gmrstock.data.mappers

import com.alius.gmrstock.domain.model.*
import kotlinx.datetime.Instant
import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds
import kotlinx.serialization.json.*

object CertificadoMapper {

    fun fromFirestore(fields: JsonObject): Certificado? {
        // Obtenemos los campos de Firestore de forma segura
        val loteNumber = fields["loteNumber"]
            ?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()

        val fechaString = fields["fecha"]
            ?.jsonObject?.get("timestampValue")?.jsonPrimitive?.content

        val fecha = fechaString?.let { Instant.parse(it) }

        val statusString = fields["status"]
            ?.jsonObject?.get("stringValue")?.jsonPrimitive?.content
            ?.uppercase() // 🚨 Importante: convertimos a mayúsculas para el enum

        // Mapeo seguro del String del JSON a nuestro enum
        val status = when (statusString) {
            "CORRECTO" -> CertificadoStatus.CORRECTO
            "ADVERTENCIA" -> CertificadoStatus.ADVERTENCIA
            else -> CertificadoStatus.SIN_DATOS
        }

        val parametrosArray = fields["parametros"]
            ?.jsonObject?.get("arrayValue")
            ?.jsonObject?.get("values")
            ?.jsonArray

        val parametros = parametrosArray?.mapNotNull { element ->
            val parametroMap = element.jsonObject["mapValue"]
                ?.jsonObject?.get("fields")
                ?.jsonObject

            parametroMap?.let { toParametro(it) }
        } ?: emptyList()

        // Si no hay loteNumber, consideramos que el documento no es válido
        return if (loteNumber.isNotEmpty()) {
            Certificado(
                loteNumber = loteNumber,
                fecha = fecha,
                status = status,
                parametros = parametros
            )
        } else {
            null
        }
    }

    private fun toParametro(firestoreData: JsonObject): Parametro {
        val descripcion = firestoreData["descripcion"]
            ?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()

        val valor = firestoreData["valor"]
            ?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()

        val warning = firestoreData["warning"]
            ?.jsonObject?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: false

        val rangoObject = firestoreData["rango"]
            ?.jsonObject?.get("mapValue")
            ?.jsonObject?.get("fields")
            ?.jsonObject

        val rango = rangoObject?.let {
            Rango(
                valorMin = it["valorMin"]
                    ?.jsonObject?.get("doubleValue")?.jsonPrimitive?.doubleOrNull,
                valorMax = it["valorMax"]
                    ?.jsonObject?.get("doubleValue")?.jsonPrimitive?.doubleOrNull
            )
        }

        val unidad = firestoreData["unidad"]
            ?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()

        val code = firestoreData["code"]
            ?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()


        return Parametro(
            descripcion = descripcion,
            rango = rango,
            unidad = unidad,
            code = code,
            valor = valor,
            warning = warning
        )
    }
}