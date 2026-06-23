package com.keygul.FeNoJam.ui.view.screen.map

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.BitmapImage
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import com.keygul.FeNoJam.R
import com.keygul.FeNoJam.domain.model.FestPlace
import com.keygul.FeNoJam.domain.model.FestWeightDaily
import com.keygul.FeNoJam.ui.view.components.DateChips
import com.keygul.FeNoJam.ui.view.components.PlaceCardView
import com.keygul.FeNoJam.utils.exts.getFestAsset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate

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
            rotationGesturesEnabled = false,
            zoomControlsEnabled = false
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
            modifier = Modifier
                .fillMaxSize(),
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
            state.festPlaces.forEach { place ->
                key(place.id) {
                    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

                    LaunchedEffect(place.thumbnail) {
                        if (place.thumbnail == null) return@LaunchedEffect
                        val request = ImageRequest.Builder(context)
                            .data(place.thumbnail)
                            .allowHardware(false)
                            .build()
                        val result = context.imageLoader.execute(request)
                        if (result is SuccessResult) {
                            bitmap = (result.image as? BitmapImage)?.bitmap
                        }
                    }

                    val markerState = rememberUpdatedMarkerState(
                        position = LatLng(
                            place.lat,
                            place.lng,
                        )
                    )

                    MarkerComposable (
                        state = markerState,
                        keys = arrayOf(bitmap ?: ""),
                        onClick = { marker ->
                            val selectedPlace: FestPlace? = if (state.selectedFestPlace == place) {
                                null
                            } else {
                                place
                            }
                            vm.onEvent (
                                MapEvent.SelectPlace(selectedPlace)
                            )
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(
                                        marker.position,
                                        selectedPlace?.let { 14F } ?: run { 8F }
                                    ),
                                    durationMs = 1000
                                )
                            }
                            return@MarkerComposable true
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White, shape = CircleShape)
                                .border(3.dp, colorResource(R.color.main), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bitmap != null) {
                                Image(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape),
                                    bitmap = bitmap!!.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray)
                                )
                            }
                        }
                    }
                }
            }
            state.selectedFestPlace?.let { place ->
                val currentDate: LocalDate = state.selectedDate ?: place.weights[0].date
                val weight: FestWeightDaily = place.weights.first { it.date == currentDate }

                val data: List<WeightedLatLng> = weight.heatPoints.map { heatPoint ->
                    WeightedLatLng (
                        LatLng(heatPoint.lat, heatPoint.lng),
                        heatPoint.weight
                    )
                }
                val heatmapProvider = remember(weight.heatPoints) {
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
        state.selectedFestPlace?.let { place ->
            state.selectedDate?.let { currentDate ->
                // 상단 Date
                DateChips (
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    items = place.weights.map { it.date },
                    currentDate = currentDate
                ) {
                    vm.onEvent (
                        MapEvent.SelectDate(it)
                    )
                }

                // 하단 CardView
                PlaceCardView (
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    festPlace = place,
                    selectedDate = currentDate
                )
            }
        }

        LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
            vm.onEvent (
                MapEvent.LoadSamples(jsonString = context.getFestAsset("sample.json"))
            )
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