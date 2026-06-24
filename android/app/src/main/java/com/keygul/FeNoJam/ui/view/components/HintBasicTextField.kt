package com.keygul.FeNoJam.ui.view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * FeNoJam
 * Class: HintBasicTextField
 * Created by Ven Choi on 2026-06-24
 */
@Composable
fun HintBasicTextField (
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String
) {
    BasicTextField (
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        decorationBox = { innerTextField ->
            Box (
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        color = Color.LightGray
                    )
                }
                innerTextField()
            }
        }
    )
}