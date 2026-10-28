package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.RichDay
import com.inky.fitnesscalendar.db.entities.totalSteps
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedElement

@Composable
fun StepsIcon(day: RichDay) {
    if (day.steps.isEmpty()) {
        return
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .sharedElement(SharedContentKey.StepsIcon(day.epochDay))
    ) {
        Text(
            day.steps.totalSteps().toString(),
            modifier = Modifier.padding(end = 4.dp)
        )
        Icons.Step(stringResource(R.string.steps))
    }
}