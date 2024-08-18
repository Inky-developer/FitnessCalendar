package com.inky.fitnesscalendar.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.gpx.Coordinate
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.CoordinateRect
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.Track
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.util.skipToLookaheadSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityCard(
    richActivity: RichActivity,
    onDelete: () -> Unit,
    onEdit: (Activity) -> Unit,
    localizationRepository: LocalizationRepository,
    modifier: Modifier = Modifier,
    onJumpTo: (() -> Unit)? = null,
    onShowDay: (() -> Unit)? = null,
    onFilter: ((ActivityFilter) -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    var showContextMenu by rememberSaveable { mutableStateOf(false) }
    var showImageViewer by rememberSaveable { mutableStateOf(false) }

    val title = remember(richActivity) { "${richActivity.type.emoji} ${richActivity.type.name}" }
    val time = remember(richActivity) {
        localizationRepository.timeFormatter.format(richActivity.activity.startTime)
    }

    val haptics = LocalHapticFeedback.current

    val trackColor = contentColorFor(backgroundColor = containerColor)
    var canvasSize by remember { mutableStateOf<Size?>(null) }
    val trackPath = rememberTrackPath(richActivity.track, canvasSize)

    CardWithCanvas(
        containerColor = containerColor,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { onEdit(richActivity.activity) },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    showContextMenu = true
                },
            )
            .skipToLookaheadSize(),
        onDraw = {
            canvasSize = size
            if (trackPath != null) {
                drawTrackPath(trackPath, trackColor)
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
        Text(
            title,
            style = MaterialTheme.typography.displaySmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        ActivityCardContent(richActivity.activity, richActivity.place)

        if (richActivity.activity.imageUri != null) {
            HorizontalDivider()
            ActivityImage(
                uri = richActivity.activity.imageUri,
                onClick = { showImageViewer = true },
                modifier = Modifier.padding(all = 8.dp)
            )
        }
    }

    if (showContextMenu) {
        ActivityCardContextMenu(
            onDismiss = { showContextMenu = false },
            onDelete = {
                showContextMenu = false
                onDelete()
            },
            onJumpTo = onJumpTo?.let {
                {
                    showContextMenu = false
                    it()
                }
            },
            onShowDay = onShowDay?.let {
                {
                    showContextMenu = false
                    it()
                }
            },
            onFilterByType = onFilter?.let {
                {
                    showContextMenu = false
                    it(ActivityFilter(types = listOf(richActivity.type)))
                }
            }
        )
    }

    if (showImageViewer && richActivity.activity.imageUri != null) {
        ImageViewer(
            imageUri = richActivity.activity.imageUri,
            onDismiss = { showImageViewer = false })
    }
}

@Composable
fun CompactActivityCard(
    richActivity: RichActivity,
    localizationRepository: LocalizationRepository,
    modifier: Modifier = Modifier,
    track: Track? = richActivity.track,
    expand: Boolean = false,
) {
    val (activity, activityType) = richActivity

    val title = remember(activityType) { "${activityType.emoji} ${activityType.name}" }
    val time = remember(activity) {
        localizationRepository.formatRelativeDate(activity.startTime)
    }

    val backgroundColor = MaterialTheme.colorScheme.surfaceContainer
    val trackColor = contentColorFor(backgroundColor)
    var canvasSize by remember { mutableStateOf<Size?>(null) }
    val trackPath = rememberTrackPath(track, canvasSize)

    CardWithCanvas(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        onDraw = {
            canvasSize = size
            if (trackPath != null && expand) {
                drawTrackPath(trackPath, trackColor)
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
        Text(
            title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        if (expand) {
            ActivityCardContent(activity = activity, place = richActivity.place)
        }
    }
}

@Composable
private fun ActivityCardContent(activity: Activity, place: Place?) {
    val timeElapsed = remember(activity) { activity.startTime until activity.endTime }

    if (activity.vehicle != null || timeElapsed.elapsedMs > 0) {
        HorizontalDivider()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp), horizontalArrangement = Arrangement.SpaceAround
    ) {
        if (activity.vehicle != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(activity.vehicle.emoji, style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(activity.vehicle.nameId),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (activity.feel != null) {
            Text(activity.feel.emoji, style = MaterialTheme.typography.bodyLarge)
        }

        if (timeElapsed.elapsedMs > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.outline_timer_24),
                    stringResource(R.string.time)
                )
                Text(timeElapsed.format(), style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (activity.distance != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    stringResource(R.string.distance)
                )
                Text(
                    stringResource(
                        R.string.n_kilometers,
                        "%.1f".format(activity.distance.kilometers)
                    )
                )
            }
        }

        if (activity.velocity != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.outline_speed_24),
                    stringResource(R.string.speed)
                )
                Text(
                    stringResource(R.string.x_kmh, "%.1f".format(activity.velocity!!.kmh))
                )
            }
        }

        if (activity.intensity != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.twotone_lightbulb_24),
                    stringResource(R.string.intensity),
                    tint = lerp(
                        colorResource(R.color.intensity_low),
                        colorResource(R.color.intensity_high),
                        activity.intensity.value.toFloat() / 10f
                    )
                )
                Text(activity.intensity.value.toString())
            }
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

    if (activity.description.isNotEmpty()) {
        HorizontalDivider()
        Text(
            activity.description,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(all = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityCardContextMenu(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onJumpTo: (() -> Unit)?,
    onShowDay: (() -> Unit)?,
    onFilterByType: (() -> Unit)?,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            stringResource(R.string.options),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(all = 8.dp))

        AnimatedVisibility(visible = onJumpTo != null) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.jump_to)) },
                leadingContent = {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        stringResource(R.string.jump_to)
                    )
                },
                modifier = Modifier.clickable { onJumpTo?.let { it() } }
            )
        }
        AnimatedVisibility(visible = onShowDay != null) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.show_day)) },
                leadingContent = {
                    Icon(
                        Icons.Outlined.DateRange,
                        stringResource(R.string.show_day)
                    )
                },
                modifier = Modifier.clickable { onShowDay?.let { it() } }
            )
        }
        AnimatedVisibility(visible = onFilterByType != null) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.only_show_this_activity_type)) },
                leadingContent = {
                    Icon(
                        painterResource(R.drawable.outline_filter_24),
                        stringResource(R.string.filter)
                    )
                },
                modifier = Modifier.clickable { onFilterByType?.let { it() } }
            )
        }
        ListItem(
            headlineContent = { Text(stringResource(R.string.delete_activity)) },
            leadingContent = { Icon(Icons.Outlined.Delete, stringResource(R.string.delete)) },
            modifier = Modifier.clickable { showDialog = true }
        )

        Spacer(Modifier.height(32.dp))
    }

    if (showDialog) {
        AlertDialog(
            icon = {
                Icon(
                    Icons.Outlined.Delete,
                    stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.ask_delete_activity)) },
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = onDelete) {
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
private fun CardWithCanvas(
    modifier: Modifier = Modifier,
    containerColor: Color,
    onDraw: DrawScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.clip(CardDefaults.shape)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(containerColor, size = size)
            onDraw()
        }
        Column {
            content()
        }
    }
}


