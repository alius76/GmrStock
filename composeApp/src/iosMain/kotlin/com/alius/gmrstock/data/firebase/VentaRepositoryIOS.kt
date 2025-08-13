package com.alius.gmrstock.data.firebase

import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.data.mappers.toVenta
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize


class VentaRepositoryIOS(
    private val config: FirebaseDbConfig
) : VentaRepository {

    private val firestore by lazy {
        val options = FirebaseOptions(
            applicationId = config.appId,
            gcmSenderId = "",
            apiKey = config.apiKey,
            projectId = config.projectId
        )
        Firebase.initialize(context = null, options = options, name = "Firestore-${config.projectId}")
        Firebase.firestore
    }

    override suspend fun mostrarTodasLasVentas(): List<Venta> {
        val snapshot = firestore.collection("venta").get()
        return snapshot.documents.mapNotNull { doc ->
            // En iOS .data es nullable Map<String, Any?>
            doc.data?.toVenta()
        }
    }
}

actual fun getVentaRepository(config: FirebaseDbConfig): VentaRepository {
    return VentaRepositoryIOS(config)
}
