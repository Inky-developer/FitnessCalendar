package com.inky.fitnesscalendar.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.gpx.Coordinate
import com.inky.fitnesscalendar.data.gpx.TrackSvg
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.util.ContextFormat
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedElement
import com.inky.fitnesscalendar.ui.util.skipToLookaheadSize
import com.inky.fitnesscalendar.util.asNonEmptyOrNull
import com.inky.fitnesscalendar.util.gpx.simplify

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityCard(
    richActivity: RichActivity,
    callbacks: ActivityCardCallbacks,
    localizationRepository: LocalizationRepository,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = contentColorFor(containerColor)
) {
    var showContextMenu by rememberSaveable { mutableStateOf(false) }
    var imageViewerUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val title = remember(richActivity) { "${richActivity.type.emoji} ${richActivity.type.name}" }
    val time = remember(richActivity) {
        localizationRepository.timeFormatter.format(richActivity.activity.startTime)
    }
    val images = richActivity.images

    val trackPreview = remember(richActivity) { richActivity.activity.trackPreview?.toTrackSvg() }

    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(CardDefaults.shape)
            .combinedClickable(
                onClick = {
                    if (trackPreview != null) {
                        callbacks.onDetails(richActivity)
                    } else {
                        callbacks.onEdit(richActivity)
                    }
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    showContextMenu = true
                },
            )
            .skipToLookaheadSize()
            .testTag("ActivityCard"),
    ) {
        Box {
            if (trackPreview != null) {
                TrackView(
                    track = trackPreview,
                    color = contentColor,
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
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                ActivityCardContent(richActivity.activity, richActivity.place)
            }
        }

        images.asNonEmptyOrNull()?.let { images ->
            HorizontalDivider()
            ActivityImages(
                images = images,
                onClick = { imageViewerUri = it.getImageUri(context) },
                modifier = Modifier
                    .padding(all = 8.dp)
                    .sharedElement(SharedContentKey.ActivityImage)
            )
        }
    }

    if (showContextMenu) {
        ActivityCardContextMenu(
            richActivity = richActivity,
            onDismiss = { showContextMenu = false },
            callbacks = callbacks,
        )
    }

    imageViewerUri?.let {
        ImageViewer(
            imageUri = it,
            onDismiss = { imageViewerUri = null })
    }
}

