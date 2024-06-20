package com.inky.fitnesscalendar.util

import com.inky.fitnesscalendar.BuildConfig
import com.inky.fitnesscalendar.R


const val APP_URL = BuildConfig.APPLICATION_ID

const val DATABASE_NAME = "activity_db"

const val NOTIFICATION_CHANNEL_RECORD = "record"

const val ACTION_CANCEL = "$APP_URL.action.cancel"
const val ACTION_SAVE = "$APP_URL.action.save"

const val EXTRA_RECORDING_ID = "recording_id"

const val ACTIVITY_IMAGES_DIR = "activity_images"
const val SHARED_MEDIA_DIR = "shared_media_cache"

val ACTIVITY_TYPE_COLOR_IDS =
    listOf(R.color.stats_1, R.color.stats_2, R.color.stats_3, R.color.stats_4, R.color.stats_5)