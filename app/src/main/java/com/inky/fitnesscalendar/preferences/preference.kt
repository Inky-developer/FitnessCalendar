package com.inky.fitnesscalendar.preferences

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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
import java.util.Date
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

    suspend fun set(context: Context, value: T) = update(context) { value }

    suspend fun update(context: Context, func: (T) -> T) {
        context.dataStore.edit { settings ->
            val value = settings[key]?.let { deserialize(it) } ?: default
            settings[key] = serialize(func(value))
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

    class UriPreference(name: String) : Preference<Uri?, String>(stringPreferencesKey(name), null) {
        override fun serialize(value: Uri?): String {
            return value?.toString() ?: ""
        }

        override fun deserialize(primitive: String): Uri? {
            return if (primitive.isBlank()) {
                null
            } else {
                primitive.toUri()
            }
        }
    }

    class UriSetPreference(name: String) :
        Preference<Set<Uri>, Set<String>>(stringSetPreferencesKey(name), emptySet()) {
        override fun serialize(value: Set<Uri>): Set<String> {
            return value.map { it.toString() }.toSet()
        }

        override fun deserialize(primitive: Set<String>): Set<Uri> {
            return primitive.map { it.toUri() }.toSet()
        }

        suspend fun add(context: Context, value: Uri) = update(context) { it + value }
    }

    class DatePreference(name: String) :
        Preference<Date, Long>(longPreferencesKey(name), Date(0)) {
        override fun serialize(value: Date): Long {
            return value.time
        }

        override fun deserialize(primitive: Long): Date {
            return Date(primitive)
        }
    }

    companion object {
        val PREF_STATS_PROJECTION = EnumPreference("PREF_STATS_PROJECTION", Projection::class)
        val PREF_BACKUP_URI = UriPreference("PREF_BACKUP_URI")

        // Unused
//        val COLLECT_BSSID = PrimitivePreference.create("PREF_COLLECT_BSSID", false).apply {
//            titleId = R.string.setting_store_location_information_title
//            descriptionId = R.string.setting_store_location_information_description
//        }
        val PREF_ENABLE_PUBLIC_API =
            PrimitivePreference.create("PREF_ENABLE_PUBLIC_API", false).apply {
                titleId = R.string.setting_enable_public_api_title
                descriptionId = R.string.setting_enable_public_api_description
            }

        val PREF_WATCHED_FOLDERS = UriSetPreference("PREF_WATCHED_FOLDERS")
        val PREF_WATCHED_FOLDERS_LAST_IMPORT = DatePreference("PREF_WATCHED_FOLDERS_LAST_IMPORT")
        val PREF_PREFER_END_DATE_AS_DURATION =
            PrimitivePreference.create("PREF_PREFER_END_DATE_AS_DURATION", false)
    }
}