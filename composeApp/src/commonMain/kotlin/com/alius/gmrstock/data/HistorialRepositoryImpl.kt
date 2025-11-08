package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.buildQueryHistorialDeHoy
import com.alius.gmrstock.data.firestore.buildQueryPorNumeroExacto
import com.alius.gmrstock.data.firestore.buildCreateBodyForLote
import com.alius.gmrstock.data.firestore.parseRunQueryResponse
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.data.HistorialRepository
// 🚨 Importaciones necesarias para la nueva función:

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.* // Necesario para buildJsonObject y Json

class HistorialRepositoryImpl(
    private val client: HttpClient,
    private val firestoreRunQueryUrl: String // Url para :runQuery
) : HistorialRepository {

    // 🚨 Configuración de Json (Ajusta según tu implementación global de Kotlinx Serialization)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Extrae la URL base del proyecto (sin :runQuery ni ?key=...)
     */
    private fun buildDocumentBaseUrlWithoutKey(): String = firestoreRunQueryUrl.substringBeforeLast(":runQuery")

    /**
     * 🛑 FUNCIÓN CLAVE: Extrae solo el valor de la API Key (lo que va después de ?key=)
     */
    private fun extractApiKey(): String {
        // Busca el valor después de "?key=" en la URL completa, ignorando otros parámetros (&).
        return firestoreRunQueryUrl.substringAfter("?key=", "").substringBefore("&").trim()
    }

    /**
     * Función genérica para ejecutar una query contra Firestore
     */
    private suspend fun ejecutarQuery(query: String): List<LoteModel> = withContext(Dispatchers.IO) {
        // ... (Tu implementación existente de consulta se mantiene)
        try {
            println("🌐 POST $firestoreRunQueryUrl")
            println("📤 Body: $query")

            val response: HttpResponse = client.post(firestoreRunQueryUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            parseRunQueryResponse(responseText)
        } catch (e: Exception) {
            println("❌ Error en ejecutarQuery para Historial: ${e.message}")
            emptyList()
        }
    }

    override suspend fun listarLotesHistorialDeHoy(): List<LoteModel> {
        val query = buildQueryHistorialDeHoy()
        return ejecutarQuery(query)
    }

    // ----------------------------------------------------
    // 🚀 IMPLEMENTACIONES DE RESURRECCIÓN
    // ----------------------------------------------------

    override suspend fun getLoteHistorialByNumber(number: String): LoteModel? {
        // Usamos la nueva función de query con la colección "historial"
        val query = buildQueryPorNumeroExacto(number, collection = "historial")
        return ejecutarQuery(query).firstOrNull()
    }

    // ----------------------------------------------------
    // FUNCIÓN ORIGINAL (SE MANTIENE)
    // ----------------------------------------------------
    override suspend fun agregarLote(lote: LoteModel): Boolean = withContext(Dispatchers.IO) {

        // 1. URL para la colección 'lote' (para que Firebase genere un ID nuevo)
        val collectionUrl = "${buildDocumentBaseUrlWithoutKey()}/lote"

        // 2. Obtenemos la API Key y el cuerpo del lote
        val apiKey = extractApiKey()
        // 🚨 buildCreateBodyForLote DEBE incluir el campo 'id' (aunque sea placeholder)
        val requestBody = buildCreateBodyForLote(lote)

        try {
            // 🛑 CRÍTICO: Usamos POST a la colección
            println("🌐 POST $collectionUrl (Creando nuevo Lote en Stock, número: ${lote.number})")

            val response: HttpResponse = client.post(collectionUrl) {

                // 🛑 SOLUCIÓN AL 404: Añadir la clave de API como parámetro
                if (apiKey.isNotEmpty()) {
                    url.parameters.append("key", apiKey)
                }

                headers { append("Content-Type", ContentType.Application.Json) }
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                println("✅ Lote ${lote.number} replicado exitosamente a la colección 'lote' con nuevo ID.")
                true
            } else {
                val errorBody = response.bodyAsText()
                println("❌ ERROR FIREBASE: La solicitud POST falló con Status: ${response.status}")
                println("❌ ERROR FIREBASE: Cuerpo de Respuesta de Error: $errorBody")
                false
            }
        } catch (e: Exception) {
            println("❌ Error en agregarLote (POST Lote): ${e.message}")
            false
        }
    }


// ----------------------------------------------------
// 🚀 NUEVA FUNCIÓN: POST + PATCH (Solución 1: Corregir ID)
// ----------------------------------------------------

    /**
     * Crea un nuevo documento 'lote' usando POST (Firebase genera el ID),
     * y luego usa PATCH para actualizar el campo de datos 'id' con el ID real generado.
     * @return El ID real del documento creado (String) o null en caso de error.
     */
    override suspend fun agregarYLigaroLote(lote: LoteModel): String? = withContext(Dispatchers.IO) {

        val collectionUrl = "${buildDocumentBaseUrlWithoutKey()}/lote"
        val apiKey = extractApiKey()
        // buildCreateBodyForLote debe incluir el campo "id" para evitar el error de esquema.
        val requestBody = buildCreateBodyForLote(lote)

        try {
            // --- 1. POST: Crear el documento (Firebase genera el ID real) ---
            val postResponse: HttpResponse = client.post(collectionUrl) {
                if (apiKey.isNotEmpty()) url.parameters.append("key", apiKey)
                headers { append("Content-Type", ContentType.Application.Json) }
                setBody(requestBody)
            }

            if (!postResponse.status.isSuccess()) {
                // Mantenemos esta impresión para errores críticos de la creación inicial
                val postErrorBody = postResponse.bodyAsText()
                println("❌ ERROR FIREBASE (POST): Falló la creación inicial. Status: ${postResponse.status}. Body: $postErrorBody")
                return@withContext null
            }

            // --- 2. CAPTURA: Obtener el ID real generado ---
            val responseText = postResponse.bodyAsText()
            val docResponse = json.decodeFromString<FirebaseDocument>(responseText)
            val documentPath = docResponse.name
            val newDocumentId = documentPath.substringAfterLast("/")

            // 🚨 PASO DE CORRECCIÓN: Calcular la URL completa del PATCH
            val baseUrlApi = firestoreRunQueryUrl.substringBefore("/v1/") + "/v1"
            val fullPatchUrl = "$baseUrlApi/$documentPath"

            // --- 3. PATCH: Actualizar el campo de datos 'id' interno ---

            // 3.1. Cuerpo del PATCH: SOLO contiene 'fields', sin 'updateMask'
            val patchBody = buildJsonObject {
                putJsonObject("fields") {
                    // Sobrescribir el campo de datos 'id' con el ID real del documento
                    putJsonObject("id") { put("stringValue", newDocumentId) }
                }
            }.toString()

            // El campo a actualizar (updateMask) ahora va en la URL
            val updateMask = "id"

            // 3.2. Ejecutar el PATCH usando la URL COMPLETA
            val patchResponse: HttpResponse = client.patch(fullPatchUrl) {
                if (apiKey.isNotEmpty()) url.parameters.append("key", apiKey)
                // CLAVE: Añadir el updateMask como parámetro de URL
                url.parameters.append("updateMask.fieldPaths", updateMask)

                headers { append("Content-Type", ContentType.Application.Json) }
                setBody(patchBody)
            }

            if (patchResponse.status.isSuccess()) {
                // Puedes optar por eliminar o dejar este log de éxito
                // println("✅ Lote ${lote.number} creado y campo 'id' ligado exitosamente a $newDocumentId.")
                return@withContext newDocumentId // Devolver el ID real
            } else {
                // Mantenemos esta impresión para diagnosticar fallos futuros del PATCH
                val errorBody = patchResponse.bodyAsText()
                println("❌ ERROR FIREBASE (PATCH): Falló la ligadura del campo 'id'. Status: ${patchResponse.status}. Body: $errorBody")
                return@withContext null
            }

        } catch (e: Exception) {
            // Mantenemos esta impresión para errores de red o deserialización
            println("❌ Error en agregarYLigaroLote (POST/PATCH): ${e.message}")
            return@withContext null
        }
    }

    override suspend fun eliminarLoteHistorial(loteId: String): Boolean = withContext(Dispatchers.IO) {

        val baseUrl = buildDocumentBaseUrlWithoutKey()
        val apiKey = extractApiKey()

        // URL del documento específico en la colección 'historial'
        val docUrl = "$baseUrl/historial/$loteId"

        try {
            println("🌐 DELETE $docUrl (Eliminando de Historial)")
            val response: HttpResponse = client.delete(docUrl) {
                // 🛑 CORRECCIÓN: Aseguramos la API Key para el DELETE
                if (apiKey.isNotEmpty()) {
                    url.parameters.append("key", apiKey)
                }
            }

            if (response.status.isSuccess()) {
                println("✅ Lote $loteId eliminado de Historial.")
                true
            } else {
                println("❌ Error al eliminar Lote $loteId de Historial. Status: ${response.status}")
                println("Response Body: ${response.bodyAsText()}")
                false
            }
        } catch (e: Exception) {
            println("❌ Error en eliminarLoteHistorial: ${e.message}")
            false
        }
    }
}