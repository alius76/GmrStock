package com.alius.gmrstock.domain.model

data class MaterialGroup(

    val description: String,      // Descripción del material
    val totalWeight: String,      // Peso total de los lotes con esa descripción
    val totalLotes: Int,          // Cantidad de lotes con esa descripción
    val loteNumbers: List<String> // Lista con los números de lote
)