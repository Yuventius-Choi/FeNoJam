package com.keygul.FeNoJam.utils.exts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/**
 * FeNoJam
 * Class: ModifierExt
 * Created by Ven Choi on 2026-06-27
 */
@Composable
fun Modifier.noRippleClickable (
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = this.clickable (
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onClick = onClick
)