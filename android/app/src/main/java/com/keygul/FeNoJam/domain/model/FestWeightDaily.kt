package com.keygul.FeNoJam.domain.model

import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.times

data class FestWeightDaily(
    val date: LocalDate = LocalDate.now(),
    val weight: Double = 0.0,
    val heatPoints: List<FestHeatPoint> = listOf()
) {
    fun generateCircularHeatPoints(
        lat: Double,
        lng: Double,
        pointCount: Int = 100,
        radius: Double = 0.01
    ): List<FestHeatPoint> {

        return List(pointCount) {
            // 균일한 원 내부 분포
            val distance = radius * kotlin.math.sqrt(Random.nextDouble())

            val angle = Random.nextDouble(0.0, 2 * PI)

            val latOffset = distance * cos(angle)
            val lngOffset = distance * sin(angle)

            // 중심에서 멀수록 weight 감소
            val normalizedDistance = distance / radius

            val weight =
                this.weight * (1.0 - normalizedDistance)
                    .coerceAtLeast(0.05)

            FestHeatPoint(
                lat = lat + latOffset,
                lng = lng + lngOffset,
                weight = weight
            )
        }
    }
}
