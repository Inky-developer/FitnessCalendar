package com.inky.fitnesscalendar.ui.util

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.inky.fitnesscalendar.R

object Icons {
    class Icon(@DrawableRes val resourceId: Int) {
        @Composable
        operator fun invoke(
            contentDescription: String,
            modifier: Modifier = Modifier,
            tint: Color = LocalContentColor.current,
        ) {
            Icon(
                painterResource(resourceId),
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
    }


    val Add = Icon(R.drawable.add_24px)
    val ArrowBack = Icon(R.drawable.arrow_back_24px)
    val ArrowForward = Icon(R.drawable.arrow_forward_24px)
    val CalendarToday = Icon(R.drawable.calendar_today_24px)
    val Check = Icon(R.drawable.check_24px)
    val Close = Icon(R.drawable.close_24px)
    val DateRange = Icon(R.drawable.date_range_24px)
    val Delete = Icon(R.drawable.delete_24px)
    val Edit = Icon(R.drawable.edit_24px)
    val Face = Icon(R.drawable.face_24px)
    val FavoriteFilled = Icon(R.drawable.favorite_filled_24px)
    val FavoriteOutlined = Icon(R.drawable.favorite_outlined_24px)
    val Info = Icon(R.drawable.info_24px)
    val KeyboardArrowLeft = Icon(R.drawable.keyboard_arrow_left_24px)
    val KeyboardArrowRight = Icon(R.drawable.keyboard_arrow_right_24px)
    val KeyboardArrowUp = Icon(R.drawable.keyboard_arrow_up_24px)
    val Location = Icon(R.drawable.location_on_24px)
    val Menu = Icon(R.drawable.menu_24px)
    val MoreOptions = Icon(R.drawable.more_vert_24px)
    val PlayArrow = Icon(R.drawable.play_arrow_24px)
    val PlayArrowFilled = Icon(R.drawable.play_arrow_filled_24px)
    val Search = Icon(R.drawable.search_24px)
    val Share = Icon(R.drawable.share_24px)
    val Timer = Icon(R.drawable.timer_24px)
    val Warning = Icon(R.drawable.warning_24px)
}