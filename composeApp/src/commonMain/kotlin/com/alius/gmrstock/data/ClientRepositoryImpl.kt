package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.data.firestore.buildCreateBodyForClient
import com.alius.gmrstock.data.firestore.buildPatchBodyForClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ClientRepositoryImpl(
    private val client: HttpClient,
    private val databaseUrl: String
) : ClientRepository {

    // 1. URL de la API REST para Consultas (usa :runQuery)
    private val databaseQueryUrl = databaseUrl

    // 2. 🚨 FUNCIÓN PARA CONSTRUIR LA URL DE DOCUMENTO (sin :runQuery)

    private fun buildDocumentBaseUrl(): String {
        // La URL de CRUD debe ser: .../documents?key=API_KEY
        // La obtenemos quitando el ":runQuery" del final de la URL de consulta.
        return databaseUrl.substringBeforeLast(":runQuery")
    }

    // 3. 🚨 CORRECCIÓN CRÍTICA: URL base para la colección de clientes (CRUD)
    // Debe usar la URL de documento, no la URL de consulta.
    private val clientCollectionUrl = "${buildDocumentBaseUrl()}/cliente"

    // Configuración para el parser de JSON
    private val json = Json { ignoreUnknownKeys = true }

// -----------------------------------------------------------------------------
// MÉTODOS DE LECTURA (USAN QUERY URL)
// -----------------------------------------------------------------------------

    private suspend fun internalGetAllClientsWithIds(): List<Pair<String, Cliente>> = withContext(Dispatchers.IO) {
        try {
            val body = """
            {
              "structuredQuery": {
                "from": [{ "collectionId": "cliente" }],
                "orderBy": [{ "field": { "fieldPath": "cliNombre" }, "direction": "ASCENDING" }]
              }
            }
            """.trimIndent()

            // 💡 Usamos POST al endpoint de consulta (databaseQueryUrl)
            val response: HttpResponse = client.post(databaseQueryUrl) { // 🚨 USAR databaseQueryUrl
                headers { append("Content-Type", "application/json") }
                setBody(body)
            }
            // ... (Resto de la lógica de parsing es correcta)
            val responseText = response.bodyAsText()
            val jsonArray = json.parseToJsonElement(responseText).jsonArray

            jsonArray.mapNotNull { element ->
                val doc = element.jsonObject["document"]?.jsonObject ?: return@mapNotNull null
                val fields = doc["fields"]?.jsonObject ?: return@mapNotNull null

                val fullPath = doc["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val documentId = fullPath.substringAfterLast("/")

                val cliente = Cliente(
                    cliNombre = fields["cliNombre"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                    cliObservaciones = fields["cliObservaciones"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                )
                documentId to cliente
            }
        } catch (e: Exception) {
            println("❌ Error al obtener clientes (internal): ${e.message}")
            emptyList()
        }
    }

    override suspend fun getAllClientsOrderedByName(): List<Cliente> {
        return internalGetAllClientsWithIds().map { it.second }
    }

    override suspend fun getAllClientsWithIds(): List<Pair<String, Cliente>> {
        return internalGetAllClientsWithIds()
    }

// -----------------------------------------------------------------------------
// MÉTODOS DE ESCRITURA (USAN DOCUMENT URL)
// -----------------------------------------------------------------------------

    // 3. Crear Cliente (POST)
    override suspend fun addClient(cliente: Cliente): String = withContext(Dispatchers.IO) {
        val body = buildCreateBodyForClient(cliente)
        try {
            // 💡 clientCollectionUrl es ahora la URL correcta: .../documents?key=API_KEY/cliente
            val response: HttpResponse = client.post(clientCollectionUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(body)
            }

            val responseJson = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val fullPath = responseJson["name"]?.jsonPrimitive?.content ?: throw IllegalStateException("Error al obtener ID del nuevo cliente.")
            return@withContext fullPath.substringAfterLast("/")
        } catch (e: Exception) {
            println("❌ Error al crear cliente: ${e.message}")
            throw e
        }
    }

    // 4. Actualizar Cliente (PATCH)
    override suspend fun updateClient(documentId: String, cliente: Cliente): Unit = withContext(Dispatchers.IO) {

        // 1. Crear la URL BASE del documento SIN MÁSCARA, SIN PARÁMETROS extra
        val documentUrl = "$clientCollectionUrl/$documentId"

        val body = buildPatchBodyForClient(cliente)

        try {
            // 2. Usamos Ktor's client.patch y le pasamos la URL base.
            client.patch(documentUrl) {

                // 🚨 CORRECCIÓN CRÍTICA: Añadimos la máscara de actualización usando el builder de Ktor
                url.parameters.append("updateMask.fieldPaths", "cliNombre")
                url.parameters.append("updateMask.fieldPaths", "cliObservaciones")

                // Nota: Ktor agregará automáticamente &updateMask.fieldPaths=...
                // La API Key ya está en la URL base (documentUrl) que viene de databaseUrl.

                headers { append("Content-Type", "application/json") }
                setBody(body)
            }.bodyAsText() // Consumir respuesta
        } catch (e: Exception) {
            println("❌ Error al actualizar cliente $documentId: ${e.message}")
            throw e
        }
    }

    // 5. Eliminar Cliente (DELETE)
    override suspend fun deleteClient(documentId: String): Unit = withContext(Dispatchers.IO) {
        val documentUrl = "$clientCollectionUrl/$documentId" // URL del documento: .../cliente/[ID]

        try {
            client.delete(documentUrl).bodyAsText()
        } catch (e: Exception) {
            println("❌ Error al eliminar cliente $documentId: ${e.message}")
            throw e
        }
    }
}