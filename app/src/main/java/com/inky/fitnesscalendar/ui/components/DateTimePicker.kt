package com.inky.fitnesscalendar.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.TimeZone

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
    // TODO: Make the date and time pickers show the correct initial values
    val datePickerState =
        rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

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
                            state.selectedDateTime = Instant.ofEpochMilli(selectedDate)
                                .plus(timePickerState.hour.toLong(), ChronoUnit.HOURS).plus(
                                    timePickerState.minute.toLong(), ChronoUnit.MINUTES
                                ).toEpochMilli().withTimezoneCorrected()
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


private fun Long.withTimezoneCorrected(): Long {
    val timezoneOffset = TimeZone.getDefault().getOffset(this)
    return this - timezoneOffset
}