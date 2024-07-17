package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.data.ActivityTypeColor

@Composable
fun ColorSelector(
    isSelected: (ActivityTypeColor) -> Boolean,
    onSelect: (ActivityTypeColor) -> Unit
) {
    LazyRow {
        items(ActivityTypeColor.entries) { color ->
            FilterChip(
                selected = isSelected(color),
                onClick = { onSelect(color) },
                label = {
                    Surface(
                        color = colorResource(color.colorId),
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                            .clip(CircleShape)
                    ) {}
                },
                modifier = Modifier.padding(all = 4.dp)
            )
        }
    }
}