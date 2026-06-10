package com.keygul.FeNoJam.domain.model

import java.time.LocalDate

data class FestTraffic(
    val date: LocalDate = LocalDate.now(),
    val items: List<FestTrafficItem> = listOf()
)
