package com.alius.gmrstock.data.mappers

import com.alius.gmrstock.data.LoteDto
import com.alius.gmrstock.domain.model.LoteModel
import kotlinx.datetime.Instant

object LoteDtoMapper {

    fun fromDto(dto: LoteDto): LoteModel {
        return LoteModel(
            id = dto.id,
            number = dto.number,
            description = dto.description,
            date = dto.date?.let { Instant.fromEpochMilliseconds(it) },
            location = dto.location,
            count = dto.count,
            weight = dto.weight,
            status = dto.status,
            totalWeight = dto.totalWeight,
            qrCode = dto.qrCode,
            bigBag = dto.bigBag,
            booked = dto.booked,
            dateBooked = dto.dateBooked?.let { Instant.fromEpochMilliseconds(it) },
            remark = dto.remark,
            createdAt = dto.createdAt?.let { Instant.fromEpochMilliseconds(it) },
            certificateOk = dto.certificateOk
        )
    }

    fun toDto(model: LoteModel): LoteDto {
        return LoteDto(
            id = model.id,
            number = model.number,
            description = model.description,
            date = model.date?.toEpochMilliseconds(),
            location = model.location,
            count = model.count,
            weight = model.weight,
            status = model.status,
            totalWeight = model.totalWeight,
            qrCode = model.qrCode,
            bigBag = model.bigBag,
            booked = model.booked,
            dateBooked = model.dateBooked?.toEpochMilliseconds(),
            remark = model.remark,
            createdAt = model.createdAt?.toEpochMilliseconds(),
            certificateOk = model.certificateOk
        )
    }
}
