package com.keygul.FeNoJam.data.model

import com.keygul.FeNoJam.data.BaseMapper
import com.keygul.FeNoJam.domain.model.FestHeatPoint
import com.keygul.FeNoJam.domain.model.FestPlace
import com.keygul.FeNoJam.domain.model.FestTraffic
import com.keygul.FeNoJam.domain.model.FestTrafficItem
import com.keygul.FeNoJam.domain.model.FestWeightDaily
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class FestPlaceDto (
    val id: Long,
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val stDate: String,
    val enDate: String,
    val thumbnail: String? = null,
    val weights: List<FestWeightDto>,
    val traffics: List<FestTrafficDto>
)

@Serializable
data class FestWeightDto (
    val date: String,
    val weight: Double
)

@Serializable
data class FestTrafficDto (
    val date: String,
    val weights: List<FestTrafficItemDto>
)

@Serializable
data class FestTrafficItemDto (
    val datetime: String,
    val weight: Double
)

object FestPlaceMapper: BaseMapper<FestPlace, FestPlaceDto> {
    override fun toDomain(data: FestPlaceDto): FestPlace = FestPlace (
        id = data.id,
        name = data.name,
        lat = data.lat,
        lng = data.lng,
        address = data.address,
        stDate = LocalDateTime.parse(data.stDate),
        enDate = LocalDateTime.parse(data.enDate),
        thumbnail = data.thumbnail,
        weights = data.weights.map {
            val temp = FestWeightMapper.toDomain(it)
            temp.copy(
                heatPoints = temp.generateCircularHeatPoints(data.lat, data.lng)
            )
        },
        traffics = data.traffics.map { FestTrafficMapper.toDomain(it) }
    )

    override fun toData(domain: FestPlace): FestPlaceDto? = null
}

object FestWeightMapper: BaseMapper<FestWeightDaily, FestWeightDto> {
    override fun toDomain(data: FestWeightDto): FestWeightDaily = FestWeightDaily (
        date = LocalDate.parse(data.date),
        weight = data.weight
    )

    override fun toData(domain: FestWeightDaily): FestWeightDto? = null
}

object FestTrafficMapper: BaseMapper<FestTraffic, FestTrafficDto> {
    override fun toDomain(data: FestTrafficDto): FestTraffic = FestTraffic (
        date = LocalDate.parse(data.date),
        items = data.weights.map { FestTrafficItemMapper.toDomain(it) }
    )

    override fun toData(domain: FestTraffic): FestTrafficDto? = null
}

object FestTrafficItemMapper: BaseMapper<FestTrafficItem, FestTrafficItemDto> {
    override fun toDomain(data: FestTrafficItemDto): FestTrafficItem = FestTrafficItem (
        datetime = LocalDateTime.parse(data.datetime),
        weight = data.weight
    )

    override fun toData(domain: FestTrafficItem): FestTrafficItemDto? = null
}