package com.inky.fitnesscalendar.ui.views

import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.inky.fitnesscalendar.R


enum class View(
    val nameId: Int,
    private val navId: String,
    private val arguments: List<Argument<*>> = emptyList()
) {
    Home(R.string.today, "home"),
    ActivityLog(R.string.activity_log, "activity_log"),
    FilterActivity(R.string.filter, "filter_activity"),
    NewActivity(R.string.new_activity, "new_activity", listOf(Argument.ACTIVITY_ID)),
    ImportExport(R.string.import_export, "import_export"),
    Settings(R.string.settings, "settings");

    fun getPath(): String = if (arguments.isEmpty()) {
        pathTemplate()
    } else {
        throw Error("Must be called with arguments")
    }

    protected fun getPath(args: List<String>): String = navId + args.joinToString("/", prefix = "/")

    fun getPath(vararg args: Any) = getPath(args.map { it.toString() })

    fun pathTemplate(): String = if (arguments.isEmpty()) {
        navId
    } else {
        navId + arguments.joinToString("/", prefix = "/") { "{${it.id}}" }
    }

    fun navArgs() = arguments.map {
        navArgument(it.id) {
            type = it.type
            nullable = it.nullable
        }
    }

    abstract class Argument<T>(
        val id: String,
        val type: NavType<T>,
        val nullable: Boolean = false,
        val default: T? = null
    ) {
        abstract fun extract(bundle: Bundle): T?

        companion object {
            val ACTIVITY_ID =
                object : Argument<Int>("activity_id", NavType.IntType, default = -1) {
                    override fun extract(bundle: Bundle) =
                        when (val value = bundle.getInt(id, -1)) {
                            -1 -> null
                            else -> value
                        }
                }
        }
    }
}