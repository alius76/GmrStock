package com.alius.gmrstock.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LoteModel(
    val id: String,
    val number: String,
    val description: String,
    val date: Instant?,
    val location: String,
    val count: String,
    val weight: String,
    val status: String,
    val totalWeight: String,
    val qrCode: String?,
    val bigBag: List<BigBags>,
    val booked: Cliente? = null,
    val dateBooked: Instant?,
    val remark: String,
    val certificateOk: Boolean = false,
    val createdAt: Instant?
)