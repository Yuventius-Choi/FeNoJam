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
    val weights: List<FestWeightDaily> = listOf(),
    val traffics: List<FestTraffic> = listOf(),
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
}
