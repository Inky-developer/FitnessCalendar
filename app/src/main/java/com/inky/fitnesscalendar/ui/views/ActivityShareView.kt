package com.inky.fitnesscalendar.ui.views

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.BuildConfig
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ContentColor
import com.inky.fitnesscalendar.data.measure.Temperature
import com.inky.fitnesscalendar.data.measure.VerticalDistance
import com.inky.fitnesscalendar.data.measure.bpm
import com.inky.fitnesscalendar.data.measure.kilometers
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.Track
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivityCardContent
import com.inky.fitnesscalendar.ui.components.ActivityImage
import com.inky.fitnesscalendar.ui.components.TrackView
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.theme.FitnessCalendarTheme
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.util.getOrCreateSharedMediaCache
import com.inky.fitnesscalendar.util.getOrCreateSharedTracksCache
import com.inky.fitnesscalendar.util.gpx.GpxWriter
import com.inky.fitnesscalendar.util.toLocalDate
import com.inky.fitnesscalendar.view_model.BaseViewModel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.FileWriter
import java.time.Instant
import java.util.Date

@Composable
fun ActivityShareView(
    viewModel: BaseViewModel = hiltViewModel(),
    activityId: Int,
    onBack: () -> Unit
) {
    val richActivity by remember {
        viewModel.repository.getActivity(activityId)
    }.collectAsState(initial = null)
    val track by remember {
        viewModel.repository.getTrackByActivity(activityId)
    }.collectAsState(initial = null)

    when (val activity = richActivity) {
        null -> CircularProgressIndicator()
        else -> ActivityShareView(
            richActivity = activity,
            track = track,
            localizationRepository = viewModel.repository.localizationRepository,
            onBack = onBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityShareView(
    richActivity: RichActivity,
    track: Track?,
    localizationRepository: LocalizationRepository,
    onBack: () -> Unit
) {
    var shareCardConfig by rememberSaveable { mutableStateOf(ShareCardConfig()) }

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
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        val bitmap = shareGraphicsLayer.toImageBitmap()
                        context.shareImageBitmap(richActivity, bitmap)
                    }
                },
                text = { Text(stringResource(R.string.share_activity_as_image)) },
                icon = {
                    Icon(Icons.Outlined.Share, stringResource(R.string.share))
                }
            )
        },
        floatingActionButtonPosition = FabPosition.EndOverlay,
        bottomBar = {
            BottomAppBar {
                if (track != null) {
                    TextButton(
                        onClick = { context.shareGpxTrack(richActivity, track) }
                    ) {
                        Text(stringResource(R.string.share_gpx))
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ShareSettings(
                activity = richActivity,
                config = shareCardConfig,
                setConfig = { shareCardConfig = it }
            )

            FitnessCalendarTheme(runSideEffects = false, darkTheme = false, dynamicColor = false) {
                ScreenShotBox(shareGraphicsLayer) {
                    ActivityShareCard(
                        richActivity = richActivity,
                        config = shareCardConfig,
                        localizationRepository = localizationRepository
                    )
                }
            }
        }
    }
}

@Parcelize
private data class ShareCardConfig(
    val showImage: Boolean = true,
    val showDescription: Boolean = true,
    val showTrack: Boolean = true,
) : Parcelable

@Composable
private fun ShareSettings(
    activity: RichActivity,
    config: ShareCardConfig,
    setConfig: (ShareCardConfig) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(all = 8.dp)
    ) {
        if (activity.activity.imageName != null) {
            SettingsChip(
                selected = config.showImage,
                onToggle = { setConfig(config.copy(showImage = !config.showImage)) },
                label = stringResource(R.string.show_image)
            )
        }
        if (activity.activity.description.isNotBlank()) {
            SettingsChip(
                selected = config.showDescription,
                onToggle = { setConfig(config.copy(showDescription = !config.showDescription)) },
                label = stringResource(R.string.show_description)
            )
        }
        if (activity.activity.trackPreview != null) {
            SettingsChip(
                selected = config.showTrack,
                onToggle = { setConfig(config.copy(showTrack = !config.showTrack)) },
                label = stringResource(R.string.show_track)
            )
        }
    }
}

