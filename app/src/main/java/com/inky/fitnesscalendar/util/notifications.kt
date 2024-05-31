package com.inky.fitnesscalendar.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.inky.fitnesscalendar.MainActivity
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.broadcast_receiver.NotificationBroadcastReceiver
import com.inky.fitnesscalendar.data.ActivityType

fun Context.showRecordingNotification(
    recordingId: Int,
    recordingType: ActivityType,
    startTimeMs: Long
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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

    val title = getString(recordingType.nameId)

    val launchIntent = Intent(this, MainActivity::class.java)
    val pendingIntent =
        PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE)


    val cancelIntent = notificationBroadcastIntent(recordingId) {
        action = ACTION_CANCEL
    }

    val saveIntent = notificationBroadcastIntent(recordingId) {
        action = ACTION_SAVE
    }

    val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_RECORD).apply {
        setPriority(NotificationCompat.PRIORITY_MAX)
        setOnlyAlertOnce(true)
        setOngoing(true)
        setCategory(NotificationCompat.CATEGORY_SERVICE)
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setUsesChronometer(true)
        setWhen(startTimeMs)
        setContentTitle(title)
        setSmallIcon(R.drawable.outline_timer_24)
        setContentIntent(pendingIntent)
        addAction(R.drawable.ic_notification_cancel, getString(R.string.abort), cancelIntent)
        addAction(R.drawable.ic_notification_save, getString(R.string.save), saveIntent)
    }
    notificationManager.notify(recordingId, builder.build())
}

fun Context.hideRecordingNotification(recordingId: Int) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.cancel(recordingId)
}

private fun Context.notificationBroadcastIntent(
    recordingId: Int,
    body: Intent.() -> Unit
): PendingIntent {
    val intent = Intent(this, NotificationBroadcastReceiver::class.java).apply {
        body()
        putExtra(EXTRA_RECORDING_ID, recordingId)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        this, recordingId, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
    )

    return pendingIntent
}