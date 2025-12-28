package com.alius.gmrstock.domain.model

data class Ratio(
    val ratioId: String,
    val ratioDate: Long,          // timestamp en millis
    val ratioTotalWeight: String,  // peso en kilos como string
    val ratioLoteId: String
)
