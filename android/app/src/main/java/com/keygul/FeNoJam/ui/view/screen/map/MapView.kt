package com.keygul.FeNoJam.ui.view.screen.map

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.BitmapImage
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
import com.keygul.FeNoJam.ui.view.scene.map.MapPlaceSearchScene
import com.keygul.FeNoJam.ui.view.screen.map.search.MapSearchView
import com.keygul.FeNoJam.utils.exts.getFestAsset
import com.keygul.FeNoJam.utils.exts.noRippleClickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MapView (
    modifier: Modifier = Modifier,
    backStack: SnapshotStateList<Any>,
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

    var activatedSearchFlag by remember { mutableStateOf<Boolean>(false) }

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
//            onMapLoaded = {
//                if (state.selectedFestPlace == null) {
//                    cameraPositionState.move(
//                        CameraUpdateFactory.newLatLngBounds(
//                            koreaBounds,
//                            100
//                        )
//                    )
//                }
//            }
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
                            if (selectedPlace == null) {
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.zoomTo(
                                            8F
                                        ),
                                        durationMs = 1000
                                    )
                                }
                            }
                            vm.onEvent (
                                MapEvent.SelectPlace(selectedPlace)
                            )
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
                                Image(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape),
                                    painter = painterResource(R.drawable.logo),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
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
        } ?: run {
            if (state.festPlaces.isNotEmpty()) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(Color.White, RoundedCornerShape(size = 16.dp))
                        .noRippleClickable {
                            activatedSearchFlag = true
                            backStack.add (
                                MapPlaceSearchScene (onSelectedPlace = { festPlace ->
                                    vm.onEvent(MapEvent.SelectPlace(festPlace))
                                })
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val contentColor = Color.LightGray

                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = contentColor
                    )
                    Text (
                        modifier = Modifier.
                        weight(1F),
                        text = stringResource(R.string.txt_search),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngBounds(
                    koreaBounds,
                    100
                )
            )
            if (state.festPlaces.isEmpty()) {
                vm.onEvent (
                    MapEvent.LoadSamples(jsonString = context.getFestAsset("sample.json"))
                )
            }
        }

        LaunchedEffect(state.selectedFestPlace) {
            state.selectedFestPlace?.let { place ->
                if (activatedSearchFlag) {
                    cameraPositionState.move(
                        update = CameraUpdateFactory.newLatLngZoom(
                            LatLng(place.lat, place.lng),
                            14F
                        )
                    )
                    activatedSearchFlag = false
                } else {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(
                                LatLng(place.lat, place.lng),
                                14F
                            ),
                            durationMs = 1000
                        )
                    }
                }
            }
        }
    }

    BackHandler (
        enabled = state.selectedFestPlace != null
    ) {
        vm.onEvent(MapEvent.SelectPlace(null))
        coroutineScope.launch {
            cameraPositionState.animate(
                update = CameraUpdateFactory.zoomTo(
                    8F
                ),
                durationMs = 1000
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