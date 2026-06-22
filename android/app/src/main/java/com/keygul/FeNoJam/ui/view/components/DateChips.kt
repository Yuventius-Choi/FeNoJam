package com.keygul.FeNoJam.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.keygul.FeNoJam.utils.exts.format
import java.time.LocalDate

@Composable
fun DateChips (
    modifier: Modifier = Modifier,
    items: List<LocalDate>,
    currentDate: LocalDate,
    onChipClicked: (LocalDate) -> Unit
) {
    LazyRow (
        modifier = modifier,
    ) {
        items(items) { item ->
            DateChip(
                modifier = Modifier.padding(horizontal = 4.dp),
                item = item,
                isSelected = item == currentDate,
                onChipClicked = onChipClicked
            )
        }
    }
}

@Composable
fun DateChip (
    modifier: Modifier,
    item: LocalDate,
    isSelected: Boolean,
    onChipClicked: (LocalDate) -> Unit
) {
    Surface(
        modifier = modifier
            .clickable {
                onChipClicked(item)
            },
        shape = RoundedCornerShape(100.dp),
        color = Color.White,
    ) {
        Text (
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            text = item.format(),
            color = if (isSelected) Color.Black else Color.LightGray
        )
    }
}
