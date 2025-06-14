package com.inky.fitnesscalendar.ui.views

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.BuildConfig
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivityCardContent
import com.inky.fitnesscalendar.ui.components.ActivityImage
import com.inky.fitnesscalendar.ui.components.TrackView
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.ui.util.skipToLookaheadSize
import com.inky.fitnesscalendar.util.getOrCreateSharedMediaCache
import com.inky.fitnesscalendar.view_model.BaseViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ActivityShareView(
    viewModel: BaseViewModel = hiltViewModel(),
    activityId: Int,
    onBack: () -> Unit
) {
    val richActivity by remember { viewModel.repository.getActivity(activityId) }.collectAsState(
        initial = null
    )

    when (val activity = richActivity) {
        null -> CircularProgressIndicator()
        else -> ActivityShareView(
            richActivity = activity,
            localizationRepository = viewModel.repository.localizationRepository,
            onBack = onBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityShareView(
    richActivity: RichActivity,
    localizationRepository: LocalizationRepository,
    onBack: () -> Unit
) {
    val shareGraphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.share_activity)) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        val bitmap = shareGraphicsLayer.toImageBitmap()
                        context.shareImageBitmap(bitmap)
                    }
                }
            ) {
                Icon(Icons.Outlined.Done, stringResource(R.string.share))
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            ScreenShotBox(shareGraphicsLayer) {
                ActivityShareCard(
                    richActivity = richActivity,
                    localizationRepository = localizationRepository
                )
            }
        }
    }
}

@Composable
private fun ActivityShareCard(
    richActivity: RichActivity,
    localizationRepository: LocalizationRepository
) {
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    val title = remember(richActivity) { "${richActivity.type.emoji} ${richActivity.type.name}" }
    val time = remember(richActivity) {
        localizationRepository.timeFormatter.format(richActivity.activity.startTime)
    }
    val imageUri = richActivity.activity.imageName?.getImageUri()

    val trackColor = contentColorFor(containerColor)
    val trackPreview = remember(richActivity) { richActivity.activity.trackPreview?.toTrackSvg() }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(CardDefaults.shape)
            .skipToLookaheadSize()
            .testTag("ActivityCard"),
    ) {
        Box {
            if (trackPreview != null) {
                TrackView(
                    track = trackPreview,
                    color = trackColor,
                    modifier = Modifier
                        .matchParentSize()
                        .padding(all = 8.dp)
                )
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    time,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 4.dp)
                )
                Text(
                    title,
                    style = MaterialTheme.typography.displaySmall,
                    color = contentColorFor(containerColor),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                ActivityCardContent(richActivity.activity, richActivity.place)
            }
        }

        if (imageUri != null) {
            HorizontalDivider()
            ActivityImage(
                uri = imageUri,
                modifier = Modifier.padding(all = 8.dp)
            )
        }
    }
}

@Composable
private fun ScreenShotBox(graphicsLayer: GraphicsLayer, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.drawWithContent {
            graphicsLayer.record {
                this@drawWithContent.drawContent()
            }
            drawLayer(graphicsLayer)
        },
        content = content
    )
}

private fun Context.shareImageBitmap(image: ImageBitmap) {
    val cache = getOrCreateSharedMediaCache()
    val file = File(cache, getString(R.string.activity_summary) + ".png")
    file.delete()
    file.deleteOnExit()

    file.outputStream().use { out ->
        image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
    }

    val shareableUri =
        FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file)
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, shareableUri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    startActivity(Intent.createChooser(intent, getString(R.string.share_activity)))
}