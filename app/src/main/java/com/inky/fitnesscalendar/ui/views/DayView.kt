package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivityCard
import com.inky.fitnesscalendar.ui.components.ActivityImage
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.components.NewActivityFAB
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.ui.util.sharedElement
import com.inky.fitnesscalendar.util.toDate
import com.inky.fitnesscalendar.view_model.BaseViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayView(
    viewModel: BaseViewModel = hiltViewModel(),
    initialEpochDay: EpochDay,
    onEditActivity: (RichActivity) -> Unit,
    onShareActivity: (RichActivity) -> Unit,
    onTrackDetails: (RichActivity) -> Unit,
    onJumpToActivity: (RichActivity) -> Unit,
    onNewActivity: (EpochDay) -> Unit,
    onEditDay: (EpochDay) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val animationScope = rememberCoroutineScope()
    val pagerState =
        rememberPagerState(initialPage = initialEpochDay.day.toInt()) { EpochDay.today().day.toInt() + 1 }
    val epochDay by remember { derivedStateOf { EpochDay(pagerState.currentPage.toLong()) } }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    if (showDatePicker) {
        DayPickerDialog(
            onDismiss = { showDatePicker = false },
            onScrollTo = { day ->
                animationScope.launch {
                    // TODO: use `animateScrollToPage`
                    // At the time of me writing this, the function is bugged and does not complete the scroll
                    pagerState.scrollToPage(day.day.toInt())
                }
            },
            epochDay = epochDay
        )
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
                colors = defaultTopAppBarColors(),
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
        bottomBar = {
            BottomAppBar(
                actions = {
                    PrevAndNextDaySelector(
                        day = epochDay,
                        onNext = { animationScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        onPrev = { animationScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    )
                }
            )
        },
        floatingActionButton = { NewActivityFAB(onClick = { onNewActivity(epochDay) }) },
        snackbarHost = { SnackbarHost(viewModel.snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val actualEpochDay = EpochDay(page.toLong())
                val day by remember(actualEpochDay) { viewModel.repository.getDay(actualEpochDay) }
                    .collectAsState(initial = null)
                val activities by remember(actualEpochDay) {
                    viewModel.repository.getDayActivities(actualEpochDay)
                }.collectAsState(initial = null)

                if (day != null && activities != null) {
                    DayViewInner(
                        day = day!!,
                        activities = activities!!,
                        localizationRepository = viewModel.repository.localizationRepository,
                        onDeleteActivity = { viewModel.deleteActivity(it) },
                        onShare = onShareActivity,
                        onEditActivity = onEditActivity,
                        onTrackDetails = onTrackDetails,
                        onJumpToActivity = onJumpToActivity,
                        onEditDay = { onEditDay(actualEpochDay) },
                        // Kind of hacky. The problem is that Horizontal Pager instantiates
                        // multiple [DayViewInner]s, but only one of them should have the shared
                        // content keys for the animation from [HomeView] to [DayView].
                        // Otherwise artifacts occur.
                        sharedElement = if (page == pagerState.currentPage) {
                            { this.sharedElement(it) }
                        } else {
                            { this }
                        }
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
    localizationRepository: LocalizationRepository,
    onDeleteActivity: (RichActivity) -> Unit,
    onEditActivity: (RichActivity) -> Unit,
    onTrackDetails: (RichActivity) -> Unit,
    onJumpToActivity: (RichActivity) -> Unit,
    onShare: (RichActivity) -> Unit,
    onEditDay: () -> Unit,
    sharedElement: @Composable Modifier.(SharedContentKey) -> Modifier,
) {
    var showImageViewer by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(bottom = 128.dp)
    ) {
        AnimatedContent(
            targetState = day.imageName?.getImageUri(),
            label = stringResource(R.string.image)
        ) { imageUri ->
            if (imageUri != null) {
                ActivityImage(
                    uri = imageUri,
                    onClick = { showImageViewer = true },
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .sharedElement(SharedContentKey.DayImage)
                )
            }
        }

        Card(
            onClick = onEditDay,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = day.feel,
                label = stringResource(R.string.feel)
            ) { feel ->
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        feel.emoji, modifier = Modifier
                            .padding(end = 8.dp, top = 8.dp)
                            .sharedElement(SharedContentKey.DayFeel)
                    )
                }
            }
            AnimatedContent(
                targetState = day.description,
                label = stringResource(R.string.description)
            ) { description ->
                if (description.isNotBlank()) {
                    Text(
                        day.description,
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .sharedElement(SharedContentKey.DayDescription)
                    )
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
                        onDetails = { onTrackDetails(richActivity) },
                        onJumpTo = { onJumpToActivity(richActivity) },
                        onShare = { onShare(richActivity) },
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

    val imageUri = day.imageName?.getImageUri()
    if (showImageViewer && imageUri != null) {
        ImageViewer(imageUri = imageUri, onDismiss = { showImageViewer = false })
    }
}

@Composable
private fun RowScope.PrevAndNextDaySelector(
    day: EpochDay,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val today = remember { EpochDay.today() }
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DayPickerDialog(
    onDismiss: () -> Unit,
    onScrollTo: (day: EpochDay) -> Unit,
    epochDay: EpochDay
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = epochDay.toLocalDate().atStartOfDay()
            .toDate(ZoneId.of("UTC")).time,
        selectableDates = PastDates
    )

    DatePickerDialog(
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val dateMillis = datePickerState.selectedDateMillis
                    if (dateMillis != null) {
                        val date = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(dateMillis),
                            ZoneId.systemDefault()
                        ).toLocalDate()
                        onScrollTo(EpochDay(date.toEpochDay()))
                    }

                    onDismiss()
                }
            ) { Text(stringResource(R.string.confirm)) }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
object PastDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) =
        Date.from(Instant.now()).time >= utcTimeMillis
}