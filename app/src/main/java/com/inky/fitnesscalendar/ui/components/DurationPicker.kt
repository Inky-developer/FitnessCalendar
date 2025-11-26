package com.inky.fitnesscalendar.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import java.time.Duration
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationPicker(duration: Duration, onConfirm: (Duration) -> Unit, onDismiss: () -> Unit) {
    // Prevent durations longer than 24 hours since they are not supported by the material time picker.
    // Maybe switch to a custom implementation later if we need longer input
    val asTime = LocalTime.ofSecondOfDay(
        minOf(
            duration.toMillis() / 1000,
            Duration.ofDays(1).toMillis() / 1000 - 1
        )
    )
    val timePickerState =
        rememberTimePickerState(initialHour = asTime.hour, initialMinute = asTime.minute)

    OkayCancelDialog(
        onDismiss = onDismiss,
        onOkay = {
            val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
            val duration = Duration.ofSeconds(time.toSecondOfDay().toLong())
            onConfirm(duration)
        }
    ) {
        TimeInput(state = timePickerState)
    }
}