package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.buildQueryRatiosDelMesActual
import com.alius.gmrstock.data.mappers.RatioMapper
import com.alius.gmrstock.domain.model.Ratio
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class RatioRepositoryImpl(
    private val client: HttpClient,
    private val databaseUrl: String
) : RatioRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun listarRatiosDelMes(): List<Ratio> = withContext(Dispatchers.IO) {
        println("🌐 Iniciando listado de ratios del mes desde: $databaseUrl")

        try {
            val query = buildQueryRatiosDelMesActual()
            println("📤 Query Firestore:\n$query")

            val response: HttpResponse = client.post(databaseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            println("📥 Respuesta cruda (primeros 500 chars):\n${responseText.take(500)}")

            val rootArray = json.parseToJsonElement(responseText).jsonArray
            val ratios = rootArray.mapNotNull { element ->
                try {
                    val fields = element.jsonObject["document"]?.jsonObject
                        ?.get("fields")?.jsonObject

                    fields?.let {
                        val ratio = RatioMapper.fromFirestore(it)
                        println("📄 Ratio parseado: ${ratio.ratioId} | ${ratio.ratioDate} | ${ratio.ratioTotalWeight}")
                        ratio
                    }
                } catch (e: Exception) {
                    println("⚠️ Error parseando un ratio: ${e.message}")
                    null
                }
            }

            println("✅ Total de ratios obtenidos: ${ratios.size}")
            ratios

        } catch (e: Exception) {
            println("❌ Error en listarRatiosDelMes: ${e.message}")
            emptyList()
        } finally {
            println("⏹️ Fin de la carga de ratios")
        }
    }
}


