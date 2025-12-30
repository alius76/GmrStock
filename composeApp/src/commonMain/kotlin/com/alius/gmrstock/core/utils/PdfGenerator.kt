package com.alius.gmrstock.core.utils

import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.domain.model.Ratio
import com.alius.gmrstock.domain.model.Venta
import com.alius.gmrstock.presentation.screens.ProduccionDiaria

expect object PdfGenerator {

    fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    )

    fun generateProductionReportPdf(
        ratios: List<Ratio>,
        totalKilos: Double,
        promedio: Double,
        dateRange: String,
        loteNombresMap: Map<String, String>
    )

    fun generateVentasReportPdf(
        clienteNombre: String,
        ventas: List<Venta>,
        totalKilos: Double,
        dateRange: String,
        desgloseMateriales: Map<String, Double>
    )

}

