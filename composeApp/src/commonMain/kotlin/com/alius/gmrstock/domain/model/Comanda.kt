package com.alius.gmrstock.domain.model

import com.alius.gmrstock.domain.model.Cliente
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Comanda(
    val idComanda: String,
    val numeroDeComanda: Long = 0,
    val numberLoteComanda: String,
    val descriptionLoteComanda: String,
    val dateBookedComanda: Instant? = null,
    val totalWeightComanda: String,
    val bookedClientComanda: Cliente? = null,
    val remarkComanda: String = "",
    val fueVendidoComanda: Boolean = false
)