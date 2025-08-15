package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.Cliente
import com.alius.gmrstock.data.ClientRepository
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

    override suspend fun getAllClientsOrderedByName(): List<Cliente> = withContext(Dispatchers.IO) {
        try {
            val body = """
            {
              "structuredQuery": {
                "from": [{ "collectionId": "cliente" }],
                "orderBy": [{ "field": { "fieldPath": "cliNombre" }, "direction": "ASCENDING" }]
              }
            }
            """.trimIndent()

            val response: HttpResponse = client.post(databaseUrl) {
                headers {
                    append("Content-Type", "application/json")
                }
                setBody(body)
            }

            val responseText = response.bodyAsText()
            val jsonArray = Json.parseToJsonElement(responseText).jsonArray

            jsonArray.mapNotNull { element ->
                val doc = element.jsonObject["document"]?.jsonObject ?: return@mapNotNull null
                val fields = doc["fields"]?.jsonObject ?: return@mapNotNull null
                Cliente(
                    cliNombre = fields["cliNombre"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                    cliObservaciones = fields["cliObservaciones"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                )
            }
        } catch (e: Exception) {
            println("❌ Error al obtener clientes: ${e.message}")
            emptyList()
        }
    }
}


