package com.alius.gmrstock.core.utils

import com.alius.gmrstock.domain.model.Comanda

expect object PdfGenerator {

    fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    )
}

