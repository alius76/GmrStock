package com.alius.gmrstock.data


import com.alius.gmrstock.data.firestore.buildQueryUltimosProcesos
import com.alius.gmrstock.data.mappers.ProcessMapper
import com.alius.gmrstock.domain.model.Process
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class ProcessRepositoryImpl(
    private val client: HttpClient,
    private val databaseUrl: String
) : ProcessRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun listarProcesos(): List<Process> = withContext(Dispatchers.IO) {
        println("🌐 Iniciando listado de procesos desde: $databaseUrl")

        try {
            val query = buildQueryUltimosProcesos()
            println("📤 Query Firestore:\n$query")

            val response: HttpResponse = client.post(databaseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            println("📥 Respuesta cruda (primeros 500 chars):\n${responseText.take(500)}")

            val rootArray = json.parseToJsonElement(responseText).jsonArray
            val procesos = rootArray.mapNotNull { element ->
                try {
                    val fields = element.jsonObject["document"]?.jsonObject
                        ?.get("fields")?.jsonObject

                    fields?.let {
                        val process = ProcessMapper.fromFirestore(it)
                        println("📄 Proceso parseado: ${process.number} | ${process.description} | ${process.date}")
                        process
                    }
                } catch (e: Exception) {
                    println("⚠️ Error parseando un proceso: ${e.message}")
                    null
                }
            }

            println("✅ Total de procesos obtenidos: ${procesos.size}")
            procesos

        } catch (e: Exception) {
            println("❌ Error en listarProcesos: ${e.message}")
            emptyList()
        } finally {
            println("⏹️ Fin de la carga de procesos")
        }
    }
}
