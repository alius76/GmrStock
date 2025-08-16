package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.buildQueryPorFecha
import com.alius.gmrstock.data.firestore.buildQueryPorNumero
import com.alius.gmrstock.data.firestore.buildQueryPorNumeroExacto
import com.alius.gmrstock.data.firestore.buildQueryUltimosLotes
import com.alius.gmrstock.data.firestore.parseRunQueryResponse
import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.MaterialGroup
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.http.*
import kotlinx.coroutines.IO
import kotlinx.datetime.*

class LoteRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : LoteRepository {

    /**
     * Función genérica para ejecutar una query contra Firestore
     */
    private suspend fun ejecutarQuery(query: String): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            println("🌐 POST $baseUrl")
            println("📤 Body: $query")

            val response: HttpResponse = client.post(baseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            parseRunQueryResponse(responseText)
        } catch (e: Exception) {
            println("❌ Error en ejecutarQuery: ${e.message}")
            emptyList()
        }
    }

    override suspend fun listarLotes(data: String): List<LoteModel> {
        val query = buildQueryPorNumero(data)
        return ejecutarQuery(query)
    }

    override suspend fun listarGruposPorDescripcion(filter: String): List<MaterialGroup> {
        val lotes = listarLotes(filter)
        return agruparPorMaterial(lotes)
    }

    override suspend fun getLoteByNumber(number: String): LoteModel? {
        val query = buildQueryPorNumeroExacto(number)
        return ejecutarQuery(query).firstOrNull()
    }

    override suspend fun listarLotesCreadosHoy(): List<LoteModel> {
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val inicioDeHoy = hoy.atStartOfDayIn(TimeZone.currentSystemDefault())
        val inicioDeManana = inicioDeHoy.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

        val query = buildQueryPorFecha(inicioDeHoy, inicioDeManana)
        return ejecutarQuery(query)
    }

    override suspend fun listarLotesPorFecha(fecha: LocalDate): List<LoteModel> {
        val inicio = fecha.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fin = inicio.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

        val query = buildQueryPorFecha(inicio, fin)
        return ejecutarQuery(query)
    }

    override suspend fun listarUltimosLotes(cantidad: Int): List<LoteModel> {
        val query = buildQueryUltimosLotes(cantidad)
        return ejecutarQuery(query)
    }
}