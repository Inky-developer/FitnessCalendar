package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivityCard
import com.inky.fitnesscalendar.ui.components.ActivityImage
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.getAppBarContainerColor
import com.inky.fitnesscalendar.ui.util.horizontalOrderedTransitionSpec
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.util.toDate
import com.inky.fitnesscalendar.view_model.BaseViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayView(
    viewModel: BaseViewModel = hiltViewModel(),
    initialEpochDay: EpochDay,
    onEditActivity: (RichActivity) -> Unit,
    onJumpToActivity: (RichActivity) -> Unit,
    onEditDay: (EpochDay) -> Unit,
    onOpenDrawer: () -> Unit
) {
    var epochDay by rememberSaveable(initialEpochDay) { mutableStateOf(initialEpochDay) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = epochDay.toLocalDate().atStartOfDay()
            .toDate(ZoneId.of("UTC")).time
    )
    val scrollState = rememberScrollState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val topAppBarColors = TopAppBarDefaults.topAppBarColors(
        scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    val appBarContainerColor =
        getAppBarContainerColor(scrollBehavior = scrollBehavior, topAppBarColors = topAppBarColors)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val dateMillis = datePickerState.selectedDateMillis
                        if (dateMillis != null) {
                            val date = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(dateMillis),
                                ZoneId.systemDefault()
                            ).toLocalDate()
                            epochDay = EpochDay(day = date.toEpochDay())
                            showDatePicker = false
                        }
                    }
                ) { Text(stringResource(R.string.confirm)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val todayString = remember(epochDay) {
                        viewModel.repository.localizationRepository.formatRelativeLocalDate(epochDay.toLocalDate())
                    }
                    Text(todayString)
                },
                colors = topAppBarColors,
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(R.string.Menu),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditDay(epochDay) }) {
                        Icon(Icons.Outlined.Edit, stringResource(R.string.edit_day))
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Outlined.DateRange, stringResource(R.string.select_date))
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        snackbarHost = { SnackbarHost(viewModel.snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            PrevAndNextDaySelector(
                day = epochDay,
                onNext = { epochDay = EpochDay(epochDay.day + 1) },
                onPrev = { epochDay = EpochDay(epochDay.day - 1) },
                modifier = Modifier.background(appBarContainerColor)
            )
            AnimatedContent(
                targetState = epochDay,
                transitionSpec = horizontalOrderedTransitionSpec(),
                label = stringResource(R.string.day),
            ) { actualEpochDay ->
                val day by remember(actualEpochDay) { viewModel.repository.getDay(actualEpochDay!!) }
                    .collectAsState(initial = null)
                val activities by remember(actualEpochDay) {
                    viewModel.repository.getDayActivities(actualEpochDay!!)
                }.collectAsState(initial = null)

                if (day != null && activities != null) {
                    DayViewInner(
                        day = day!!,
                        activities = activities!!,
                        scrollState = scrollState,
                        localizationRepository = viewModel.repository.localizationRepository,
                        onDeleteActivity = { viewModel.deleteActivity(it) },
                        onEditActivity = onEditActivity,
                        onJumpToActivity = onJumpToActivity,
                        onEditDay = { onEditDay(actualEpochDay!!) }
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayViewInner(
    day: Day,
    activities: List<RichActivity>,
    scrollState: ScrollState,
    localizationRepository: LocalizationRepository,
    onDeleteActivity: (RichActivity) -> Unit,
    onEditActivity: (RichActivity) -> Unit,
    onJumpToActivity: (RichActivity) -> Unit,
    onEditDay: () -> Unit,
) {
    var showImageViewer by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.verticalScroll(scrollState)) {
        AnimatedContent(
            targetState = day.imageUri,
            label = stringResource(R.string.image)
        ) { imageUri ->
            if (imageUri != null) {
                ActivityImage(uri = imageUri, onClick = { showImageViewer = true })
            } else {
                Spacer(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                )
            }
        }

        Card(
            onClick = onEditDay,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = day.feel,
                label = stringResource(R.string.feel)
            ) { feel ->
                if (feel != null) {
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(feel.emoji, modifier = Modifier.padding(end = 8.dp, top = 8.dp))
                    }
                }
            }
            AnimatedContent(
                targetState = day.description,
                label = stringResource(R.string.description)
            ) { description ->
                if (description.isNotBlank()) {
                    Text(day.description, modifier = Modifier.padding(all = 8.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(all = 8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            stringResource(R.string.info),
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            stringResource(R.string.no_notes_for_day),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = activities,
            label = "ActivitiesToday"
        ) { activities ->
            Column {
                for (richActivity in activities) {
                    ActivityCard(
                        richActivity = richActivity,
                        onDelete = { onDeleteActivity(richActivity) },
                        onEdit = { onEditActivity(richActivity) },
                        onJumpTo = { onJumpToActivity(richActivity) },
                        localizationRepository = localizationRepository,
                        modifier = Modifier.sharedBounds(
                            SharedContentKey.ActivityCard(
                                richActivity.activity.uid
                            )
                        )
                    )
                }
            }
        }
    }

    if (showImageViewer && day.imageUri != null) {
        ImageViewer(imageUri = day.imageUri, onDismiss = { showImageViewer = false })
    }
}

@Composable
fun PrevAndNextDaySelector(
    day: EpochDay,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { EpochDay.today() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
    ) {
        Button(
            onClick = onPrev, enabled = day.day > 0, modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        ) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
        }
        Button(
            onClick = onNext, enabled = day < today, modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        ) {
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, stringResource(R.string.forward))
        }
    }
}
