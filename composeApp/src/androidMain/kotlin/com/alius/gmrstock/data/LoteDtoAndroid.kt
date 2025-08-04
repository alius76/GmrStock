package com.alius.gmrstock.data

import com.alius.gmrstock.domain.model.BigBags
import com.alius.gmrstock.domain.model.Cliente
import com.google.firebase.Timestamp

data class LoteDtoAndroid(
    var id: String = "",
    var number: String = "",
    var description: String = "",
    var date: Timestamp? = null,
    var location: String = "",
    var count: String = "",
    var weight: String = "",
    var status: String = "",
    var totalWeight: String = "",
    var qrCode: String? = null,
    var bigBag: List<BigBags> = emptyList(),
    val booked: String? = null,
    var dateBooked: Timestamp? = null,
    var remark: String = ""
) {
    fun toCommonDto(): LoteDto {
        return LoteDto(
            id = id,
            number = number,
            description = description,
            date = date?.toDate()?.time,
            location = location,
            count = count,
            weight = weight,
            status = status,
            totalWeight = totalWeight,
            qrCode = qrCode,
            bigBag = bigBag,
            booked = booked,
            dateBooked = dateBooked?.toDate()?.time,
            remark = remark
        )
    }
}