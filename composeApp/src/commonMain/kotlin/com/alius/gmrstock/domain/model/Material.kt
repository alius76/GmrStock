package com.alius.gmrstock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Material(
    val id: String = "",
    val materialNombre: String = "",
    val tipoPlastico: String = "", // Nuevo campo
    val parametros: List<Parametro> = emptyList() // Nuevo campo
)