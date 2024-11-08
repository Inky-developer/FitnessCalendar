package com.inky.p2psync

import kotlinx.serialization.Serializable

@Serializable
data class SynchronizationLog(
    val versionID: Int,
    val deviceName: String,
    val entries: List<Entry>
) {
    @Serializable
    sealed interface Entry {
        @Serializable
        data class Insert(val value: String)

        @Serializable
        data class Delete(val id: String)

        @Serializable
        data class Update(val id: String, val field: String, val value: String)
    }

}