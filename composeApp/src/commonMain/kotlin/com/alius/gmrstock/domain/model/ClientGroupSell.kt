package com.alius.gmrstock.domain.model
import kotlinx.serialization.Serializable

@Serializable
data class ClientGroupSell(
    val cliente: Cliente,
    val totalVentasMes: Int,
    val totalKilosVendidos: Int
)
