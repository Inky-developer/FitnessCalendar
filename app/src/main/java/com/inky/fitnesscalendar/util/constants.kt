package com.inky.fitnesscalendar.util

import com.inky.fitnesscalendar.BuildConfig


const val APP_URL = BuildConfig.APPLICATION_ID

const val DATABASE_NAME = "activity_db"

const val NOTIFICATION_CHANNEL_RECORD = "record"

const val ACTION_CANCEL = "$APP_URL.action.cancel"
const val ACTION_SAVE = "$APP_URL.action.save"

const val EXTRA_RECORDING_ID = "recording_id"