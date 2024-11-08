package com.inky.p2psync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Date
import java.util.UUID

interface Model {
    suspend fun getCreatedObjects(since: Date): List<Serializable>

    suspend fun getUpdatedObjects(since: Date): List<Serializable>

    suspend fun getDeletedObjects(since: Date): List<UUID>

    suspend fun getObject(id: UUID): Serializable?

    suspend fun upsertObject(json: Json)

    suspend fun deleteObject(id: UUID)
}