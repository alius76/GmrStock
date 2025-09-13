package com.alius.gmrstock.domain.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Propiedad(
    val nombre: String = "",
    val valor: String = "",
    val warning: Boolean = false
)

@Serializable
data class Certificado(
    val loteNumber: String = "",
    val fecha: Instant? = null,
    val status: String = "c", // "c" = correct, "w" = warning
    val propiedades: List<Propiedad> = emptyList()
)

