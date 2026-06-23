package com.keygul.FeNoJam

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygul.FeNoJam.ui.theme.FeNoJamTheme
import com.keygul.FeNoJam.ui.view.screen.map.MapView

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
        enableEdgeToEdge()

        setContent {
            FeNoJamTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
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
                ) { innerPadding ->
                    MapView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }
            }
        }
    }
}