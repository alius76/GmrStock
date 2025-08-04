package com.alius.gmrstock.data

import android.util.Log
import com.google.firebase.Timestamp
import com.alius.gmrstock.data.mappers.LoteDtoMapper
import com.alius.gmrstock.domain.model.LoteModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class LoteRepositoryImpl : LoteRepository {

    private val firestore = FirebaseFirestore.getInstance().collection("lote")

    override suspend fun listarLotes(data: String): List<LoteModel> {
        return try {
            val snapshot = firestore
                .orderBy("number", Query.Direction.ASCENDING)
                .startAt(data)
                .endAt(data + "\uf8ff")
                .get()
                .await()

            Log.d("LoteRepository", "Documentos encontrados: ${snapshot.documents.size}")

            val lotes = snapshot.documents.mapNotNull { doc ->
                try {
                    val dto = doc.toObject(LoteDtoAndroid::class.java)?.toCommonDto()
                    Log.d("LoteRepository", "Documento convertido: ${dto?.number}")
                    dto?.let { LoteDtoMapper.fromDto(it) }
                } catch (e: Exception) {
                    Log.e("LoteRepository", "Error al mapear lote: ${e.message}", e)
                    null
                }
            }

            Log.d("LoteRepository", "Lotes convertidos correctamente: ${lotes.size}")
            lotes

        } catch (e: Exception) {
            Log.e("LoteRepository", "Error listando lotes: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun agregarLoteConBigBags() {
        val firestore = FirebaseFirestore.getInstance()

        val bigBagsList = listOf(
            mapOf(
                "bbNumber" to "BB001",
                "bbWeight" to "100",
                "bbLocation" to "Zona A",
                "bbStatus" to "s",
                "bbRemark" to "Sin daños"
            ),
            mapOf(
                "bbNumber" to "BB002",
                "bbWeight" to "150",
                "bbLocation" to "Zona B",
                "bbStatus" to "s",
                "bbRemark" to "Revisar"
            )
        )

        val loteMap = mapOf(
            "id" to UUID.randomUUID().toString(),
            "number" to "L-2025",
            "description" to "Lote con BigBags",
            "date" to Timestamp.now(),               // <-- Aquí Timestamp
            "location" to "Almacén Principal",
            "count" to "2",
            "weight" to "250",
            "status" to "Parcial",
            "totalWeight" to "250",
            "qrCode" to "QR2025",
            "bigBag" to bigBagsList,
            "booked" to "cliente@example.com",
            "dateBooked" to Timestamp.now(),         // <-- Aquí Timestamp también
            "remark" to "Lote de ejemplo"
        )

        try {
            firestore.collection("lote")
                .add(loteMap)
                .await()
            println("✅ Lote con BigBags agregado correctamente")
        } catch (e: Exception) {
            println("❌ Error al agregar lote: ${e.message}")
        }
    }
}

actual fun getLoteRepository(): LoteRepository = LoteRepositoryImpl()