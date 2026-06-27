package com.keygul.FeNoJam.ui.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.keygul.FeNoJam.R
import com.keygul.FeNoJam.domain.model.FestPlace
import com.keygul.FeNoJam.utils.Patterns
import com.keygul.FeNoJam.utils.exts.format
import java.time.LocalDate

@Composable
fun PlaceCardView (
    modifier: Modifier = Modifier,
    festPlace: FestPlace = FestPlace(),
    selectedDate: LocalDate? = null,
    enableTraffic: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    ElevatedCard (
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.DarkGray,
            contentColor = Color.White
        ),
        onClick = {
            onClick?.invoke()
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (festPlace.thumbnail.isNullOrBlank()) {
                    Image (
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(width = 1.dp, color = colorResource(R.color.main), shape = RoundedCornerShape(4.dp)),
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null,
                    )
                } else {
                    AsyncImage (
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(width = 1.dp, color = colorResource(R.color.main), shape = RoundedCornerShape(4.dp)),
                        model = festPlace.thumbnail,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
                Column (
                    modifier = Modifier
                        .weight(1F),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text (
                        text = festPlace.name,
                        fontWeight = FontWeight.Black
                    )
                    Text (
                        text = "${festPlace.stDate.format(Patterns.MM_DD_KR)} ~ ${festPlace.enDate.format(Patterns.MM_DD_KR)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Thin,
                        color = Color.LightGray
                    )
                    Text (
                        modifier = Modifier
                            .padding(top = 4.dp),
                        text = festPlace.address,
                        fontSize = 14.sp
                    )
                }
                if (selectedDate != null) {
                    val festWeight = festPlace.weights.first { it.date == selectedDate }.weight
                    val confusionText = when (festWeight) {
                        in 0.0..0.25 -> stringResource(R.string.txt_smooth)
                        in 0.25..0.5 -> stringResource(R.string.txt_normal)
                        in 0.5..0.75 -> stringResource(R.string.txt_caution)
                        else -> stringResource(R.string.txt_confusion)
                    }

                    val confusionColor = when (festWeight) {
                        in 0.0..0.25 -> colorResource(R.color.txt_color_green)
                        in 0.25..0.5 -> colorResource(R.color.txt_color_blue)
                        in 0.5..0.75 -> colorResource(R.color.txt_color_yellow)
                        else -> colorResource(R.color.txt_color_red)
                    }

                    Text (
                        modifier = Modifier
                            .background(confusionColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .align(Alignment.Top),
                        text = confusionText,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (enableTraffic) {
                val traffics = festPlace.traffics.first { it.date == selectedDate }

                Text (
                    text = stringResource(R.string.txt_predict_traffic),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                TrafficGradient (
                    modifier = Modifier
                        .fillMaxWidth(),
                    items = traffics.items,
                )
            }
        }
    }
}