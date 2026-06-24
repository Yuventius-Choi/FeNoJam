package com.keygul.FeNoJam.ui.view.screen.map.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.keygul.FeNoJam.R
import com.keygul.FeNoJam.domain.model.FestPlace

/**
 * FeNoJam
 * Class: MapSearchView
 * Created by Ven Choi on 2026-06-23
 */
@Composable
fun MapSearchView (
    modifier: Modifier = Modifier,
    searchResults: List<FestPlace> = listOf(),
    onSearch: (String) -> Unit = {},
    onClose: () -> Unit = {},
    onClick: (FestPlace) -> Unit = {},
) {
    Column (
        modifier = modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.8F)),
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
        Text("test")
    }
}