@Composable
fun CompactActivityCard(
    richActivity: RichActivity,
    localizationRepository: LocalizationRepository,
    modifier: Modifier = Modifier,
    expand: Boolean = false,
) {
    val (activity, activityType) = richActivity

    val title = remember(activityType) { "${activityType.emoji} ${activityType.name}" }
    val time = remember(activity) {
        localizationRepository.formatRelativeDate(activity.startTime)
    }

    val backgroundColor = MaterialTheme.colorScheme.surfaceContainer

    val trackColor = contentColorFor(backgroundColor)
    val track = remember(richActivity) { richActivity.activity.trackPreview?.toTrackSvg() }

    CardWithBackground(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        backgroundContent = {
            if (track != null) {
                TrackView(
                    track = track,
                    color = trackColor,
                    modifier = Modifier
                        .matchParentSize()
                        .padding(all = 8.dp)
                )
            }
        }
    ) {
        Text(
            time,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 4.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.weight(1f))
            if (activity.favorite) {
                FavoriteIcon(true)
            }
        }

        if (expand) {
            ActivityCardContent(activity = activity, place = richActivity.place)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityCardContent(activity: Activity, place: Place?, showDescription: Boolean = true) {
    val timeElapsed = remember(activity) { activity.startTime until activity.endTime }

    if (activity.vehicle != null || timeElapsed.elapsedMs > 0) {
        HorizontalDivider()
    }

    FlowRow(
        maxItemsInEachRow = 3,
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp)
    ) {
        if (activity.favorite) {
            FavoriteIcon(
                true,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 4.dp)
            )
        }

        if (activity.vehicle != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(activity.vehicle.emoji, style = MaterialTheme.typography.bodyLarge)
                Text(
                    activity.vehicle.text(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (activity.feel != Feel.Ok) {
            Text(
                activity.feel.emoji,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 4.dp)
            )
        }

        IconStatistic(timeElapsed.takeIf { it.elapsedMs > 0 }) {
            Icon(
                painterResource(R.drawable.outline_timer_24),
                stringResource(R.string.time)
            )
        }
        IconStatistic(activity.distance) {
            Icons.ArrowForward(stringResource(R.string.distance))
        }
        IconStatistic(activity.averageMovingSpeed) {
            Icon(
                painterResource(R.drawable.outline_speed_24),
                stringResource(R.string.speed)
            )
        }
        IconStatistic(activity.averageHeartRate) {
            Icon(
                painterResource(R.drawable.outline_heart_rate_24),
                stringResource(R.string.heart_rate)
            )
        }
        IconStatistic(activity.totalAscent) {
            Icon(
                painterResource(R.drawable.outline_ascent_24),
                stringResource(R.string.total_ascent)
            )
        }
        IconStatistic(activity.temperature) {
            Icon(
                painterResource(R.drawable.outline_temperature_24),
                stringResource(R.string.temperature)
            )
        }
        IconStatistic(activity.intensity) {
            Icon(
                painterResource(R.drawable.twotone_lightbulb_24),
                stringResource(R.string.intensity),
                tint = lerp(
                    colorResource(R.color.intensity_low),
                    colorResource(R.color.intensity_high),
                    (activity.intensity?.value?.toFloat() ?: 0f) / 10f
                )
            )
        }
    }

    if (place != null) {
        HorizontalDivider()
        PlaceInfo(
            place,
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth()
        )
    }

    AnimatedVisibility(activity.description.isNotEmpty() && showDescription) {
        HorizontalDivider()
        Text(
            activity.description,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(all = 8.dp)
        )
    }
}

data class ActivityCardCallbacks(
    val onDetails: (RichActivity) -> Unit,
    val onEdit: (RichActivity) -> Unit,
    val onDelete: (RichActivity) -> Unit,
    val onShare: (RichActivity) -> Unit,
    val onJumpTo: ((RichActivity) -> Unit)?,
    val onShowDay: ((RichActivity) -> Unit)?,
    val onFilterByType: ((RichActivity) -> Unit)?,
)

@Composable
private fun IconStatistic(stat: ContextFormat?, icon: @Composable () -> Unit) {
    if (stat != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            icon()
            Text(stat.text())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityCardContextMenu(
    richActivity: RichActivity,
    onDismiss: () -> Unit,
    callbacks: ActivityCardCallbacks,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            stringResource(R.string.options),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(all = 8.dp))

        BottomSheetButton(
            onClick = {
                onDismiss()
                callbacks.onShare(richActivity)
            },
            leadingIcon = { Icons.Share(stringResource(R.string.share)) }
        ) {
            Text(stringResource(R.string.share))
        }
        AnimatedVisibility(visible = callbacks.onJumpTo != null) {
            BottomSheetButton(
                onClick = {
                    callbacks.onJumpTo?.let { it(richActivity) }
                    onDismiss()
                },
                leadingIcon = {
                    Icons.PlayArrow(stringResource(R.string.jump_to))
                },
            ) {
                Text(stringResource(R.string.jump_to))
            }
        }
        AnimatedVisibility(visible = callbacks.onShowDay != null) {
            BottomSheetButton(
                onClick = {
                    onDismiss()
                    callbacks.onShowDay?.let { it(richActivity) }
                },
                leadingIcon = {
                    Icons.DateRange(stringResource(R.string.show_day))
                },
            ) {
                Text(stringResource(R.string.show_day))
            }
        }
        AnimatedVisibility(visible = callbacks.onFilterByType != null) {
            BottomSheetButton(
                onClick = {
                    onDismiss()
                    callbacks.onFilterByType?.let { it(richActivity) }
                },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.outline_filter_24),
                        stringResource(R.string.filter)
                    )
                },
            ) {
                Text(stringResource(R.string.only_show_this_activity_type))
            }
        }

        BottomSheetButton(
            onClick = { showDialog = true },
            leadingIcon = { Icons.Delete(stringResource(R.string.delete)) },
        ) {
            Text(stringResource(R.string.delete_activity))
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showDialog) {
        AlertDialog(
            icon = {
                Icons.Delete(
                    stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.ask_delete_activity)) },
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { callbacks.onDelete(richActivity) }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun CardWithBackground(
    modifier: Modifier = Modifier,
    containerColor: Color,
    backgroundContent: @Composable BoxScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(CardDefaults.shape)
            .background(containerColor)
    ) {
        backgroundContent()
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Preview
@Composable
private fun CardWithBackgroundPreview() {
    val trackPoints = remember {
        listOf(
            Coordinate(0.0, 0.0),
            Coordinate(1.0, 1.0),
            Coordinate(-0.5, 2.0),
            Coordinate(-1.0, 3.0),
            Coordinate(-1.0, 4.0),
            Coordinate(0.0, 5.0),
            Coordinate(2.0, 4.5),
            Coordinate(3.0, 2.0),
            Coordinate(0.0, 0.0),
        )
    }
    val simplifiedTrack = remember(trackPoints) { simplify(trackPoints, maxNumPoints = 7) }
    val path = remember(simplifiedTrack) { TrackSvg.fromPoints(simplifiedTrack) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        CardWithBackground(
            containerColor = Color.White,
            backgroundContent = {
                if (path != null) {
                    TrackView(
                        track = path,
                        color = Color.Red,
                        modifier = Modifier.matchParentSize()
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Hello", modifier = Modifier.padding(all = 8.dp))
                Text("New Line", modifier = Modifier.padding(all = 8.dp))
            }
        }
    }
}