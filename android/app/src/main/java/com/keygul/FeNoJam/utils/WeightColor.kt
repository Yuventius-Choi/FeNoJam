package com.keygul.FeNoJam.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.keygul.FeNoJam.R

/**
 * FeNoJam
 * Class: WeightColor
 * Created by Ven Choi on 2026-06-27
 */
@Composable
fun getWeightColor(weights: Weights): Color = colorResource(weights.resId)

enum class Weights(val resId: Int) {
    SMOOTH(resId = R.color.txt_color_green),
    NORMAL(resId = R.color.txt_color_blue),
    CAUTION(resId = R.color.txt_color_yellow),
    CONFUSION(resId = R.color.txt_color_red)
}