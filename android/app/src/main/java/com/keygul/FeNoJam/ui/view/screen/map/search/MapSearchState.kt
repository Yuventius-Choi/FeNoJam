package com.keygul.FeNoJam.ui.view.screen.map.search

import com.keygul.FeNoJam.domain.model.FestPlace
import java.time.LocalDate

/**
 * FeNoJam
 * Class: MapSearchState
 * Created by Ven Choi on 2026-06-24
 */
data class MapSearchState (
    val festPlaces: List<FestPlace> = emptyList(),
    val filteredFestPlaces: List<FestPlace> = emptyList(),
    val selectedFestPlace: FestPlace? = null
)

sealed class MapSearchEvent {
    data class LoadSamples (
        val jsonString: String
    ): MapSearchEvent()
    data class SearchPlace(val query: String): MapSearchEvent()
    data class SelectPlace(val place: FestPlace? = null): MapSearchEvent()
    data class SelectDate(val date: LocalDate): MapSearchEvent()
}

sealed class MapSearchSideEffect {

}