@Composable
private fun SettingsChip(selected: Boolean, onToggle: () -> Unit, label: String) {
    InputChip(
        selected = selected,
        onClick = onToggle,
        label = { Text(label) },
        leadingIcon = {
            AnimatedVisibility(selected) {
                Icon(Icons.Outlined.Check, label)
            }
        },
        modifier = Modifier.padding(all = 4.dp)
    )
}

@Composable
private fun ActivityShareCard(
    richActivity: RichActivity,
    config: ShareCardConfig,
    localizationRepository: LocalizationRepository
) {
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    val title = remember(richActivity) { "${richActivity.type.emoji} ${richActivity.type.name}" }
    val time = remember(richActivity) {
        localizationRepository.dateFormatter.format(richActivity.activity.startTime) +
                " " +
                localizationRepository.timeFormatter.format(richActivity.activity.startTime)
    }
    val imageUri = richActivity.activity.imageName?.getImageUri()

    val trackColor = contentColorFor(containerColor)
    val trackPreview = remember(richActivity) { richActivity.activity.trackPreview?.toTrackSvg() }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box {
            AnimatedContent(config.showTrack, modifier = Modifier.matchParentSize()) { showTrack ->
                if (trackPreview != null && showTrack) {
                    TrackView(
                        track = trackPreview,
                        color = trackColor,
                        modifier = Modifier
                            .matchParentSize()
                            .padding(all = 8.dp)
                    )
                }
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
                ActivityCardContent(
                    activity = richActivity.activity,
                    place = richActivity.place,
                    showDescription = config.showDescription
                )
            }
        }

        AnimatedVisibility(config.showImage) {
            if (imageUri != null) {
                HorizontalDivider()
                ActivityImage(
                    uri = imageUri,
                    modifier = Modifier.padding(all = 8.dp)
                )
            }
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

private fun Context.shareImageBitmap(richActivity: RichActivity, image: ImageBitmap) {
    val cache = getOrCreateSharedMediaCache()
    val file = File(cache, getSharedActivityTitle(richActivity, "png"))
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

private fun Context.shareGpxTrack(richActivity: RichActivity, track: Track) {
    val cache = getOrCreateSharedTracksCache()
    val file = File(cache, getSharedActivityTitle(richActivity, "gpx"))
    file.delete()
    file.deleteOnExit()
    FileWriter(file).use { writer ->
        GpxWriter.write(richActivity, track, this, writer)
    }

    val shareableUri =
        FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file)
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "application/gpx+xml"
        putExtra(Intent.EXTRA_STREAM, shareableUri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    startActivity(Intent.createChooser(intent, getString(R.string.share_gpx)))
}

private fun getSharedActivityTitle(richActivity: RichActivity, extension: String): String {
    val time =
        LocalizationRepository.localDateFormatter.format(richActivity.activity.startTime.toLocalDate())
    val name = richActivity.type.name
    return "$name $time.$extension"
}

@Preview
@Composable
fun ActivityShareCardPreview() {
    val activity = Activity(
        typeId = 0,
        startTime = Date.from(Instant.now()),
        endTime = Date.from(Instant.now().plusSeconds(4000)),
        distance = 25.0.kilometers(),
        averageHeartRate = 125.0.bpm(),
        temperature = Temperature(celsius = 20.0),
        totalAscent = VerticalDistance(meters = 1250.0)
    )
    val richActivity = RichActivity(
        activity = activity,
        type = ActivityType(
            uid = 0,
            activityCategory = ActivityCategory.Sports,
            name = "Biking",
            emoji = "🚴‍♂️",
            color = ContentColor.Color1,
        ),
        place = null
    )
    ActivityShareCard(richActivity, ShareCardConfig(), LocalizationRepository(LocalContext.current))
}