package com.inky.fitnesscalendar.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import coil.compose.AsyncImage
import com.inky.fitnesscalendar.BuildConfig
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.util.copyFileToStorage
import com.inky.fitnesscalendar.util.getOrCreateSharedMediaCache
import kotlin.math.max
import kotlin.math.min


@Composable
fun ImageViewer(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        zoom *= zoomChange
        zoom = min(max(zoom, 0.5f), 3f)
        offset += panChange
    }

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
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, stringResource(R.string.delete))
                    }
                }

                IconButton(onClick = { context.shareImage(imageUri) }) {
                    Icon(Icons.Outlined.Share, stringResource(R.string.share))
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.close))
                }
            }

            Box(
                modifier = Modifier
                    .transformable(transformState)
                    .clip(RectangleShape)
                    .weight(1f)
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(R.string.user_uploaded_image),
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = zoom
                            scaleY = zoom
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .fillMaxSize()
                )
            }
        }
    }
}

private fun Context.shareImage(uri: Uri) {
    val cacheFile = copyFileToStorage(uri, getOrCreateSharedMediaCache())?.toFile() ?: return
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