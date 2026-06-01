package com.keygul.FeNoJam.ui.view.screen.map

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import com.keygul.FeNoJam.domain.model.FestMap
import com.keygul.FeNoJam.ui.view.components.PlaceCardView
import com.keygul.FeNoJam.utils.exts.getFestAsset
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MapView (
    modifier: Modifier = Modifier,
    vm: MapVM = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val state by vm.collectAsState()

    val koreaBounds = LatLngBounds(
        LatLng(33.0, 124.0),
        LatLng(43.0, 132.0)
    )
    val koreaCenterPos = LatLng(37.5, 127.5)
    val mapProperties = MapProperties(
        latLngBoundsForCameraTarget = koreaBounds,
        minZoomPreference = 6F,
        maxZoomPreference = 14F
    )
    val uiSettings by remember {
        mutableStateOf(MapUiSettings(
            tiltGesturesEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = false
        ))
    }


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            koreaCenterPos,
            6F
        )
    }

    Box (
        modifier = modifier
            .fillMaxSize()
    ) {
        GoogleMap (
            modifier = modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = mapProperties,
            onMapLoaded = {
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngBounds(
                        koreaBounds,
                        100
                    )
                )
            }
        ) {
            state.festMaps.forEach { map ->
                val markerState = rememberUpdatedMarkerState(
                    position = LatLng(
                        map.festPlace.lat,
                        map.festPlace.lng,
                    )
                )
                Marker(
                    state = markerState,
                    icon = BitmapDescriptorFactory.defaultMarker (
                        if (state.selectedFestMap == map) {
                            BitmapDescriptorFactory.HUE_RED
                        } else {
                            BitmapDescriptorFactory.HUE_GREEN
                        }
                    ),
                    onClick = { marker ->
                        val map: FestMap? = if (state.selectedFestMap == map) {
                            null
                        } else {
                            map
                        }
                        vm.onEvent (
                            MapEvent.SelectPlace (map)
                        )
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(
                                    marker.position,
                                    map?.let { 14F } ?: run { 8F }
                                ),
                                durationMs = 1000
                            )
                        }
                        return@Marker true
                    }
                )
            }
            state.selectedFestMap?.let {
                val data: List<WeightedLatLng> = it.festHeatPoints.map { heatPoint ->
                    WeightedLatLng (
                        LatLng(heatPoint.lat, heatPoint.lng),
                        heatPoint.weight
                    )
                }
                val heatmapProvider = remember(it.festHeatPoints) {
                    HeatmapTileProvider.Builder()
                        .weightedData(data)
                        .radius(50)
                        .build()
                }

                TileOverlay (
                    tileProvider = heatmapProvider,
                    fadeIn = true
                )
            }
        }
        state.selectedFestMap?.let { map ->
            PlaceCardView (
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                festPlace = map.festPlace
            )
        }
        if (state.festMaps.isEmpty()) {
            Button (
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                onClick = {
                    vm.onEvent (
                        MapEvent.LoadSamples(jsonString = context.getFestAsset("sample.json"))
                    )
                }
            ) {
                Text(text = "LOAD")
            }
        }
    }

    vm.collectSideEffect {
        when (it) {
            is MapSideEffect.Toast -> {
                Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}