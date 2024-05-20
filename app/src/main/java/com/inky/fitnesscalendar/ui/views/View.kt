package com.inky.fitnesscalendar.ui.views

import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument


abstract class View(
    private val navId: String,
    private val arguments: List<Argument<*>> = emptyList()
) {
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

    companion object {
        val TODAY = object : View("today") {}
        val FILTER_ACTIVITY = object : View("filter_activity") {}
        val NEW_ACTIVITY = object : View("new_activity", listOf(Argument.ACTIVITY_ID)) {}
        val SETTINGS = object : View("settings") {}
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