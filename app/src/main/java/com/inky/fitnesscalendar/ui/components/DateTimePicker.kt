package com.inky.fitnesscalendar.ui.components

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.util.horizontalOrderedTransitionSpec
import com.inky.fitnesscalendar.util.toDate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    initialDateTime: LocalDateTime = rememberSaveable { LocalDateTime.now() },
    onDismiss: () -> Unit,
    onOkay: (LocalDateTime) -> Unit
) {
    var initialTime by remember(initialDateTime) { mutableStateOf(initialDateTime.toLocalTime()) }
    val timePickerState = rememberTimePickerStateKeyed(initialTime = initialTime)

    var date by rememberSaveable(initialDateTime) { mutableStateOf(initialDateTime.toLocalDate()) }
    var time by rememberSaveable(initialDateTime) { mutableStateOf(initialDateTime.toLocalTime()) }

    LaunchedEffect(key1 = timePickerState.hour, key2 = timePickerState.minute) {
        time = LocalTime.of(timePickerState.hour, timePickerState.minute)
    }

    OkayCancelDialog(
        onDismiss = onDismiss,
        onOkay = { onOkay(date.atTime(time)) },
        additionalButtons = {
            TextButton(onClick = {
                date = LocalDate.now()
                initialTime = LocalTime.now()
            }) {
                Text(
                    stringResource(R.string.now),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            TimePicker(state = timePickerState)
            DaySelector(date, onDate = { date = it })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaySelector(date: LocalDate, onDate: (LocalDate) -> Unit) {
    val dateMillis = remember(date) { date.atStartOfDay().toDate(ZoneId.of("UTC")).time }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerStateKeyed(initialSelectedDateMillis = dateMillis)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { onDate(date.minusDays(1)) }) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, stringResource(R.string.back))
        }

        AnimatedContent(
            targetState = date,
            label = stringResource(R.string.animation_displayed_date),
            transitionSpec = horizontalOrderedTransitionSpec(),
            modifier = Modifier.weight(1f)
        ) { displayDate ->
            TextButton(onClick = { showDatePicker = true }) {
                Text(
                    displayDate!!.format(LocalizationRepository.localDateFormatter),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        IconButton(onClick = { onDate(date.plusDays(1)) }) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, stringResource(R.string.forward))
        }
    }

    if (showDatePicker) {
        OkayCancelDialog(
            onDismiss = { showDatePicker = false },
            onOkay = {
                showDatePicker = false
                datePickerState.selectedDateMillis?.let { dateMillis ->
                    onDate(
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(dateMillis),
                            ZoneId.systemDefault()
                        ).toLocalDate()
                    )
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


// TODO: This does not remember with saveable which it should
// However, the current api from compose is incredibly limiting and it would be very annoying to implement it.
// Maybe support for this use case gets added in the future.
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun rememberDatePickerStateKeyed(initialSelectedDateMillis: Long): DatePickerState {
    val locale = LocalConfiguration.current.locales[0]
    return remember(key1 = initialSelectedDateMillis) {
        DatePickerState(
            initialSelectedDateMillis = initialSelectedDateMillis,
            locale = locale
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberTimePickerStateKeyed(initialTime: LocalTime): TimePickerState {
    val context = LocalContext.current
    return rememberSaveable(context, initialTime, saver = TimePickerState.Saver()) {
        val is24Hour = DateFormat.is24HourFormat(context)
        TimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = is24Hour
        )
    }
}

@Preview
@Composable
private fun DateTimePickerPreview() {
    DateTimePicker(onDismiss = {}, onOkay = { println(it) })
}