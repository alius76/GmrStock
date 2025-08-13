package com.alius.gmrstock.data.firebase

import android.content.Context
import com.alius.gmrstock.core.AppContextProvider.appContext
import com.alius.gmrstock.domain.model.Venta
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.alius.gmrstock.data.mappers.toVenta

class VentaRepositoryAndroid(
    private val context: Context,
    private val config: FirebaseDbConfig
) : VentaRepository {

    private val firestore by lazy {
        val appName = "Firestore-${config.projectId}"
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == appName }
            ?: FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setProjectId(config.projectId)
                    .setApplicationId(config.appId)
                    .setApiKey(config.apiKey)
                    .build(),
                appName
            )
        FirebaseFirestore.getInstance(app)
    }

    override suspend fun mostrarTodasLasVentas(): List<Venta> {
        val snapshot = firestore.collection("venta").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.data?.toVenta()
        }
    }
}

actual fun getVentaRepository(config: FirebaseDbConfig): VentaRepository {
    return VentaRepositoryAndroid(appContext, config)
}
