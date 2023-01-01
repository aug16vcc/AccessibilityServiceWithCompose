package com.aug16vcc.app.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Rect

@Stable
data class LabeledBoundState(
    val index: Int,
    val rect: Rect,
    val isSelected: Boolean,
)
