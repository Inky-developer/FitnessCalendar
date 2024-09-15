package com.inky.fitnesscalendar.preferences

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.view_model.statistics.Projection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

sealed class Preference<T, P>(
    private val key: Preferences.Key<P>,
    private val default: T,
    @StringRes var titleId: Int? = null,
    @StringRes var descriptionId: Int? = null,
) {
    fun flow(context: Context): Flow<T> {
        return context.dataStore.data.map { store ->
            store[key]?.let { deserialize(it) } ?: default
        }
    }

    suspend fun get(context: Context): T {
        return flow(context).first()
    }

    @Composable
    fun collectAsState(context: Context = LocalContext.current) =
        remember { flow(context) }.collectAsState(initial = default)

    suspend fun set(context: Context, value: T) {
        context.dataStore.edit { settings ->
            settings[key] = serialize(value)
        }
    }

    abstract fun deserialize(primitive: P): T

    abstract fun serialize(value: T): P

    class PrimitivePreference<T> private constructor(key: Preferences.Key<T>, default: T) :
        Preference<T, T>(key, default) {
        override fun serialize(value: T): T = value

        override fun deserialize(primitive: T): T = primitive

        companion object {
            fun create(name: String, default: Boolean) =
                PrimitivePreference(booleanPreferencesKey(name), default)

            fun create(name: String, default: Int) =
                PrimitivePreference(intPreferencesKey(name), default)

            fun create(name: String, default: Long) =
                PrimitivePreference(longPreferencesKey(name), default)

            fun create(name: String, default: Float) =
                PrimitivePreference(floatPreferencesKey(name), default)

            fun create(name: String, default: Double) =
                PrimitivePreference(doublePreferencesKey(name), default)

            fun create(name: String, default: String) =
                PrimitivePreference(stringPreferencesKey(name), default)

            fun create(name: String, default: Set<String>) =
                PrimitivePreference(stringSetPreferencesKey(name), default)
        }
    }

    class EnumPreference<E : Enum<E>>(name: String, private val enumClass: KClass<E>) :
        Preference<E, Int>(intPreferencesKey(name), enumClass.java.enumConstants!![0]) {
        override fun serialize(value: E): Int = value.ordinal

        override fun deserialize(primitive: Int): E =
            enumClass.java.enumConstants?.getOrNull(primitive) ?: super.default
    }

    companion object {
        val PREF_STATS_PROJECTION = EnumPreference("PREF_STATS_PROJECTION", Projection::class)
        val PREF_BACKUP_URI = PrimitivePreference.create("PREF_BACKUP_URI", "")

        val COLLECT_BSSID = PrimitivePreference.create("PREF_COLLECT_BSSID", false).apply {
            titleId = R.string.setting_title_store_location_information
            descriptionId = R.string.setting_description_store_location_information
        }
    }
}