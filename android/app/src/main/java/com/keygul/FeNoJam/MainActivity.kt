package com.keygul.FeNoJam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.keygul.FeNoJam.ui.theme.FeNoJamTheme
import com.keygul.FeNoJam.ui.view.screen.map.MapView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeNoJamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MapView (
                        modifier = Modifier
                            .padding(innerPadding),
                    )
                }
            }
        }
    }
}