package com.inky.fitnesscalendar.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.TypeActivity
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.util.skipToLookaheadSize
import com.inky.fitnesscalendar.util.Duration.Companion.until

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityCard(
    typeActivity: TypeActivity,
    onDelete: () -> Unit,
    onEdit: (Activity) -> Unit,
    localizationRepository: LocalizationRepository,
    modifier: Modifier = Modifier,
    onJumpTo: (() -> Unit)? = null,
    onFilter: ((ActivityFilter) -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    val (activity, activityType) = typeActivity

    var showContextMenu by rememberSaveable { mutableStateOf(false) }
    var showImageView by rememberSaveable { mutableStateOf(false) }

    val title = remember(activityType) { "${activityType.emoji} ${activityType.name}" }
    val time = remember(activity) {
        localizationRepository.formatRelativeDate(activity.startTime)
    }
    val description = remember(activity) { activity.description }
    val timeElapsed = remember(activity) { activity.startTime until activity.endTime }

    val haptics = LocalHapticFeedback.current

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 8.dp)
            .combinedClickable(
                onClick = { onEdit(activity) },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    showContextMenu = true
                },
            )
            .skipToLookaheadSize(),
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
            style = MaterialTheme.typography.displayMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        if (activity.vehicle != null || timeElapsed.elapsedMs > 0) {
            HorizontalDivider()
        }

        Row(
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
        }

        if (description.isNotEmpty()) {
            HorizontalDivider()
            Text(
                description,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(all = 8.dp)
            )
        }

        if (activity.imageUri != null) {
            HorizontalDivider()
            AsyncImage(
                model = activity.imageUri,
                contentDescription = stringResource(id = R.string.user_uploaded_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth()
                    .heightIn(max = 256.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable {
                        showImageView = true
                    }
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
            onFilterByType = onFilter?.let {
                {
                    showContextMenu = false
                    it(ActivityFilter(types = listOf(activityType)))
                }
            }
        )
    }

    if (showImageView && activity.imageUri != null) {
        ImageViewer(imageUri = activity.imageUri, onDismiss = { showImageView = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCardContextMenu(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onJumpTo: (() -> Unit)?,
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