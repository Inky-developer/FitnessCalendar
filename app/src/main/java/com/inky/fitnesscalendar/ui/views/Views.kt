package com.inky.fitnesscalendar.ui.views

import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.view_model.statistics.Period


enum class Views(
    val nameId: Int,
    private val navId: String,
    private val arguments: List<Argument<*, *>> = emptyList()
) {
    Home(R.string.today, "home"),
    EditDay(R.string.edit_activity, "edit_activity", listOf(Argument.EPOCH_DAY)),
    ActivityLog(R.string.activity_log, "activity_log", listOf(Argument.ACTIVITY_ID)),
    FilterActivity(R.string.filter, "filter_activity"),
    NewActivity(R.string.new_activity, "new_activity", listOf(Argument.ACTIVITY_ID)),
    RecordActivity(R.string.record_activity, "record_activity"),
    ImportExport(R.string.import_export, "import_export"),
    Settings(R.string.settings, "settings"),
    Statistics(R.string.statistics, "statistics", listOf(Argument.INITIAL_PERIOD));

    fun getPath(): String = if (arguments.isEmpty()) {
        pathTemplate()
    } else {
        getPath(arguments.map { it.default.toString() })
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

    abstract class Argument<Ser, De>(
        val id: String,
        val type: NavType<Ser>,
        val default: Ser,
        val nullable: Boolean = false,
    ) {
        abstract fun extract(bundle: Bundle): De

        companion object {
            val ACTIVITY_ID =
                object : Argument<Int, Int?>("activity_id", NavType.IntType, default = -1) {
                    override fun extract(bundle: Bundle) =
                        when (val value = bundle.getInt(id, -1)) {
                            -1 -> null
                            else -> value
                        }
                }

            val INITIAL_PERIOD =
                object : Argument<String?, Period?>(
                    "initial_period",
                    NavType.StringType,
                    default = null,
                    nullable = true
                ) {
                    override fun extract(bundle: Bundle) =
                        bundle.getString(id)
                            ?.let { if (it != null.toString()) Period.valueOf(it) else null }
                }

            val EPOCH_DAY =
                object : Argument<Long, EpochDay>("epoch_day", NavType.LongType, default = 0) {
                    override fun extract(bundle: Bundle) = EpochDay(bundle.getLong(id))
                }
        }
    }
}