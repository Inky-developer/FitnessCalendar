package com.inky.fitnesscalendar.ui.components

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.TimeZone

enum class DateTimePickerDialogState {
    Closed, Date, Time
}

class DateTimePickerState(initialDateTime: Long = Instant.now().toEpochMilli()) : Parcelable {
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

    constructor(parcel: Parcel) : this() {
        _initialDateTime = mutableLongStateOf(parcel.readLong())
        _selectedDateTime = mutableLongStateOf(parcel.readLong())
        _dialogState = mutableStateOf(DateTimePickerDialogState.entries[parcel.readInt()])
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(_initialDateTime.longValue)
        parcel.writeLong(_selectedDateTime.longValue)
        parcel.writeInt(_dialogState.value.ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DateTimePickerState> {
        override fun createFromParcel(parcel: Parcel): DateTimePickerState {
            return DateTimePickerState(parcel)
        }

        override fun newArray(size: Int): Array<DateTimePickerState?> {
            return arrayOfNulls(size)
        }
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