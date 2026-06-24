package com.keygul.FeNoJam.ui.view.scene.map

import com.keygul.FeNoJam.domain.model.FestPlace

/**
 * FeNoJam
 * Class: MapScene
 * Created by Ven Choi on 2026-06-24
 */
data object MapScene
data class MapPlaceSearchScene(val onSelectedPlace: ((FestPlace) -> Unit)? = null)
data class MapPlaceDetailScene(val place: FestPlace)