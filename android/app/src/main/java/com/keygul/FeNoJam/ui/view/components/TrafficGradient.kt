package com.keygul.FeNoJam.ui.view.components

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
import androidx.compose.ui.unit.dp
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
                .padding(4.dp)
        ) {
            Text("0시")
            Spacer(Modifier.weight(1F))
            Text("12시")
            Spacer(Modifier.weight(1F))
            Text("23시")
        }

    }
}

fun trafficColor(weight: Double): Color {
    return when {
        weight < 0.5 -> lerp (
            Color.Green,
            Color.Yellow,
            (weight * 2).toFloat()
        )
        else -> lerp (
            Color.Yellow,
            Color.Red,
            ((weight - 0.5) * 2).toFloat()
        )
    }
}