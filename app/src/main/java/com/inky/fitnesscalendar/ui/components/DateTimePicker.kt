package com.inky.fitnesscalendar.ui.components

import android.icu.util.Calendar
import android.text.format.DateFormat
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.time.Instant
import java.time.ZoneId
import java.util.Date

enum class DateTimePickerDialogState {
    Closed, Date, Time
}

class DateTimePickerState(initialDateTime: Long = Instant.now().toEpochMilli()) {
    private var _initialDateTime = mutableLongStateOf(initialDateTime)
    private var _selectedDateTime = mutableLongStateOf(initialDateTime)
    private var _dialogState: MutableState<DateTimePickerDialogState> =
        mutableStateOf(DateTimePickerDialogState.Closed)

    var initialDateTime: Long
        get() = _initialDateTime.longValue
        set(value) {
            _initialDateTime.longValue = value
            selectedDateTime = value
        }

    var selectedDateTime: Long
        get() = _selectedDateTime.longValue
        set(newDateTime) {
            _selectedDateTime.longValue = newDateTime
        }

    var dialogState: DateTimePickerDialogState
        get() = _dialogState.value
        set(newState) {
            _dialogState.value = newState
        }

    private constructor(storedState: List<Any>) : this() {
        _initialDateTime = mutableLongStateOf(storedState[0] as Long)
        _selectedDateTime = mutableLongStateOf(storedState[1] as Long)
        _dialogState = mutableStateOf(DateTimePickerDialogState.entries[storedState[2] as Int])
    }

    fun selectedDate(): Date = Date.from(Instant.ofEpochMilli(selectedDateTime))

    fun isOpen() = dialogState != DateTimePickerDialogState.Closed

    fun open() {
        if (dialogState == DateTimePickerDialogState.Closed) {
            dialogState = DateTimePickerDialogState.Date
        }
    }

    fun close() {
        dialogState = DateTimePickerDialogState.Closed
    }

    companion object {
        val SAVER: Saver<DateTimePickerState, *> = listSaver(
            save = { listOf(it.initialDateTime, it.selectedDateTime, it.dialogState.ordinal) },
            restore = { DateTimePickerState(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    state: DateTimePickerState,
    content: @Composable () -> Unit,
) {
    // TODO: The initialDateTime seems to be wrong, as the calendar sometimes starts on the wrong day
    val datePickerState =
        rememberDatePickerStateKeyed(state.initialDateTime)

    val timePickerState = rememberTimePickerStateKeyed(state.initialDateTime)

    if (state.isOpen()) {
        OkayCancelDialog(
            onDismiss = { state.close() },
            onOkay = {
                when (state.dialogState) {
                    DateTimePickerDialogState.Closed -> state.dialogState =
                        DateTimePickerDialogState.Closed

                    DateTimePickerDialogState.Date -> state.dialogState =
                        DateTimePickerDialogState.Time

                    DateTimePickerDialogState.Time -> {
                        state.dialogState = DateTimePickerDialogState.Closed

                        // Update the selected date
                        val selectedDate = datePickerState.selectedDateMillis
                        if (selectedDate != null) {
                            val localDateTime =
                                Instant.ofEpochMilli(selectedDate).atZone(ZoneId.of("UTC"))
                                    .toLocalDateTime().plusHours(timePickerState.hour.toLong())
                                    .plusMinutes(timePickerState.minute.toLong())
                            state.selectedDateTime =
                                localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                                    .toEpochMilli()
                        }
                    }
                }
            }
        ) {
            when (state.dialogState) {
                DateTimePickerDialogState.Closed -> {}
                DateTimePickerDialogState.Date -> {
                    DatePicker(state = datePickerState)
                }

                DateTimePickerDialogState.Time -> {
                    TimePicker(state = timePickerState)
                }
            }
        }
    }

    content()
}

// TODO: This does not remember with saveable which it should
// However, the current api from compose is incredibly limiting and it would be very annoying to implement it.
// Maybe support for this use case gets added in the future.
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun rememberDatePickerStateKeyed(initialSelectedDateMillis: Long): DatePickerState {
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
fun rememberTimePickerStateKeyed(initialDateTime: Long): TimePickerState {
    val context = LocalContext.current
    return rememberSaveable(context, initialDateTime, saver = TimePickerState.Saver()) {
        val calendar = Calendar.getInstance().apply {
            time = Date.from(Instant.ofEpochMilli(initialDateTime))
        }
        val initialMinute = calendar.get(Calendar.MINUTE)
        val initialHour = calendar.get(Calendar.HOUR)
        val is24Hour = DateFormat.is24HourFormat(context)
        TimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = is24Hour
        )
    }
}