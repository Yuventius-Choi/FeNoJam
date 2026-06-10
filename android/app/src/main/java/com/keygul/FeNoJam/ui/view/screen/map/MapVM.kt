package com.keygul.FeNoJam.ui.view.screen.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keygul.FeNoJam.data.model.FestPlaceDto
import com.keygul.FeNoJam.data.model.FestPlaceMapper
import com.keygul.FeNoJam.domain.model.FestPlace
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class MapVM (
    savedStateHandle: SavedStateHandle
): ContainerHost<MapState, MapSideEffect>, ViewModel() {
    override val container: Container<MapState, MapSideEffect> =
        container(MapState())

    fun onEvent(event: MapEvent) {
        viewModelScope.launch {
            when (event) {
                is MapEvent.LoadSamples -> addSamplePlaces(event.jsonString)
                is MapEvent.SelectPlace -> {
                    selectPlace(event.place)
                }
            }
        }
    }

    private fun selectPlace(festPlace: FestPlace?) = intent {
        reduce {
            state.copy(
                selectedFestPlace = festPlace,
                selectedDate = festPlace?.weights[0]?.date
            )
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

        postSideEffect(MapSideEffect.Toast(msg = "데이터 로드 완료"))
    }
}