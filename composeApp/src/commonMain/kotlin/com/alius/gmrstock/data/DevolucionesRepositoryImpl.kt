package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.parseRunQueryResponse
import com.alius.gmrstock.data.firestore.buildQueryPorNumeroExacto
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.BigBags
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

// Este repo trabaja con la coleccion de lote y no con la colección de devolucion

class DevolucionesRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : DevolucionesRepository {

    private suspend fun ejecutarQuery(query: String): List<LoteModel> = withContext(Dispatchers.IO) {
        // ... (Implementación de ejecutarQuery se mantiene)
        try {
            println("🌐 POST $baseUrl")
            println("📤 Body: $query")

            val response: HttpResponse = client.post(baseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            parseRunQueryResponse(response.bodyAsText())
        } catch (e: Exception) {
            println("❌ Error en ejecutarQuery Devoluciones: ${e.message}")
            emptyList()
        }
    }

    private fun buildDocumentBaseUrl(): String = baseUrl.substringBeforeLast(":runQuery")

    override suspend fun getLoteByNumber(number: String): LoteModel? {
        val query = buildQueryPorNumeroExacto(number)
        return ejecutarQuery(query).firstOrNull()
    }

    /**
     * ⚠️ NOTA: Se mantiene la función devolverBigBag para compatibilidad, pero DEBE ser
     * eliminada o refactorizada si ya no se usa.
     */
    override suspend fun devolverBigBag(loteNumber: String, bigBagNumber: String): Boolean {
        // Llama a la nueva función pasando el BigBag individual en una lista
        return devolverBigBags(loteNumber, listOf(bigBagNumber))
    }

    // 🌟 FUNCIÓN DEVOLUCIÓN MÚLTIPLE: Devuelve una lista de BigBags con una sola petición PATCH
    override suspend fun devolverBigBags(loteNumber: String, bigBagNumbers: List<String>): Boolean =
        withContext(Dispatchers.IO) {
            if (bigBagNumbers.isEmpty()) return@withContext true

            val lote = getLoteByNumber(loteNumber) ?: run {
                println("❌ Lote $loteNumber no encontrado")
                return@withContext false
            }

            // 1. Calcular los cambios de stock y peso
            val returnedBigBags = lote.bigBag.filter { bigBagNumbers.contains(it.bbNumber) }
            val totalWeightReturned = returnedBigBags.sumOf { it.bbWeight.toDoubleOrNull() ?: 0.0 }

            val newCount = (lote.count.toIntOrNull() ?: 0) + bigBagNumbers.size
            val newTotalWeight = (lote.totalWeight.toDoubleOrNull() ?: 0.0) + totalWeightReturned

            // 2. Actualizar el estado de la lista BigBag localmente
            val updatedBigBags = lote.bigBag.map { bb ->
                if (bigBagNumbers.contains(bb.bbNumber)) {
                    // Actualiza el estado a 's' (stock) y la marca (DEVO)
                    bb.copy(bbStatus = "s", bbRemark = "DEVO")
                } else {
                    bb // Mantiene el BigBag sin cambios
                }
            }

            // 3. Construir el cuerpo de la petición PATCH
            val requestBody = buildPatchBodyOptimized(updatedBigBags, newCount, newTotalWeight)

            val docUrl = "${buildDocumentBaseUrl()}/lote/${lote.id}"

            try {
                println("🌐 PATCH $docUrl (Devolución Múltiple)")
                println("📤 Body (actualiza ${bigBagNumbers.size} BBs): $requestBody")

                val response: HttpResponse = client.patch(docUrl) {
                    // ✅ IMPORTANTE: Se debe especificar qué campos se van a actualizar
                    url.parameters.append("updateMask.fieldPaths", "bigBag")
                    url.parameters.append("updateMask.fieldPaths", "count")
                    url.parameters.append("updateMask.fieldPaths", "totalWeight")
                    headers { append("Content-Type", ContentType.Application.Json) }
                    setBody(requestBody)
                }

                if (response.status.isSuccess()) {
                    println("✅ ${bigBagNumbers.size} BigBag(s) del lote ${lote.number} devueltos correctamente.")
                    true
                } else {
                    println("❌ Error al devolver BigBag(s) del lote ${lote.number}. Status: ${response.status}")
                    println("Response Body: ${response.bodyAsText()}")
                    false
                }
            } catch (e: Exception) {
                println("❌ Error en devolverBigBags: ${e.message}")
                false
            }
        }

    // ==========================================
    // 🌟 Implementación corregida del JSON para PATCH
    // ==========================================
    private fun buildPatchBodyOptimized(
        bigBags: List<BigBags>,
        count: Int,
        totalWeight: Double
    ): String {
        // Función auxiliar para formatear Double sin decimales si es un entero (según Firestore StringValue)
        fun doubleToStringSafe(value: Double): String =
            if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

        val countString = count.toString()
        val totalWeightString = doubleToStringSafe(totalWeight)

        return buildJsonObject {
            putJsonObject("fields") {
                // 1. bigBag (Array de MapValues)
                putJsonObject("bigBag") {
                    putJsonObject("arrayValue") {
                        putJsonArray("values") {
                            bigBags.forEach { bb ->
                                val bbWeightString = doubleToStringSafe(bb.bbWeight.toDoubleOrNull() ?: 0.0)
                                add(buildJsonObject {
                                    putJsonObject("mapValue") {
                                        putJsonObject("fields") {
                                            put("bbNumber", buildJsonObject { put("stringValue", bb.bbNumber) })
                                            put("bbWeight", buildJsonObject { put("stringValue", bbWeightString) })
                                            put("bbLocation", buildJsonObject { put("stringValue", bb.bbLocation) })
                                            put("bbStatus", buildJsonObject { put("stringValue", bb.bbStatus) })
                                            // Se asegura que bbRemark no sea nulo al construir el JSON
                                            put("bbRemark", buildJsonObject { put("stringValue", bb.bbRemark) })
                                        }
                                    }
                                })
                            }
                        }
                    }
                }

                // 2. count
                putJsonObject("count") { put("stringValue", countString) }

                // 3. totalWeight
                putJsonObject("totalWeight") { put("stringValue", totalWeightString) }
            }
        }.toString()
    }
}