package com.keygul.FeNoJam.data.model

import com.keygul.FeNoJam.data.BaseMapper
import com.keygul.FeNoJam.domain.model.FestHeatPoint
import com.keygul.FeNoJam.domain.model.FestMap
import com.keygul.FeNoJam.domain.model.FestPlace
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class FestMapDto (
    val festivals: List<FestPlaceDto>,
    val heatmaps: Map<String, List<FestHeatPointDto>>
)

@Serializable
data class FestPlaceDto (
    val id: Long,
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val stDate: String,
    val enDate: String,
    val thumbnail: String? = null
)

@Serializable
data class FestHeatPointDto (
    val lat: Double,
    val lng: Double,
    val weight: Double
)

object FestMapMapper: BaseMapper<List<FestMap>, FestMapDto> {
    override fun toDomain(data: FestMapDto): List<FestMap> {
        return data.festivals.map { festival ->
            val heatPointDtos: List<FestHeatPointDto> = data.heatmaps[festival.id.toString()]!!
            FestMap (
                festPlace = FestPlaceMapper.toDomain(festival),
                festHeatPoints = heatPointDtos.map {
                    FestHeatPointMapper.toDomain(it)
                }
            )
        }
    }

    override fun toData(domain: List<FestMap>): FestMapDto? = null
}

object FestPlaceMapper: BaseMapper<FestPlace, FestPlaceDto> {
    override fun toDomain(data: FestPlaceDto): FestPlace = FestPlace (
        id = data.id,
        name = data.name,
        lat = data.lat,
        lng = data.lng,
        address = data.address,
        stDate = LocalDateTime.parse(data.stDate),
        enDate = LocalDateTime.parse(data.enDate),
        thumbnail = data.thumbnail
    )

    override fun toData(domain: FestPlace): FestPlaceDto? = null
}

object FestHeatPointMapper: BaseMapper<FestHeatPoint, FestHeatPointDto> {
    override fun toDomain(data: FestHeatPointDto): FestHeatPoint = FestHeatPoint (
        lat = data.lat,
        lng = data.lng,
        weight = data.weight
    )

    override fun toData(domain: FestHeatPoint): FestHeatPointDto? = null
}