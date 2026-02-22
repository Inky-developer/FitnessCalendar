package com.inky.fitnesscalendar.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.service.quicksettings.TileService
import com.inky.fitnesscalendar.service.RecordTileService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            TileService.requestListeningState(
                context,
                ComponentName(context, RecordTileService::class.java)
            )
        }
    }
}
