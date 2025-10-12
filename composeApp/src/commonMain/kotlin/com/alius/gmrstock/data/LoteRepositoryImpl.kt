package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.buildPatchBodyForRemark
import com.alius.gmrstock.data.firestore.buildPatchBodyForBooked
import com.alius.gmrstock.data.firestore.buildQueryPorFecha
import com.alius.gmrstock.data.firestore.buildQueryPorNumero
import com.alius.gmrstock.data.firestore.buildQueryPorNumeroExacto
import com.alius.gmrstock.data.firestore.buildQueryUltimosLotes
import com.alius.gmrstock.data.firestore.parseRunQueryResponse
import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.MaterialGroup
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.http.*
import kotlinx.coroutines.IO
import kotlinx.datetime.*


class LoteRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : LoteRepository {

    /**
     * Función genérica para ejecutar una query contra Firestore
     */
    private suspend fun ejecutarQuery(query: String): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            println("🌐 POST $baseUrl")
            println("📤 Body: $query")

            val response: HttpResponse = client.post(baseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            parseRunQueryResponse(responseText)
        } catch (e: Exception) {
            println("❌ Error en ejecutarQuery: ${e.message}")
            emptyList()
        }
    }

    // 🆕 Función auxiliar para construir la URL base de documentos
    private fun buildDocumentBaseUrl(): String {
        // Asume que la URL de query es: .../documents:runQuery
        // Y la URL base de documentos es: .../documents
        return baseUrl.substringBeforeLast(":runQuery")
    }

    override suspend fun listarLotes(data: String): List<LoteModel> {
        val query = buildQueryPorNumero(data)
        return ejecutarQuery(query)
    }

    override suspend fun listarGruposPorDescripcion(filter: String): List<MaterialGroup> {
        val lotes = listarLotes(filter)
        return agruparPorMaterial(lotes)
    }

    override suspend fun getLoteByNumber(number: String): LoteModel? {
        val query = buildQueryPorNumeroExacto(number)
        return ejecutarQuery(query).firstOrNull()
    }

    override suspend fun listarLotesCreadosHoy(): List<LoteModel> {
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val inicioDeHoy = hoy.atStartOfDayIn(TimeZone.currentSystemDefault())
        val inicioDeManana = inicioDeHoy.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

        val query = buildQueryPorFecha(inicioDeHoy, inicioDeManana)
        return ejecutarQuery(query)
    }

    override suspend fun listarLotesPorFecha(fecha: LocalDate): List<LoteModel> {
        val inicio = fecha.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fin = inicio.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

        val query = buildQueryPorFecha(inicio, fin)
        return ejecutarQuery(query)
    }

    override suspend fun listarUltimosLotes(cantidad: Int): List<LoteModel> {
        val query = buildQueryUltimosLotes(cantidad)
        return ejecutarQuery(query)
    }

    // =========================================================================
    // IMPLEMENTACIÓN DE ESCRITURA: UPDATE REMARK
    // =========================================================================
    override suspend fun updateLoteRemark(loteId: String, newRemark: String): Boolean = withContext(Dispatchers.IO) {
        val docUrl = "${buildDocumentBaseUrl()}/lote/$loteId"
        val requestBody = buildPatchBodyForRemark(newRemark)

        try {
            println("🌐 PATCH $docUrl?updateMask.fieldPaths=remark")
            println("📤 Body: $requestBody")

            val response: HttpResponse = client.patch(docUrl) {
                url.parameters.append("updateMask.fieldPaths", "remark")
                headers { append("Content-Type", "application/json") }
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                println("✅ [PATCH] Observación del lote $loteId actualizada correctamente.")
                return@withContext true
            } else {
                println("❌ [PATCH] Error al actualizar la observación del lote $loteId. Status: ${response.status}")
                println("Response Body: ${response.bodyAsText()}")
                return@withContext false
            }

        } catch (e: Exception) {
            println("❌ Error en updateLoteRemark: ${e.message}")
            return@withContext false
        }
    }

    // =========================================================================
// IMPLEMENTACIÓN DE ESCRITURA: UPDATE BOOKED
// =========================================================================
    override suspend fun updateLoteBooked(
        loteId: String,
        cliente: Cliente?,
        dateBooked: Instant?,
        bookedByUser: String?,
        bookedRemark: String?

    ): Boolean = withContext(Dispatchers.IO) {
        val docUrl = "${buildDocumentBaseUrl()}/lote/$loteId"

        // ⬇️ Pasar todos los campos al constructor del cuerpo JSON ⬇️
        val requestBody = buildPatchBodyForBooked(cliente, dateBooked, bookedByUser, bookedRemark)

        try {
            // Construir la URL con la máscara de actualización.
            // TODOS los campos de reserva deben estar siempre en la máscara,
            // ya sea para actualizar su valor o para borrarlo (enviando 'null').

            // ❌ ELIMINAMOS la comprobación `if (bookedByUser != null)` etc.

            println("🌐 PATCH $docUrl?updateMask.fieldPaths=booked&updateMask.fieldPaths=dateBooked&updateMask.fieldPaths=bookedByUser&updateMask.fieldPaths=bookedRemark")
            println("📤 Body: $requestBody")

            val response: HttpResponse = client.patch(docUrl) {
                // Configurar la URL con TODOS los campos de reserva.
                url.parameters.append("updateMask.fieldPaths", "booked")
                url.parameters.append("updateMask.fieldPaths", "dateBooked")

                // ✅ CORRECCIÓN: Incluimos los nuevos campos SIEMPRE en la máscara
                url.parameters.append("updateMask.fieldPaths", "bookedByUser")
                url.parameters.append("updateMask.fieldPaths", "bookedRemark")
                // -----------------------------------------------------------

                headers { append("Content-Type", ContentType.Application.Json) }
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                println("✅ [PATCH] Reserva del lote $loteId actualizada correctamente.")
                return@withContext true
            } else {
                println("❌ [PATCH] Error al actualizar la reserva del lote $loteId. Status: ${response.status}")
                println("Response Body: ${response.bodyAsText()}")
                return@withContext false
            }

        } catch (e: Exception) {
            println("❌ Error en updateLoteBooked: ${e.message}")
            return@withContext false
        }
    }
}
