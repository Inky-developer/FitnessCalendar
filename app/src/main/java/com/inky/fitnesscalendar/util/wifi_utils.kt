package com.inky.fitnesscalendar.util

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log

private const val INSUFFICIENT_PERMISSION_BSSID: String = "02:00:00:00:00:00"
private const val TAG: String = "wifi_utils"

fun Context.getCurrentBssid(): String? {
    val wifiManager = getSystemService(WifiManager::class.java)
    // TODO: implement the more modern approach
    @Suppress("DEPRECATION") val connectionInfo = wifiManager.connectionInfo
    val bssid = connectionInfo.bssid
    if (bssid == INSUFFICIENT_PERMISSION_BSSID) {
        Log.e(TAG, "Got insufficient permissions to request bssid")
        return null
    }
    return bssid
}