@Composable
private fun rememberTrackPath(track: Track?, canvasSize: Size?): Path? {
    if (track == null || canvasSize == null) {
        return null
    }

    var path by remember { mutableStateOf<Path?>(null) }

    LaunchedEffect(track, canvasSize) {
        launch(Dispatchers.Default) {
            path = generateTrackPath(track, canvasSize)
        }
    }

    return path
}

private fun DrawScope.drawTrackPath(trackPath: Path, color: Color) {
    drawPath(trackPath, color.copy(alpha = 0.25f), style = Stroke(PathGeneratorState.STROKE_SIZE))
}

private data class PathGeneratorState(
    val bounds: CoordinateRect,
    val scale: Float,
    val xOff: Float,
    val yOff: Float
) {
    inline fun coordinateToUiPos(coordinate: Coordinate, handler: (Float, Float) -> Unit) {
        // y axis is inverted for canvas (0 at the top, but latitude is 0 at equator and increases north)
        val lat = yOff - (coordinate.latitude - bounds.latitudeMin).toFloat() * scale
        val lon = xOff + (coordinate.longitude - bounds.longitudeMin).toFloat() * scale
        handler(lat, lon)
    }

    companion object {
        const val PADDING = 16f
        const val STROKE_SIZE = 8f

        fun create(bounds: CoordinateRect, size: Size): PathGeneratorState {
            val effectiveHeight = size.height - STROKE_SIZE - PADDING
            val effectiveWidth = size.width - STROKE_SIZE - PADDING

            val scale = minOf(
                effectiveWidth / (bounds.longitudeMax - bounds.longitudeMin),
                effectiveHeight / (bounds.latitudeMax - bounds.latitudeMin)
            ).toFloat()

            val maxX = (bounds.longitudeMax - bounds.longitudeMin) * scale
            val maxY = (bounds.latitudeMax - bounds.latitudeMin) * scale

            // Align right center
            val yOff = size.height - (size.height - maxY).toFloat() / 2f
            val xOff = size.width - (PADDING + STROKE_SIZE) / 2f - maxX.toFloat()

            return PathGeneratorState(bounds = bounds, scale = scale, xOff = xOff, yOff = yOff)
        }
    }
}

private fun generateTrackPath(track: Track, canvasSize: Size): Path? {
    if (track.points.isEmpty()) {
        return null
    }

    val path = Path()
    val bounds = track.calculateBounds() ?: return null
    val state = PathGeneratorState.create(bounds, canvasSize)

    val stepSize = 8
    state.coordinateToUiPos(track.points[0].coordinate) { lat, lon -> path.moveTo(lon, lat) }
    for (index in stepSize..<track.points.size step stepSize) {
        val point = track.points[index]

        state.coordinateToUiPos(point.coordinate) { lat, lon -> path.lineTo(lon, lat) }
    }

    // Make sure that the end point is included
    if (track.points.isNotEmpty() && (track.points.size - 1) % stepSize != 0) {
        val point = track.points.last()
        state.coordinateToUiPos(point.coordinate) { lat, lon -> path.lineTo(lon, lat) }
    }

    return path
}