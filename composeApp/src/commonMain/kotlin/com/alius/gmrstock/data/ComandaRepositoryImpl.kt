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
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.days


class ComandaRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : ComandaRepository {

    // ------------------ Helpers ------------------

    private suspend fun ejecutarQuery(query: String): List<Comanda> = withContext(Dispatchers.IO) {
        println(">>> [REPO] Ejecutando Query en Firestore...")
        try {
            val response: HttpResponse = client.post(baseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }
            val responseBody = response.bodyAsText()

            // Asumo que parseRunQueryResponseComanda está definido en firestore.*
            val comandasList = parseRunQueryResponseComanda(responseBody)
            println(">>> [REPO] Query exitosa. Documentos parseados: ${comandasList.size}")
            return@withContext comandasList
        } catch (e: Exception) {
            println(">>> [REPO] ❌ ERROR al ejecutar/parsear Query: ${e.message}")
            emptyList()
        }
    }

    private fun buildDocumentBaseUrl(): String =
        baseUrl.substringBeforeLast(":runQuery")

    // ------------------ 🔥 Generador incremental ------------------

    private suspend fun obtenerSiguienteNumeroDeComanda(): Long = withContext(Dispatchers.IO) {

        val url = "${buildDocumentBaseUrl()}/metadata/comanda_counter"
        println(">>> [REPO] Intentando obtener siguiente número de comanda desde: $url")

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

            println(">>> [REPO] Último número: $ultimo. Nuevo número: $nuevo")

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
            println(">>> [REPO] Contador de comanda actualizado a $nuevo.")

            nuevo

        } catch (e: Exception) {
            println(">>> [REPO] ❌ ERROR al obtener/actualizar contador de comanda: ${e.message}")
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

    // 🔥 NUEVA FUNCIÓN DE QUERY: Filtra por una fecha límite inferior
    private fun buildQueryComandasDesdeFecha(fechaLimite: Instant): String {
        val fechaIso = fechaLimite.toString()
        println(">>> [REPO] ✅ Ejecutando Query: Comandas desde $fechaIso (hace 1 mes).")
        return """
        {
            "structuredQuery": {
                "from": [{ "collectionId": "comanda" }],
                "where": {
                    "fieldFilter": {
                        "field": { "fieldPath": "dateBookedComanda" },
                        "op": "GREATER_THAN_OR_EQUAL",
                        "value": { "timestampValue": "$fechaIso" }
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
    override suspend fun getPendingComandasByClient(clientName: String): List<Comanda> {
        // Asumiendo que esta función existe en tu capa de datos Firestore
        val query = buildQueryPendingComandasByClient(clientName)
        return ejecutarQuery(query)
    }

    // ------------------ Funciones CRUD ------------------

    override suspend fun listarComandas(filter: String): List<Comanda> =
        ejecutarQuery(buildQueryPorFecha(filter))

    override suspend fun listarTodasComandas(): List<Comanda> {
        // 1. Obtener la fecha de hoy en la zona horaria del sistema
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

        // 2. Calcular el primer día del mes anterior (estricto "día 1")
        val monthAgo = today.minus(DatePeriod(months = 1))
        val firstDayOfMonthAgo = LocalDate(
            year = monthAgo.year,
            monthNumber = monthAgo.monthNumber,
            dayOfMonth = 1
        )

        // 3. Convertir el inicio de ese día a Instant (usando la medianoche UTC)
        val fechaLimite = firstDayOfMonthAgo.atStartOfDayIn(TimeZone.UTC)

        // 4. Ejecutar la query con el nuevo límite
        return ejecutarQuery(buildQueryComandasDesdeFecha(fechaLimite))
    }

    override suspend fun getComandaByNumber(number: String): Comanda? =
        ejecutarQuery(buildQueryPorNumeroExacto(number, collection = "comanda")).firstOrNull()

    override suspend fun addComanda(comanda: Comanda): Boolean = withContext(Dispatchers.IO) {
        try {
            val numeroNuevo = obtenerSiguienteNumeroDeComanda()
            println(">>> [REPO] Intentando agregar Comanda #${numeroNuevo}...")

            val docUrl = "${buildDocumentBaseUrl()}/comanda"

            val requestBody = buildPostBodyForComanda(
                comanda.copy(numeroDeComanda = numeroNuevo)
            )

            val response: HttpResponse = client.post(docUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(requestBody)
            }

            val success = response.status.isSuccess()
            println(">>> [REPO] Resultado addComanda: $success (Status: ${response.status})")
            success
        } catch (e: Exception) {
            println(">>> [REPO] ❌ ERROR en addComanda: ${e.message}")
            false
        }
    }

    override suspend fun updateComandaRemark(comandaId: String, newRemark: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val docUrl = "${buildDocumentBaseUrl()}/comanda/$comandaId"

                val requestBody = """
                {
                    "fields": {
                        "remarkComanda": { "stringValue": "$newRemark" }
                    }
                }
            """.trimIndent()

                println(">>> [REPO] Intentando actualizar remark para $comandaId. Body: $requestBody")

                val response: HttpResponse = client.patch(docUrl) {
                    // 🔑 IMPORTANTE: Solo actualizamos el campo 'remarkComanda'
                    url.parameters.append("updateMask.fieldPaths", "remarkComanda")
                    headers { append("Content-Type", "application/json") }
                    setBody(requestBody)
                }
                val success = response.status.isSuccess()
                println(">>> [REPO] Resultado updateComandaRemark: $success (Status: ${response.status})")
                success
            } catch (e: Exception) {
                println(">>> [REPO] ❌ ERROR en updateComandaRemark: ${e.message}")
                false
            }
        }

    override suspend fun getComandaByLoteNumber(loteNumber: String): Comanda? {
        // Si el número es vacío, no buscamos nada
        if (loteNumber.isBlank()) return null

        val query = buildQueryPorLoteExacto(loteNumber)
        // Usamos tu función ejecutarQuery que ya maneja el POST y el parseo
        return ejecutarQuery(query).firstOrNull()
    }

    // Función auxiliar para construir el JSON de Firestore
    private fun buildQueryPorLoteExacto(loteNumber: String): String {
        return """
    {
        "structuredQuery": {
            "from": [{ "collectionId": "comanda" }],
            "where": {
                "fieldFilter": {
                    "field": { "fieldPath": "numberLoteComanda" },
                    "op": "EQUAL",
                    "value": { "stringValue": "$loteNumber" }
                }
            },
            "limit": 1
        }
    }
    """.trimIndent()
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
            println(">>> [REPO] Intentando actualizar booked data para $comandaId...")


            val response: HttpResponse = client.patch(docUrl) {
                url.parameters.append("updateMask.fieldPaths", "bookedClientComanda")
                url.parameters.append("updateMask.fieldPaths", "dateBookedComanda")
                url.parameters.append("updateMask.fieldPaths", "bookedRemarkComanda")
                headers { append("Content-Type", "application/json") }
                setBody(requestBody)
            }
            val success = response.status.isSuccess()
            println(">>> [REPO] Resultado updateComandaBooked: $success (Status: ${response.status})")
            success
        } catch (e: Exception) {
            println(">>> [REPO] ❌ ERROR en updateComandaBooked: ${e.message}")
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
            println(">>> [REPO] Intentando actualizar fecha para $comandaId a $dateBooked...")

            val response: HttpResponse = client.patch(docUrl) {
                url.parameters.append("updateMask.fieldPaths", "dateBookedComanda")
                headers { append("Content-Type", "application/json") }
                setBody(requestBody)
            }

            val success = response.status.isSuccess()
            println(">>> [REPO] Resultado updateComandaDate: $success (Status: ${response.status})")
            success
        } catch (e: Exception) {
            println(">>> [REPO] ❌ ERROR en updateComandaDate: ${e.message}")
            false
        }
    }

    // 🆕 IMPLEMENTACIÓN: Asignación del número de lote a la comanda
    override suspend fun updateComandaLoteNumber(comandaId: String, loteNumber: String): Boolean =
        withContext(Dispatchers.IO) {
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
                println(">>> [REPO] Intentando eliminar comanda $comandaId...")

                val response: HttpResponse = client.delete(docUrl) {
                    headers { append("Content-Type", "application/json") }
                }

                val success = response.status.isSuccess()
                println(">>> [REPO] Resultado deleteComanda: $success (Status: ${response.status})")
                success
            } catch (e: Exception) {
                println(">>> [REPO] ❌ ERROR en deleteComanda: ${e.message}")
                false
            }
        }
}