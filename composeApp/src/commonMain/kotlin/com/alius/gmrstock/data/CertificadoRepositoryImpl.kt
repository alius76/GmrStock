package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.buildQueryCertificadoPorNumero
import com.alius.gmrstock.data.mappers.CertificadoMapper
import com.alius.gmrstock.domain.model.Certificado
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class CertificadoRepositoryImpl(
    private val client: HttpClient,
    private val databaseUrl: String
) : CertificadoRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getCertificadoByLoteNumber(loteNumber: String): Certificado? = withContext(Dispatchers.IO) {
        println("🌐 [CertificadoRepo] Buscando certificado para lote=$loteNumber en $databaseUrl")

        try {
            val query = buildQueryCertificadoPorNumero(loteNumber)
            println("📤 [CertificadoRepo] Query:\n$query")

            val response: HttpResponse = client.post(databaseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            println("📥 [CertificadoRepo] Respuesta cruda (500 chars):\n${responseText.take(500)}")

            val rootArray = json.parseToJsonElement(responseText).jsonArray

            val firstDoc = rootArray.firstOrNull()
                ?.jsonObject?.get("document")
                ?.jsonObject?.get("fields")
                ?.jsonObject

            firstDoc?.let {
                val certificado = CertificadoMapper.fromFirestore(it)
                println("✅ [CertificadoRepo] Certificado encontrado: ${certificado.loteNumber}")
                certificado
            }

        } catch (e: Exception) {
            println("❌ [CertificadoRepo] Error en getCertificadoByLoteNumber: ${e.message}")
            null
        } finally {
            println("⏹️ [CertificadoRepo] Fin de búsqueda")
        }
    }
}


