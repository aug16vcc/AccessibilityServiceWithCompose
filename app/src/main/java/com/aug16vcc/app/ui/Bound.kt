package com.aug16vcc.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalStatusBarHeight
import kotlin.math.max

@Composable
fun LabeledBound(
    state: LabeledBoundState,
    modifier: Modifier = Modifier,
    unselectedBoundingColor: Color = if (isSystemInDarkTheme()) Color.White else Color.Black,
    unselectedLabelColor: Color = if (isSystemInDarkTheme()) Color.Black else Color.White,
    selectedBoundingColor: Color = MaterialTheme.colors.primary,
    selectedLabelColor: Color = MaterialTheme.colors.onPrimary,
) {
    val (index, rect, isSelected) = state
    val density = LocalDensity.current
    val statusBarHeight = LocalStatusBarHeight.current
    var sizeTopBar by remember { mutableStateOf(IntSize.Zero) }
    var positionInRoot by remember { mutableStateOf(Offset.Zero) }
    val boundingColor = if (isSelected) selectedBoundingColor else unselectedBoundingColor
    val labelColor = if (isSelected) selectedLabelColor else unselectedLabelColor
    val boundingWidth = if (isSelected) 9f else 3f
    Layout(
        content = {
            Text(
                text = "$index",
                modifier = Modifier
                    .wrapContentSize()
                    .background(boundingColor, CircleShape)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val c = max(placeable.width, placeable.height)
                        layout(c, c) {
                            placeable.placeRelative(
                                x = (c - placeable.width) / 2,
                                y = (c - placeable.height) / 2
                            )
                        }
                    },
                style = TextStyle.Default.copy(color = labelColor),
            )
        },
        modifier = Modifier
            .offset {
                IntOffset(rect.left.toInt(), rect.top.toInt() - statusBarHeight)
            }
            .then(modifier)
            .size((rect.width / density.density).dp, (rect.height / density.density).dp)
            .drawBehind {
                drawRect(boundingColor, style = Stroke(boundingWidth))
            }
            .onGloballyPositioned {
                sizeTopBar = it.size
                positionInRoot = it.positionInRoot()
            }
    ) { measurables, constraints ->
        val minConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeable = measurables.first().measure(minConstraints)

        layout(minConstraints.maxWidth, minConstraints.maxHeight) {
            placeable.placeRelative(x = -placeable.width / 4, -placeable.height / 4)
        }
    }
}

@Preview
@Composable
internal fun PreviewLabeledBound() {
    Box(
        modifier = Modifier
            .background(Color.White)
            .size(120.dp),
    ) {
        LabeledBound(
            state = LabeledBoundState(
                index = 1,
                rect = Rect(
                    Offset(56f, 64f), Size(
                        56f, 64f
                    )
                ),
                isHighlighted = true
            ),
        )
    }
}
