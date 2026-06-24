package com.keygul.FeNoJam.ui.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.keygul.FeNoJam.domain.model.FestPlace
import com.keygul.FeNoJam.utils.exts.format
import java.time.LocalDate

@Composable
fun PlaceCardView (
    modifier: Modifier = Modifier,
    festPlace: FestPlace = FestPlace(),
    selectedDate: LocalDate = LocalDate.now(),
    enableTraffic: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    ElevatedCard (
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        onClick = {
            onClick?.invoke()
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage (
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    model = festPlace.thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
                Column (
                    modifier = Modifier
                        .weight(1F)
                ) {
                    Text(text = "이름:\t${festPlace.name}")
                    Text(text = "개최기간:\t${festPlace.stDate.format()} ~ ${festPlace.enDate.format()}")
                    Text(text = "주소:\t${festPlace.address}")
                }
            }

            if (enableTraffic) {
                val traffics = festPlace.traffics.first { it.date == selectedDate }

                TrafficGradient (
                    modifier = Modifier
                        .fillMaxWidth(),
                    items = traffics.items,
                )
            }
        }
    }
}