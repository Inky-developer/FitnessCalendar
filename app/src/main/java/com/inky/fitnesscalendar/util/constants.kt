package com.inky.fitnesscalendar.util

import com.inky.fitnesscalendar.BuildConfig


const val APP_URL = BuildConfig.APPLICATION_ID

const val DATABASE_NAME = "activity_db"

const val NOTIFICATION_CHANNEL_RECORD = "record"

const val ACTION_CANCEL = "$APP_URL.action.cancel"
const val ACTION_SAVE = "$APP_URL.action.save"

const val EXTRA_RECORDING_ID = "recording_id"

const val IMAGES_DIR = "activity_images"
const val SHARED_MEDIA_DIR = "shared_media_cache"

// Support for VACUUM INTO was added at sqlite 3.28,
// which is supported at android sdk version 30 (Android 11) and higher
const val SDK_MIN_VERSION_FOR_SQLITE_VACUUM = 30

const val BACKUP_CACHE_FILE = "backup-temp.zip"
const val BACKUP_DB_NAME = "database.sqlite"

const val EXTRA_TOAST = "toast_message"

// Specifies by how many hours offset a day starts.
// E.g. A value of 2 means that the day goes from 2 am to 2 am next day
const val DAY_START_OFFSET_HOURS = 2L

enum class Ordering {
    ASC,
    DESC
}