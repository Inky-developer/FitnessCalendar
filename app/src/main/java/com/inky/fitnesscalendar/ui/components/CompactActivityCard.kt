package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.data.TypeActivity
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.util.skipToLookaheadSize

@Composable
fun CompactActivityCard(
    typeActivity: TypeActivity,
    localizationRepository: LocalizationRepository,
    modifier: Modifier = Modifier
) {
    val (activity, activityType) = typeActivity

    val title = remember(activityType) { "${activityType.emoji} ${activityType.name}" }
    val time = remember(activity) {
        localizationRepository.formatRelativeDate(activity.startTime)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 8.dp)
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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}