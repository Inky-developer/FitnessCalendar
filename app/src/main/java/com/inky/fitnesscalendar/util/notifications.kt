package com.inky.fitnesscalendar.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import com.inky.fitnesscalendar.MainActivity
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityType
import java.util.Locale

fun Context.showRecordingNotification(recordingId: Int, recordingType: ActivityType, startTimeMs: Long) {
    if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        return
    }

    val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_RECORD,
        getString(R.string.recordings),
        NotificationManager.IMPORTANCE_HIGH
    )
    notificationManager.createNotificationChannel(channel)

    val title = getString(
        R.string.recording_activity_type,
        getString(recordingType.nameId).lowercase(Locale.getDefault())
    )
    val launchIntent = Intent(this, MainActivity::class.java)
    val pendingIntent =
        PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE)
    val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_RECORD).apply {
        setPriority(NotificationCompat.PRIORITY_DEFAULT)
        setOnlyAlertOnce(true)
        setOngoing(true)
        setCategory(NotificationCompat.CATEGORY_SERVICE)
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setUsesChronometer(true)
        setWhen(startTimeMs)
        setContentTitle(title)
        setSmallIcon(R.drawable.outline_timer_24)
        setContentIntent(pendingIntent)
    }
    notificationManager.notify(recordingId, builder.build())
}

fun Context.hideRecordingNotification(recordingId: Int) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.cancel(recordingId)
}