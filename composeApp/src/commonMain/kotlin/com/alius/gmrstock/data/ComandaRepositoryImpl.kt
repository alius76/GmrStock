package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.*
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.Comanda
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


class ComandaRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : ComandaRepository {

    // ------------------ Helpers ------------------

    private suspend fun ejecutarQuery(query: String): List<Comanda> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.post(baseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }
            // Asumo que parseRunQueryResponseComanda está definido en firestore.*
            parseRunQueryResponseComanda(response.bodyAsText())
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun buildDocumentBaseUrl(): String =
        baseUrl.substringBeforeLast(":runQuery")

    // ------------------ 🔥 Generador incremental ------------------

    private suspend fun obtenerSiguienteNumeroDeComanda(): Long = withContext(Dispatchers.IO) {

        val url = "${buildDocumentBaseUrl()}/metadata/comanda_counter"

        try {
            val response = client.get(url)
            val jsonElement = Json.parseToJsonElement(response.bodyAsText())

            val fields = jsonElement.jsonObject["fields"]?.jsonObject
            val ultimoStr = fields
                ?.get("ultimo")
                ?.jsonObject
                ?.get("integerValue")
                ?.jsonPrimitive
                ?.content

            val ultimo = ultimoStr?.toLongOrNull() ?: 0L
            val nuevo = ultimo + 1

            // Actualizamos contador
            val patchBody = """
            {
              "fields": {
                "ultimo": { "integerValue": $nuevo }
              }
            }
        """.trimIndent()

            client.patch(url) {
                url {
                    parameters.append("updateMask.fieldPaths", "ultimo")
                }
                headers { append("Content-Type", "application/json") }
                setBody(patchBody)
            }

            nuevo

        } catch (_: Exception) {
            1L // fallback seguro
        }
    }

    // ------------------ Queries ------------------

    private fun buildQueryPorFecha(fecha: String): String {
        val fechaInicio = "${fecha}T00:00:00Z"
        val fechaFin = "${fecha}T23:59:59Z"

        return """
        {
            "structuredQuery": {
                "from": [{ "collectionId": "comanda" }],
                "where": {
                    "compositeFilter": {
                        "op": "AND",
                        "filters": [
                            {
                                "fieldFilter": {
                                    "field": { "fieldPath": "dateBookedComanda" },
                                    "op": "GREATER_THAN_OR_EQUAL",
                                    "value": { "timestampValue": "$fechaInicio" }
                                }
                            },
                            {
                                "fieldFilter": {
                                    "field": { "fieldPath": "dateBookedComanda" },
                                    "op": "LESS_THAN_OR_EQUAL",
                                    "value": { "timestampValue": "$fechaFin" }
                                }
                            }
                        ]
                    }
                },
                "orderBy": [
                    { "field": { "fieldPath": "dateBookedComanda" }, "direction": "ASCENDING" }
                ]
            }
        }
        """.trimIndent()
    }

    // 🆕 FUNCIÓN DE CONSULTA DE COMANDAS PENDIENTES
    // NOTA: Esta función requiere que 'buildQueryPendingComandasByClient' esté definida en tu módulo 'com.alius.gmrstock.data.firestore'
    override suspend fun getPendingComandasByClient(clientName: String): List<Comanda> {
        val query = buildQueryPendingComandasByClient(clientName)
        return ejecutarQuery(query)
    }

    // ------------------ Funciones CRUD ------------------

    override suspend fun listarComandas(filter: String): List<Comanda> =
        ejecutarQuery(buildQueryPorFecha(filter))

    override suspend fun getComandaByNumber(number: String): Comanda? =
        ejecutarQuery(buildQueryPorNumeroExacto(number, collection = "comanda")).firstOrNull()

    override suspend fun addComanda(comanda: Comanda): Boolean = withContext(Dispatchers.IO) {
        try {
            val numeroNuevo = obtenerSiguienteNumeroDeComanda()

            val docUrl = "${buildDocumentBaseUrl()}/comanda"

            val requestBody = buildPostBodyForComanda(
                comanda.copy(numeroDeComanda = numeroNuevo)
            )

            val response: HttpResponse = client.post(docUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(requestBody)
            }

            response.status.isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun updateComandaRemark(comandaId: String, newRemark: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val docUrl = "${buildDocumentBaseUrl()}/comanda/$comandaId"
                val requestBody = buildPatchBodyForRemark(newRemark)

                val response: HttpResponse = client.patch(docUrl) {
                    url.parameters.append("updateMask.fieldPaths", "remarkComanda")
                    headers { append("Content-Type", "application/json") }
                    setBody(requestBody)
                }
                response.status.isSuccess()
            } catch (_: Exception) {
                false
            }
        }

    override suspend fun updateComandaBooked(
        comandaId: String,
        cliente: Cliente?,
        dateBooked: Instant?,
        bookedRemark: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val docUrl = "${buildDocumentBaseUrl()}/comanda/$comandaId"
            val requestBody =
                buildPatchBodyForBooked(cliente, dateBooked, null, bookedRemark)

            val response: HttpResponse = client.patch(docUrl) {
                url.parameters.append("updateMask.fieldPaths", "bookedClientComanda")
                url.parameters.append("updateMask.fieldPaths", "dateBookedComanda")
                url.parameters.append("updateMask.fieldPaths", "bookedRemarkComanda")
                headers { append("Content-Type", "application/json") }
                setBody(requestBody)
            }
            response.status.isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun updateComandaDate(
        comandaId: String,
        dateBooked: Instant
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val docUrl = "${buildDocumentBaseUrl()}/comanda/$comandaId"
            val requestBody = """
            {
                "fields": {
                    "dateBookedComanda": { "timestampValue": "${dateBooked}" }
                }
            }
        """.trimIndent()

            val response: HttpResponse = client.patch(docUrl) {
                url.parameters.append("updateMask.fieldPaths", "dateBookedComanda")
                headers { append("Content-Type", "application/json") }
                setBody(requestBody)
            }

            response.status.isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    // 🆕 IMPLEMENTACIÓN: Asignación del número de lote a la comanda
    override suspend fun updateComandaLoteNumber(comandaId: String, loteNumber: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val docUrl = "${buildDocumentBaseUrl()}/comanda/$comandaId"
            val requestBody = """
            {
                "fields": {
                    "numberLoteComanda": { "stringValue": "$loteNumber" }
                }
            }
            """.trimIndent()

            val response: HttpResponse = client.patch(docUrl) {
                url.parameters.append("updateMask.fieldPaths", "numberLoteComanda")
                headers { append("Content-Type", "application/json") }
                setBody(requestBody)
            }

            println("✅ [PATCH Comanda] Lote $loteNumber asignado a comanda $comandaId. Status: ${response.status}")
            response.status.isSuccess()
        } catch (e: Exception) {
            println("❌ Error en updateComandaLoteNumber: ${e.message}")
            false
        }
    }


    override suspend fun deleteComanda(comandaId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val docUrl = "${buildDocumentBaseUrl()}/comanda/$comandaId"

                val response: HttpResponse = client.delete(docUrl) {
                    headers { append("Content-Type", "application/json") }
                }

                response.status.isSuccess()
            } catch (_: Exception) {
                false
            }
        }
}