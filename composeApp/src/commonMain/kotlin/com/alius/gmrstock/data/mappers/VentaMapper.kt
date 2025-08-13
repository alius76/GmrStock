package com.alius.gmrstock.data.mappers

import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.domain.model.VentaBigbag
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Instant

fun Map<String, Any?>.toVenta(): Venta {
    val fecha = (this["ventaFecha"] as? Timestamp)?.toKotlinInstant()

    val bigbags = (this["ventaBigbags"] as? List<Map<String, Any?>>)?.map { bb ->
        VentaBigbag(
            ventaBbNumber = bb["ventaBbNumber"] as? String ?: "",
            ventaBbWeight = bb["ventaBbWeight"] as? String ?: ""
        )
    } ?: emptyList()

    return Venta(
        ventaCliente = this["ventaCliente"] as? String ?: "",
        ventaLote = this["ventaLote"] as? String ?: "",
        ventaMaterial = this["ventaMaterial"] as? String ?: "",
        ventaFecha = fecha,
        ventaBigbags = bigbags
    )
}

fun Timestamp.toKotlinInstant(): Instant =
    Instant.fromEpochSeconds(this.seconds, this.nanoseconds)