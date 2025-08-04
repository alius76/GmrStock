package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Cliente
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual

@Serializable
data class LoteDto(
    val id: String,
    val number: String,
    val description: String,
    val date: Long?,
    val location: String,
    val count: String,
    val weight: String,
    val status: String,
    val totalWeight: String,
    val qrCode: String? = null,
    val bigBag: List<BigBags> = emptyList(),
    val booked: String? = null,
    val dateBooked: Long? = null,
    val remark: String = ""
)

