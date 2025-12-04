package com.alius.gmrstock.data

import com.alius.gmrstock.data.firestore.parseRunQueryResponseMaterial
import com.alius.gmrstock.domain.model.Material
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.IO

class MaterialRepositoryImpl(
    private val client: HttpClient,
    private val databaseUrl: String
) : MaterialRepository {

    override suspend fun getAllMaterialsOrderedByName(): List<Material> = withContext(Dispatchers.IO) {
        try {
            val query = """
                {
                  "structuredQuery": {
                    "from": [{ "collectionId": "material" }],
                    "orderBy": [{ "field": { "fieldPath": "materialNombre" }, "direction": "ASCENDING" }]
                  }
                }
            """.trimIndent()

            Napier.i("🌐 POST $databaseUrl")
            Napier.i("📤 Body: $query")

            val response: HttpResponse = client.post(databaseUrl) {
                headers { append("Content-Type", "application/json") }
                setBody(query)
            }

            val responseText = response.bodyAsText()
            parseRunQueryResponseMaterial(responseText)
        } catch (e: Exception) {
            Napier.e("❌ Error en getAllMaterialsOrderedByName: ${e.message}", e)
            emptyList()
        }
    }
}
