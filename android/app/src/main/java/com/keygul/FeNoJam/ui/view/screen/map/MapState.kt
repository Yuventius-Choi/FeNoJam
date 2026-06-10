package com.keygul.FeNoJam.ui.view.screen.map

import com.keygul.FeNoJam.domain.model.FestPlace
import java.time.LocalDate

data class MapState (
    val festPlaces: List<FestPlace> = emptyList(),
    val selectedFestPlace: FestPlace? = null,
    val selectedDate: LocalDate? = null
)

sealed class MapEvent {
    data class LoadSamples (
        val jsonString: String
    ): MapEvent()
    data class SelectPlace(val place: FestPlace? = null): MapEvent()
    data class SelectDate(val date: LocalDate): MapEvent()
}

sealed class MapSideEffect {
    data class Toast(val msg: String): MapSideEffect()
}