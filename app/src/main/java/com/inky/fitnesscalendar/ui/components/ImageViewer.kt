package com.inky.fitnesscalendar.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import coil3.compose.AsyncImage
import com.inky.fitnesscalendar.BuildConfig
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.util.NonEmptyList
import com.inky.fitnesscalendar.util.copyFileToStorage
import com.inky.fitnesscalendar.util.getOrCreateSharedMediaCache
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs


@Composable
fun ImageViewer(
    imageUris: NonEmptyList<Uri>,
    initialImageIndex: Int,
    onDismiss: () -> Unit,
    onDelete: ((Uri) -> Unit)? = null,
) {
    val state = rememberPagerState(initialPage = initialImageIndex) { imageUris.size }
    val currentUri = imageUris[state.currentPage]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            val context = LocalContext.current

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                if (onDelete != null) {
                    IconButton(onClick = { onDelete(currentUri) }) {
                        Icons.Delete(stringResource(R.string.delete))
                    }
                }

                IconButton(onClick = { context.shareImage(currentUri) }) {
                    Icons.Share(stringResource(R.string.share))
                }

                IconButton(onClick = onDismiss) {
                    Icons.Close(stringResource(R.string.close))
                }
            }

            HorizontalPager(state, pageSpacing = 8.dp) { index ->
                InteractiveImage(imageUris[index])
            }
        }
    }
}

val NORMAL_BOUNDS = 1.0f..3.0f
val EXTREME_BOUNDS = 0.5f..5.0f

const val OFFSET_BOUNDS = 0f

private fun clampAxis(value: Float, size: Float, visible: Float): Float {
    val slack = visible * OFFSET_BOUNDS
    val min = -slack
    val max = size - visible + slack
    return if (min <= max) value.coerceIn(min, max) else (size - visible) / 2
}

private fun Offset.clampToBounds(size: Size, zoom: Float): Offset {
    val visible = size / zoom
    return Offset(
        clampAxis(x, size.width, visible.width),
        clampAxis(y, size.height, visible.height),
    )
}

@Composable
fun InteractiveImage(uri: Uri) {
    var zoom by remember(uri) { mutableFloatStateOf(1f) }
    var offset by remember(uri) { mutableStateOf(Offset.Zero) }

    var size by remember { mutableStateOf(Size.Zero) }

    val transformState = rememberTransformableState { centroid, zoomChange, panChange, _ ->
        val oldScale = zoom
        zoom *= zoomChange
        zoom = zoom.coerceIn(EXTREME_BOUNDS)
        // If the centroid isn't specified, assume it should be applied from the center
        val effectiveCentroid = centroid.takeIf { it.isSpecified } ?: size.center
        val rawOffset =
            (offset + effectiveCentroid / oldScale) - (effectiveCentroid / zoom + panChange / oldScale)
        offset = rawOffset.clampToBounds(size, zoom)
    }

    LaunchedEffect(transformState.isTransformInProgress) {
        if (transformState.isTransformInProgress) return@LaunchedEffect

        coroutineScope {
            if (!NORMAL_BOUNDS.contains(zoom)) {
                launch {
                    animate(
                        initialValue = zoom,
                        targetValue = zoom.coerceIn(NORMAL_BOUNDS),
                    ) { value, _ ->
                        zoom = value
                        offset = offset.clampToBounds(size, zoom)
                    }
                }
            }

            if (zoom < NORMAL_BOUNDS.start) {
                launch {
                    animate(
                        Offset.VectorConverter,
                        initialValue = offset,
                        targetValue = Offset.Zero
                    ) { value, _ ->
                        offset = value
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .onSizeChanged { size = it.toSize() }
            .transformable(transformState, canPan = { pan ->
                if (zoom <= 1f) return@transformable false
                // Leave horizontal swipes to the pager once the image is scrolled to that edge
                val pannedX = (offset - pan / zoom).clampToBounds(size, zoom).x
                abs(pan.x) <= abs(pan.y) || pannedX != offset.x
            }),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = uri,
            contentDescription = stringResource(R.string.user_uploaded_image),
            modifier = Modifier
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                    translationX = -offset.x * zoom
                    translationY = -offset.y * zoom
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .wrapContentSize(Alignment.Center)
                .fillMaxSize()
        )
    }
}


private fun Context.shareImage(uri: Uri) {
    val cacheFile = copyFileToStorage(uri, getOrCreateSharedMediaCache())?.uri?.toFile() ?: return
    cacheFile.deleteOnExit()
    val sharableUri =
        FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", cacheFile)
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, sharableUri)
        type = "image/*"
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    startActivity(Intent.createChooser(intent, null))
}