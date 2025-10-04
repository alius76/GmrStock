package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.buildQueryHistorialDeHoy
import com.alius.gmrstock.data.firestore.parseRunQueryResponse
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.data.HistorialRepository
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext


class HistorialRepositoryImpl(
    private val client: HttpClient,
    private val firestoreRunQueryUrl: String
) : HistorialRepository {

    /**
     * Función genérica para ejecutar una query contra Firestore
     */
    private suspend fun ejecutarQuery(query: String): List<LoteModel> = withContext(Dispatchers.IO) {
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
}