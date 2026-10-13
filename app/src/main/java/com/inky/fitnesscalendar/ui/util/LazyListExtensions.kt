package com.inky.fitnesscalendar.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.translate
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


/**
 * A sticky header that can additionally render a body that is non-sticky (collapsible).
 * Must be placed inside a call to [androidx.compose.foundation.lazy.LazyListScope.stickyHeader]
 */
@Composable
fun CollapsibleStickyHeader(
    state: LazyListState,
    index: Int,
    contentHeader: @Composable ColumnScope.() -> Unit,
    contentBody: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    padding: PaddingValues = PaddingValues()
) {
    // Works by translating the body down the amount of pixels it would have moved if it was non-sticky.
    // Additionally, the next sticky header can bump into the (now clipped) body and move the header out of view.
    // To counteract this, the header is also translated, but downwards
    val layout by remember(state, index) {
        derivedStateOf {
            val item = state.layoutInfo.visibleItemsInfo.firstOrNull() ?: return@derivedStateOf null
            if (item.index != index) return@derivedStateOf null
            val nextItem = state.layoutInfo.visibleItemsInfo.find { it.index == index + 1 }
                ?: return@derivedStateOf HeaderLayout(
                    coverPx = Integer.MAX_VALUE,
                    offset = item.offset
                )

            val cover = (item.size - (nextItem.offset - item.offset)).coerceAtLeast(0)
            HeaderLayout(coverPx = cover, offset = item.offset)
        }
    }
    var bodyHeight by remember { mutableIntStateOf(0) }
    // The amount of pixels by which the header should be shifted down.
    // It is the negative of the offset applied by compose, but limited to at most the body height, so
    // that it can actually move out of the view once the next sticky header arrives
    val headerOffset = (-(layout?.offset ?: 0)).coerceIn(0, bodyHeight)

    Column(
        modifier = Modifier
            .padding(padding)
            .graphicsLayer {
                // Make sure to clip the column to only the visible part, so that e.g.
                // click events work correctly
                // So the bottom part must be cut by the amount the body is moved
                // and the upper part must be moved down by the amount the header is moved
                clip = true
                this.shape = CroppedShape(
                    shape,
                    headerOffset,
                    (layout?.coverPx ?: 0).coerceAtMost(bodyHeight - headerOffset)
                )
            }
            .then(modifier)
    ) {
        CollapsibleHeaderHeader(headerOffset) {
            contentHeader()
        }
        CollapsibleHeaderBody(
            cover = layout?.coverPx,
            onSizeChanged = { bodyHeight = it.height }
        ) {
            contentBody()
        }
    }
}

@Composable
private fun CollapsibleHeaderHeader(
    visibleHeight: Int,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.graphicsLayer {
            translationY = visibleHeight.toFloat()

        }
    ) {
        content()
    }
}

@Composable
private fun CollapsibleHeaderBody(
    cover: Int?,
    onSizeChanged: (IntSize) -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .graphicsLayer {
                if (cover != null) {
                    translationY = -cover.toFloat()
                }
            }
            .zIndex(-1f)
            .onSizeChanged(onSizeChanged)
    ) {
        content()
    }
}

/**
 * @param coverPx: The amount of pixels by which the body should be shifted up
 * @param offset: The offset of the header caused by the compose sticky header implementation
 */
private data class HeaderLayout(
    val coverPx: Int,
    val offset: Int
)

/**
 * A wrapper class around the [wrapped] shape.
 * The shape is vertically offset by [offsetY] pixels and the
 * height is reduced by [croppedHeight] pixels.
 */
private class CroppedShape(
    private val wrapped: Shape,
    private val offsetY: Int,
    private val croppedHeight: Int
) :
    Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val outline = wrapped.createOutline(
            size.copy(height = size.height - offsetY - croppedHeight),
            layoutDirection,
            density
        )

        val offset = Offset(0f, offsetY.toFloat())
        return when (outline) {
            is Outline.Generic -> Outline.Generic(outline.path.apply { translate(offset) })
            is Outline.Rounded -> Outline.Rounded(outline.roundRect.translate(offset))
            is Outline.Rectangle -> Outline.Rectangle(outline.rect.translate(offset))
        }
    }
}

@Preview
@Composable
private fun PreviewCollapsibleHeaders() {
    val state = rememberLazyListState()
    LazyColumn(
        state = state,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .height(256.dp)
    ) {
        for (i in 0..6) {
            stickyHeader { index ->
                CollapsibleStickyHeader(
                    state = state,
                    index = index,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    padding = PaddingValues(8.dp),
                    contentHeader = {
                        Text(
                            "Hello world no. $i",
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .fillMaxWidth()
                        )
                    },
                    contentBody = {
                        Text("Some\nLong\nText")
                    }
                )
            }

            for (j in 0..10) {
                item(key = "item-$i-$j") {
                    Text("-----item-$j")
                }
            }
        }
    }
}