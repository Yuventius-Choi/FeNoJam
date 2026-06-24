package com.keygul.FeNoJam.ui.view.screen.map.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keygul.FeNoJam.data.model.FestPlaceDto
import com.keygul.FeNoJam.data.model.FestPlaceMapper
import com.keygul.FeNoJam.domain.model.FestPlace
import com.keygul.FeNoJam.ui.view.screen.map.MapEvent
import com.keygul.FeNoJam.ui.view.screen.map.MapSideEffect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

/**
 * FeNoJam
 * Class: MapSearchVM
 * Created by Ven Choi on 2026-06-24
 */
class MapSearchVM (
    savedStateHandle: SavedStateHandle
): ContainerHost<MapSearchState, Any>, ViewModel() {
    override val container: Container<MapSearchState, Any> = container(MapSearchState())

    fun onEvent(event: MapSearchEvent) {
        viewModelScope.launch {
            when (event) {
                is MapSearchEvent.LoadSamples -> addSamplePlaces(event.jsonString)
                is MapSearchEvent.SearchPlace -> {
                    filterPlaces(event.query)
                }
                is MapSearchEvent.SelectPlace -> {

                }
                is MapSearchEvent.SelectDate -> {

                }
            }
        }
    }

    private fun filterPlaces(query: String) = intent {
        reduce {
            if (query.isBlank()) {
                state.copy(filteredFestPlaces = listOf())
            } else {
                state.copy(
                    filteredFestPlaces = state.festPlaces.filter { place ->
                        place.name.contains(query)
                    }
                )
            }
        }
    }

    private fun addSamplePlaces (
        jsonString: String
    ) = intent {
        val jsonParser = Json { ignoreUnknownKeys = true }
        val response = jsonParser.decodeFromString<List<FestPlaceDto>>(jsonString)

        val result: List<FestPlace> = response.map {
            FestPlaceMapper.toDomain(it)
        }

        reduce {
            state.copy(festPlaces = result)
        }
    }
}