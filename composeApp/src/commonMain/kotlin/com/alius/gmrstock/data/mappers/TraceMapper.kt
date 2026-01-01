package com.alius.gmrstock.data.mappers

import com.alius.gmrstock.domain.model.Devolucion
import com.alius.gmrstock.domain.model.LoteModel
import com.alius.gmrstock.domain.model.Reprocesar
import com.alius.gmrstock.domain.model.TraceBigBag
import com.alius.gmrstock.domain.model.TraceEvent
import com.alius.gmrstock.domain.model.TraceEventType
import com.alius.gmrstock.domain.model.Venta

object TraceMapper {

    fun fromLote(lote: LoteModel) = TraceEvent(
        date = lote.createdAt,
        type = TraceEventType.CREACION,
        title = "Creación: ${lote.number}",
        subtitle = lote.description,
        totalWeight = lote.totalWeight,
        bigBags = lote.bigBag.map { TraceBigBag(it.bbNumber, it.bbWeight) },
        referenceId = lote.id
    )

    fun fromVenta(venta: Venta) = TraceEvent(
        date = venta.ventaFecha,
        type = TraceEventType.VENTA,
        title = "Venta: ${venta.ventaCliente}",
        subtitle = venta.ventaMaterial,
        totalWeight = venta.ventaPesoTotal ?: "0",
        bigBags = venta.ventaBigbags.map { TraceBigBag(it.ventaBbNumber, it.ventaBbWeight) },
        referenceId = venta.ventaLote
    )

    fun fromReproceso(repro: Reprocesar) = TraceEvent(
        date = repro.reprocesoDate,
        type = TraceEventType.REPROCESO,
        title = "Reprocesado a: ${repro.reprocesoTargetLoteNumber}",
        subtitle = repro.reprocesoDescription,
        totalWeight = repro.reprocesoLoteWeight,
        bigBags = repro.reprocesoBigBag.map { TraceBigBag(it.bbNumber, it.bbWeight) },
        referenceId = repro.reprocesoNumber
    )

    fun fromDevolucion(dev: Devolucion) = TraceEvent(
        date = dev.devolucionFecha,
        type = TraceEventType.DEVOLUCION,
        title = "Devolución: ${dev.devolucionCliente}",
        subtitle = dev.devolucionMaterial,
        totalWeight = dev.devolucionPesoTotal ?: "0",
        bigBags = dev.devolucionBigbags.map { TraceBigBag(it.devolucionBbNumber, it.devolucionBbWeight) },
        referenceId = dev.devolucionLote
    )
}