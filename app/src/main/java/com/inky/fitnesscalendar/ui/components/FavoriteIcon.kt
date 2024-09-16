package com.inky.fitnesscalendar.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.inky.fitnesscalendar.R

@Composable
fun FavoriteIcon(isFavorite: Boolean, modifier: Modifier = Modifier) {
    if (isFavorite) {
        Icon(
            Icons.Filled.Favorite,
            stringResource(R.string.favorite),
            tint = colorResource(R.color.favorite),
            modifier = modifier
        )
    } else {
        Icon(
            Icons.Outlined.FavoriteBorder,
            stringResource(R.string.favorite),
            modifier = modifier
        )
    }
}