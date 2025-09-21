package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.buildQueryRatiosDelDia
import com.alius.gmrstock.data.firestore.buildQueryRatiosDelMesActual
import com.alius.gmrstock.data.mappers.RatioMapper
import com.alius.gmrstock.domain.model.Ratio
import io.github.aakira.napier.Napier
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
        Napier.d { "🌐 Iniciando listado de ratios del mes desde: $databaseUrl" }

        try {
            val query = buildQueryRatiosDelMesActual()
            Napier.d { "📤 Query Firestore:\n$query" }

            val response: HttpResponse = client.post(databaseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            Napier.d { "📥 Respuesta cruda (primeros 500 chars):\n${responseText.take(500)}" }

            val rootArray = json.parseToJsonElement(responseText).jsonArray
            val ratios = rootArray.mapNotNull { element ->
                try {
                    val fields = element.jsonObject["document"]?.jsonObject
                        ?.get("fields")?.jsonObject

                    fields?.let {
                        val ratio = RatioMapper.fromFirestore(it)
                        Napier.i { "📄 Ratio parseado: ${ratio.ratioId} | ${ratio.ratioDate} | ${ratio.ratioTotalWeight}" }
                        ratio
                    }
                } catch (e: Exception) {
                    Napier.e(e) { "⚠️ Error parseando un ratio." }
                    null
                }
            }

            Napier.d { "✅ Total de ratios obtenidos: ${ratios.size}" }
            ratios

        } catch (e: Exception) {
            Napier.e(e) { "❌ Error en listarRatiosDelMes." }
            emptyList()
        } finally {
            Napier.d { "⏹️ Fin de la carga de ratios del mes" }
        }
    }

    // Nueva función para listar ratios del día
    override suspend fun listarRatiosDelDia(): List<Ratio> = withContext(Dispatchers.IO) {
        Napier.d { "🌐 Iniciando listado de ratios del día desde: $databaseUrl" }

        try {
            val query = buildQueryRatiosDelDia()
            Napier.d { "📤 Query Firestore:\n$query" }

            val response: HttpResponse = client.post(databaseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            Napier.d { "📥 Respuesta cruda (primeros 500 chars):\n${responseText.take(500)}" }

            val rootArray = json.parseToJsonElement(responseText).jsonArray
            val ratios = rootArray.mapNotNull { element ->
                try {
                    val fields = element.jsonObject["document"]?.jsonObject
                        ?.get("fields")?.jsonObject

                    fields?.let {
                        val ratio = RatioMapper.fromFirestore(it)
                        Napier.i { "📄 Ratio parseado: ${ratio.ratioId} | ${ratio.ratioDate} | ${ratio.ratioTotalWeight}" }
                        ratio
                    }
                } catch (e: Exception) {
                    Napier.e(e) { "⚠️ Error parseando un ratio." }
                    null
                }
            }

            Napier.d { "✅ Total de ratios obtenidos: ${ratios.size}" }
            ratios

        } catch (e: Exception) {
            Napier.e(e) { "❌ Error en listarRatiosDelDia." }
            emptyList()
        } finally {
            Napier.d { "⏹️ Fin de la carga de ratios del día" }
        }
    }
}