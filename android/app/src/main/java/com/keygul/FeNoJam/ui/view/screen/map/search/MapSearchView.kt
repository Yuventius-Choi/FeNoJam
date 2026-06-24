package com.keygul.FeNoJam.ui.view.screen.map.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keygul.FeNoJam.R
import com.keygul.FeNoJam.domain.model.FestPlace
import com.keygul.FeNoJam.ui.view.components.HintBasicTextField
import com.keygul.FeNoJam.ui.view.components.PlaceCardView
import com.keygul.FeNoJam.ui.view.scene.map.MapPlaceSearchScene
import com.keygul.FeNoJam.utils.exts.getFestAsset
import kotlinx.coroutines.flow.debounce
import org.orbitmvi.orbit.compose.collectAsState

/**
 * FeNoJam
 * Class: MapSearchView
 * Created by Ven Choi on 2026-06-23
 */
@Composable
fun MapSearchView (
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onClick: (FestPlace) -> Unit = {},
    vm: MapSearchVM = viewModel()
) {
    val state by vm.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var searchText by remember { mutableStateOf("") }

    Column (
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.End),
        ) {
            IconButton(
                onClick = onClose,
                shape = CircleShape
            ) {
                Icon (
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Close"
                )
            }
        }
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .background(Color.White, RoundedCornerShape(size = 16.dp))
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
            HintBasicTextField (
                modifier = Modifier.weight(1F),
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                hint = stringResource(R.string.txt_hint_festival_search)
            )
        }

        if (state.filteredFestPlaces.isEmpty()) {
            Text (
                text = stringResource(R.string.txt_festival_search_not_exist)
            )
        } else {
            Column (
                modifier = Modifier
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.filteredFestPlaces.forEach { festPlace ->
                    PlaceCardView (
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        festPlace = festPlace,
                        enableTraffic = false,
                        onClick = {
                            onClick(festPlace)
                            focusManager.clearFocus()
                            searchText = ""
                            onClose.invoke()
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (state.festPlaces.isEmpty()) {
            vm.onEvent (
                MapSearchEvent.LoadSamples(jsonString = context.getFestAsset("sample.json"))
            )
        }

    }

    LaunchedEffect(searchText) {
        snapshotFlow { searchText }
            .debounce(300)
            .collect { query ->
                vm.onEvent(MapSearchEvent.SearchPlace(searchText))
            }
    }
}