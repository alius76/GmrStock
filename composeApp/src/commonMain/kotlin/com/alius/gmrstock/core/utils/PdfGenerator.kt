package com.alius.gmrstock.core.utils

import com.alius.gmrstock.domain.model.Comanda
import com.alius.gmrstock.presentation.screens.ProduccionDiaria

expect object PdfGenerator {

    fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    )

    fun generateProductionReportPdf(
        datosAgrupados: List<ProduccionDiaria>,
        totalKilos: Double,
        promedio: Double,
        dateRange: String
    )

}

