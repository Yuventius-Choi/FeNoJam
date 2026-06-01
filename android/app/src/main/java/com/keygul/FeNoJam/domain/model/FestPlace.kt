package com.keygul.FeNoJam.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class FestPlace(
    val id: Long = -1L,
    val name: String = "강원도 테스트 축제",
    val lat: Double = 37.8228,
    val lng: Double = 128.1555,
    val address: String = "강원도",
    val stDate: LocalDateTime = LocalDateTime.now(),
    val enDate: LocalDateTime = LocalDateTime.now(),
    val thumbnail: String? = "https://images.unsplash.com/photo-1571566882372-1598d88abd90?q=80&w=1287&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
    val weight: Double = 1.0,
    val heatPoints: List<FestHeatPoint> = listOf()
) {
    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other is FestPlace) {
                return this.id == other.id
            }
        }
        return false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + lat.hashCode()
        result = 31 * result + lng.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + stDate.hashCode()
        result = 31 * result + enDate.hashCode()
        result = 31 * result + (thumbnail?.hashCode() ?: 0)
        return result
    }

    fun generateCircularHeatPoints(
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
                lat = this.lat + latOffset,
                lng = this.lng + lngOffset,
                weight = weight
            )
        }
    }

    fun generateHeatPoints(
        rings: Int = 4,
        pointsPerRing: Int = 8,
        radiusStep: Double = 0.001
    ): List<FestHeatPoint> {

        val result = mutableListOf<FestHeatPoint>()

        // 중심점
        result += FestHeatPoint(
            lat = this.lat,
            lng = this.lng,
            weight = this.weight
        )

        for (ring in 1..rings) {
            val radius = radiusStep * ring

            val weight = (
                    this.weight *
                            (1.0 - ring.toDouble() / (rings + 1))
                    ).coerceAtLeast(0.1)

            repeat(pointsPerRing) { index ->
                val angle = 2 * PI * index / pointsPerRing

                result += FestHeatPoint(
                    lat = this.lat + radius * cos(angle),
                    lng = this.lng + radius * sin(angle),
                    weight = weight
                )
            }
        }

        return result
    }
}
