package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedElement

@Composable
fun InfoBox(message: String, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Icons.Info(
            stringResource(R.string.info),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(32.dp)
                .aspectRatio(1f)
                .align(Alignment.CenterVertically)
        )
        Text(
            message,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun NoActivitiesInfoBox(isFilterEmpty: Boolean, modifier: Modifier = Modifier) {
    val textId = if (isFilterEmpty) {
        R.string.no_activities_yet
    } else {
        R.string.no_activities_with_filter
    }

    InfoBox(
        stringResource(textId),
        modifier = modifier.sharedElement(SharedContentKey.NoActivitiesInfoBox)
    )
}