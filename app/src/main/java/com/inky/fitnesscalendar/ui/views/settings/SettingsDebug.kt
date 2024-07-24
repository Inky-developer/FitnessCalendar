package com.inky.fitnesscalendar.ui.views.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.di.DecisionTrees.classifyNow
import com.inky.fitnesscalendar.ui.components.debug.DecisionTreeVisualization
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun SettingsDebug() {
    val context = LocalContext.current
    var classification by remember { mutableStateOf<ActivityType?>(null) }


    Column {
        TextButton(onClick = {
            DecisionTrees.activityType?.let {
                classification = it.classifyNow(context)
            }
        }) {
            Text("Classify")
        }

        Text(classification?.toString() ?: "")

        val tree = DecisionTrees.activityType
        if (tree != null) {
            DecisionTreeVisualization(
                tree = tree,
                attributes = remember {
                    listOf(
                        "Time of day" to { it: Any? ->
                            mapOf(
                                0 to "02:00 - 06:00",
                                1 to "06:00 - 10:00",
                                2 to "10:00 - 14:00",
                                3 to "14:00 - 18:00",
                                4 to "18:00 - 22:00",
                                5 to "22:00 - 02:00"
                            )[it] ?: "<error>"
                        },
                        "Day of week" to { it: Any? ->
                            (it as DayOfWeek).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        },
                        "Wifi ID" to { it: Any? -> it?.toString() ?: "<No Wifi>" }
                    )
                }
            )
        }
    }
}