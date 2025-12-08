package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.buildCreateBodyForDevolucion
import com.alius.gmrstock.data.mappers.DevolucionMapper
import com.alius.gmrstock.domain.model.Devolucion
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.* // ⬅️ IMPORTACIÓN NECESARIA PARA FECHAS

class DevolucionRepositoryImpl(
    private val client: HttpClient,
    private val databaseBaseUrl: String // URL base sin ":runQuery"
) : DevolucionRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun obtenerTodasLasDevoluciones(): List<Devolucion> = withContext(Dispatchers.IO) {
        try {
            // 🔑 CORRECCIÓN: Usamos databaseBaseUrl directamente.
            // Si databaseBaseUrl ya es "BASE_URL/documents:runQuery", no necesitamos añadirlo de nuevo.
            val url = databaseBaseUrl
            println("🌐 Consultando todas las devoluciones con URL CORREGIDA: $url")

            val body = """
                {
                    "structuredQuery": {
                        "from": [{ "collectionId": "devolucion" }]
                    }
                }
            """.trimIndent()

            val response: HttpResponse = client.post(url) {
                headers { append("Content-Type", "application/json") }
                setBody(body)
            }

            val responseText = response.bodyAsText()
            println("📥 Respuesta cruda de obtenerTodasLasDevoluciones: $responseText")

            // Si la respuesta es HTML (404), esto fallará, pero ahora la URL está correcta.
            val jsonArray = json.parseToJsonElement(responseText).jsonArray
            jsonArray.mapNotNull { element ->
                try {
                    val doc = element.jsonObject["document"]?.jsonObject ?: return@mapNotNull null
                    val fields = doc["fields"]?.jsonObject ?: return@mapNotNull null
                    DevolucionMapper.fromFirestore(fields)
                } catch (e: Exception) {
                    println("⚠️ Error parseando devolución: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo devoluciones: ${e.message}")
            emptyList()
        }
    }

    override suspend fun agregarDevolucion(devolucion: Devolucion): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1️⃣ URL correcta para POST (CRUD, no runQuery)
            val url = databaseBaseUrl.removeSuffix(":runQuery").replace("/documents", "") + "/documents/devolucion"
            println("🌐 Intentando agregar devolución con URL: $url")

            // 2️⃣ Construimos el JSON para Firestore
            val body = buildCreateBodyForDevolucion(devolucion)
            println("📤 JSON enviado a Firestore:\n$body")

            // 3️⃣ Llamada HTTP
            val response: HttpResponse = client.post(url) {
                headers { append("Content-Type", "application/json") }
                setBody(body)
            }

            // 4️⃣ Respuesta cruda
            val responseText = response.bodyAsText()
            println("📥 Respuesta cruda de Firestore:\n$responseText")

            // 5️⃣ Parsear ID del documento creado
            val responseJson = json.parseToJsonElement(responseText).jsonObject
            val fullPath = responseJson["name"]?.jsonPrimitive?.content

            if (fullPath != null) {
                val documentId = fullPath.substringAfterLast("/")
                println("✅ Devolución guardada correctamente con ID: $documentId")
                true
            } else {
                println("⚠️ Error al guardar devolución: no se devolvió ID")
                false
            }
        } catch (e: Exception) {
            println("❌ Excepción agregando devolución: ${e.message}")
            false
        }
    }

    override suspend fun obtenerDevolucionesPorLote(loteNumber: String): List<Devolucion> {
        // Filtrado en memoria; si quieres, podemos construir una query específica en Firestore
        return obtenerTodasLasDevoluciones().filter { it.devolucionLote == loteNumber }
    }

    /**
     * 🔑 NUEVA IMPLEMENTACIÓN: Obtiene las devoluciones del mes actual filtrando en memoria.
     * Esto asume que obtenerTodasLasDevoluciones es rápido. Si la colección es grande,
     * se debería crear una StructuredQuery específica en Firestore.
     */
    override suspend fun obtenerDevolucionesDelMes(): List<Devolucion> {
        // Obtener la zona horaria del sistema para la hora actual
        val systemTimeZone = TimeZone.currentSystemDefault()

        // Obtener la fecha y hora actual en la zona horaria del sistema
        val now = Clock.System.now().toLocalDateTime(systemTimeZone)

        // Obtener el mes y año actual
        val currentMonth = now.month
        val currentYear = now.year

        return obtenerTodasLasDevoluciones().filter { devolucion ->
            devolucion.devolucionFecha?.let { instant ->
                // Convertir el Instant de la devolución a LocalDateTime
                val devolucionDateTime = instant.toLocalDateTime(systemTimeZone)

                // Comparar el mes y el año
                devolucionDateTime.month == currentMonth && devolucionDateTime.year == currentYear
            } ?: false
        }
    }
}