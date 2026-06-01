package com.keygul.FeNoJam.ui.view.screen.map

import com.keygul.FeNoJam.domain.model.FestMap

data class MapState (
    val festMaps: List<FestMap> = emptyList(),
    val selectedFestMap: FestMap? = null
)

sealed class MapEvent {
    data class LoadSamples (
        val jsonString: String
    ): MapEvent()
    data class SelectPlace(val place: FestMap? = null): MapEvent()

}

sealed class MapSideEffect {
    data class Toast(val msg: String): MapSideEffect()
}