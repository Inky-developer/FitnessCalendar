package com.inky.fitnesscalendar.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.util.Icons

@Composable
fun FavoriteIcon(isFavorite: Boolean, modifier: Modifier = Modifier) {
    AnimatedContent(isFavorite) { isFavorite ->
        if (isFavorite) {
            Icons.FavoriteFilled(
                stringResource(R.string.favorite),
                tint = colorResource(R.color.favorite),
                modifier = modifier
            )
        } else {
            Icons.FavoriteOutlined(
                stringResource(R.string.favorite),
                modifier = modifier
            )
        }
    }
}