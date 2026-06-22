package com.keygul.FeNoJam.domain.model

import java.time.LocalDateTime

data class FestTrafficItem (
    val datetime: LocalDateTime = LocalDateTime.now(),
    val weight: Double = 0.0
)