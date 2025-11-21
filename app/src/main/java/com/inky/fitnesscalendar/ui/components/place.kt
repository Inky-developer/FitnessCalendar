package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.ui.util.Icons

@Composable
fun PlaceInfo(place: Place, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        PlaceIcon(place)
        Text(
            place.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun PlaceIcon(place: Place) {
    Icons.Location(stringResource(R.string.place), tint = colorResource(place.color.colorId))
}