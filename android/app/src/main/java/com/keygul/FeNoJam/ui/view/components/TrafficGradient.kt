package com.keygul.FeNoJam.ui.view.components

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygul.FeNoJam.R
import com.keygul.FeNoJam.domain.model.FestTrafficItem

@Composable
fun TrafficGradient (
    modifier: Modifier = Modifier,
    items: List<FestTrafficItem>
) {
    if (items.size != 24) {
        throw RuntimeException("Traffic item size must be 24")
    }

    val colors = items.map {
        trafficColor(it.weight)
    }
    val brush = Brush.horizontalGradient(
        colors = colors
    )

    Box (
        modifier = modifier
            .background(
                brush = brush,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row (
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text (
                text = stringResource(R.string.txt_hour_0),
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.weight(1F))
            Text (
                text = stringResource(R.string.txt_hour_6),
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.weight(1F))
            Text (
                text = stringResource(R.string.txt_hour_12),
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.weight(1F))
            Text (
                text = stringResource(R.string.txt_hour_18),
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.weight(1F))
            Text (
                text = stringResource(R.string.txt_hour_23),
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }

    }
}

@Composable
fun trafficColor(weight: Double): Color {

    return when {
        weight < 0.1 -> lerp(
            colorResource(R.color.traffic_color_1),
            colorResource(R.color.traffic_color_2),
            (weight / 0.1).toFloat()
        )

        weight < 0.2 -> lerp(
            colorResource(R.color.traffic_color_2),
            colorResource(R.color.traffic_color_3),
            ((weight - 0.1) / 0.1).toFloat()
        )

        weight < 0.3 -> lerp(
            colorResource(R.color.traffic_color_3),
            colorResource(R.color.traffic_color_4),
            ((weight - 0.2) / 0.1).toFloat()
        )

        weight < 0.4 -> lerp(
            colorResource(R.color.traffic_color_4),
            colorResource(R.color.traffic_color_5),
            ((weight - 0.3) / 0.1).toFloat()
        )

        weight < 0.5 -> lerp(
            colorResource(R.color.traffic_color_5),
            colorResource(R.color.traffic_color_6),
            ((weight - 0.4) / 0.1).toFloat()
        )

        weight < 0.6 -> lerp(
            colorResource(R.color.traffic_color_6),
            colorResource(R.color.traffic_color_7),
            ((weight - 0.5) / 0.1).toFloat()
        )

        weight < 0.7 -> lerp(
            colorResource(R.color.traffic_color_7),
            colorResource(R.color.traffic_color_8),
            ((weight - 0.6) / 0.1).toFloat()
        )

        weight < 0.8 -> lerp(
            colorResource(R.color.traffic_color_8),
            colorResource(R.color.traffic_color_9),
            ((weight - 0.7) / 0.1).toFloat()
        )

        weight < 0.9 -> lerp(
            colorResource(R.color.traffic_color_9),
            colorResource(R.color.traffic_color_10),
            ((weight - 0.8) / 0.1).toFloat()
        )

        else -> colorResource(R.color.traffic_color_10)
    }
}