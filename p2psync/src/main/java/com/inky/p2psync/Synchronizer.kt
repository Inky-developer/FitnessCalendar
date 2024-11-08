package com.inky.p2psync

import kotlinx.serialization.json.Json

class Synchronizer(val versionID: Int, val deviceName: String, val models: List<Model>) {
    suspend fun pull(file: String) {
        val log = Json.decodeFromString<SynchronizationLog>(file)
        assert(versionID == log.versionID) { "TODO: Handle mismatching versions" }
        assert(deviceName == log.deviceName) { "TODO: Handle mismatching device name" }

        for (entry in log.entries) {
            println(entry)
        }
    }
}