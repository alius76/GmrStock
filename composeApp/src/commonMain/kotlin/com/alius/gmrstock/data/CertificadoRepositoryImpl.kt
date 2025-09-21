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
import kotlinx.serialization.json.*

import io.github.aakira.napier.Napier

class CertificadoRepositoryImpl(
    private val client: HttpClient,
    private val databaseUrl: String
) : CertificadoRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getCertificadoByLoteNumber(loteNumber: String): Certificado? = withContext(Dispatchers.IO) {
        Napier.i(message = "🌐 [CertificadoRepo] Buscando certificado para lote=$loteNumber en $databaseUrl")
        try {
            val query = buildQueryCertificadoPorNumero(loteNumber)
            Napier.i(message = "📤 [CertificadoRepo] Query:\n$query")

            val response: HttpResponse = client.post(databaseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            Napier.i(message = "📥 [CertificadoRepo] Respuesta cruda:\n$responseText")

            // ⚠️ AQUÍ ESTÁ EL CAMBIO CLAVE
            val documentsArray = json.parseToJsonElement(responseText).jsonArray

            Napier.i(message = "🔍 [CertificadoRepo] Documentos encontrados: ${documentsArray.size}")

            val firstDocFields = documentsArray.firstOrNull()?.jsonObject
                ?.get("document")?.jsonObject
                ?.get("fields")?.jsonObject

            firstDocFields?.let {
                Napier.i(message = "✅ [CertificadoRepo] Campos de certificado encontrados:\n${it.toString()}")
                val certificado = CertificadoMapper.fromFirestore(it)
                Napier.i(message = "🚀 [CertificadoRepo] Certificado mapeado: ${certificado?.loteNumber}")
                return@withContext certificado
            }
            return@withContext null
        } catch (e: Exception) {
            Napier.e(message = "❌ [CertificadoRepo] Error en getCertificadoByLoteNumber: ${e.message}", throwable = e)
            return@withContext null
        } finally {
            Napier.i(message = "⏹️ [CertificadoRepo] Fin de búsqueda")
        }
    }
}