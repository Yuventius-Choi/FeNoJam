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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygul.FeNoJam.R
import com.keygul.FeNoJam.domain.model.FestTrafficItem
import com.keygul.FeNoJam.utils.Weights
import com.keygul.FeNoJam.utils.getWeightColor

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
                color = Color.White,
                fontSize = 10.sp
            )
            Spacer(Modifier.weight(1F))
            Text (
                text = stringResource(R.string.txt_hour_6),
                color = Color.White,
                fontSize = 10.sp
            )
            Spacer(Modifier.weight(1F))
            Text (
                text = stringResource(R.string.txt_hour_12),
                color = Color.White,
                fontSize = 10.sp
            )
            Spacer(Modifier.weight(1F))
            Text (
                text = stringResource(R.string.txt_hour_18),
                color = Color.White,
                fontSize = 10.sp
            )
            Spacer(Modifier.weight(1F))
            Text (
                text = stringResource(R.string.txt_hour_23),
                color = Color.White,
                fontSize = 10.sp
            )
        }

    }
}

@Composable
fun trafficColor(weight: Double): Color {
    return when {
        // 1단계: 0.0 ~ 0.25 (SMOOTH -> NORMAL)
        weight < 0.25 -> lerp(
            getWeightColor(Weights.SMOOTH),
            getWeightColor(Weights.NORMAL),
            (weight * 4).toFloat()
        )
        // 2단계: 0.25 ~ 0.5 (NORMAL -> CAUTION)
        weight < 0.5 -> lerp(
            getWeightColor(Weights.NORMAL),
            getWeightColor(Weights.CAUTION),
            ((weight - 0.25) * 4).toFloat()
        )
        // 3단계: 0.5 ~ 0.75 (CAUTION -> CONFUSION)
        weight < 0.75 -> lerp(
            getWeightColor(Weights.CAUTION),
            getWeightColor(Weights.CONFUSION),
            ((weight - 0.5) * 4).toFloat()
        )
        // 0.75 ~ 1.0 (CONFUSION 고정)
        else -> getWeightColor(Weights.CONFUSION)
    }
}