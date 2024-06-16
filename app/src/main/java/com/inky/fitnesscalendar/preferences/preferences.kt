package com.inky.fitnesscalendar.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.inky.fitnesscalendar.view_model.statistics.Projection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

sealed class Preference<T, P>(private val key: Preferences.Key<P>, private val default: T) {
    fun flow(context: Context): Flow<T> {
        return context.dataStore.data.map { store ->
            store[key]?.let { deserialize(it) } ?: default
        }
    }

    suspend fun get(context: Context): T {
        return flow(context).first()
    }

    suspend fun set(context: Context, value: T) {
        context.dataStore.edit { settings ->
            settings[key] = serialize(value)
        }
    }

    abstract fun deserialize(primitive: P): T

    abstract fun serialize(value: T): P

    class PrimitivePreference<T>(key: Preferences.Key<T>, default: T) :
        Preference<T, T>(key, default) {
        init {
            assert(default is Boolean || default is Int || default is Long || default is Float || default is Set<*>) { "T must be a primitive type" }
        }

        override fun serialize(value: T): T = value

        override fun deserialize(primitive: T): T = primitive
    }

    class EnumPreference<E : Enum<E>>(name: String, private val enumClass: KClass<E>) :
        Preference<E, Int>(intPreferencesKey(name), enumClass.java.enumConstants!![0]) {
        override fun serialize(value: E): Int = value.ordinal

        override fun deserialize(primitive: Int): E =
            enumClass.java.enumConstants?.getOrNull(primitive) ?: super.default
    }

    companion object {
        val PREF_STATS_PROJECTION = EnumPreference("PREF_STATS_PROJECTION", Projection::class)
    }
}