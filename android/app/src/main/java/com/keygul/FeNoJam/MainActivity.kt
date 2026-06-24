package com.keygul.FeNoJam

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.keygul.FeNoJam.ui.theme.FeNoJamTheme
import com.keygul.FeNoJam.ui.view.scene.map.MapPlaceDetailScene
import com.keygul.FeNoJam.ui.view.scene.map.MapPlaceSearchScene
import com.keygul.FeNoJam.ui.view.scene.map.MapScene
import com.keygul.FeNoJam.ui.view.screen.map.MapView
import com.keygul.FeNoJam.ui.view.screen.map.search.MapSearchView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val bottomTabs = mapOf(
            Pair(R.drawable.ic_home, getString(R.string.tab_home)),
            Pair(R.drawable.ic_thumb_up, getString(R.string.tab_recommend)),
            Pair(R.drawable.ic_heart, getString(R.string.tab_favorite)),
            Pair(R.drawable.ic_more_h, getString(R.string.tab_more))
        )
        val tabState = mutableIntStateOf(R.drawable.ic_home)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge (
            statusBarStyle = SystemBarStyle.dark(
                scrim = getColor(R.color.main),
            )
        )

        setContent {
            val backStack = remember {
                mutableStateListOf<Any>(MapScene)
            }
            FeNoJamTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    bottomBar = {
                        AnimatedVisibility(
                            visible = backStack.last() is MapScene,
                            enter = slideInVertically(
                                initialOffsetY = { it }
                            ),
                            exit = slideOutVertically(
                                targetOffsetY = { it }
                            )
                        ) {
                            NavigationBar(
                                containerColor = Color.White
                            ) {
                                bottomTabs.forEach { (resId, title) ->
                                    NavigationBarItem (
                                        selected = tabState.intValue == resId,
                                        icon = {
                                            Icon(
                                                modifier = Modifier
                                                    .size(25.dp),
                                                painter = painterResource(resId),
                                                contentDescription = title
                                            )
                                        },
                                        label = {
                                            Text (
                                                text = title,
                                                color = Color.Black,
                                                fontSize = 14.sp,
                                                fontWeight = if (tabState.intValue == resId) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = colorResource(R.color.main),
                                            indicatorColor = Color.Transparent
                                        ),
                                        onClick = {
                                            tabState.intValue = resId
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavDisplay(
                        modifier = Modifier
                            .padding(innerPadding),
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = { key ->
                            when (key) {
                                is MapScene -> NavEntry(key) {
                                    MapView(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        backStack = backStack
                                    )
                                }

                                is MapPlaceSearchScene -> NavEntry(key) {
                                    MapSearchView(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        onClick = { festPlace ->
                                            key.onSelectedPlace?.invoke(festPlace)
                                        },
                                        onClose = { backStack.removeLastOrNull() }
                                    )
                                }

                                is MapPlaceDetailScene -> NavEntry(key) {

                                }
                                else -> NavEntry(Unit) { Text("Unknown route") }
                            }
                        }
                    )
                }
            }
        }
    }
}