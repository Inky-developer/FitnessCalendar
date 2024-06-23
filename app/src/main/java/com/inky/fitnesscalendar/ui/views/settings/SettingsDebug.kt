package com.inky.fitnesscalendar.ui.views.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.di.ActivityTypeDecisionTree
import com.inky.fitnesscalendar.ui.components.debug.DecisionTreeVisualization
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun SettingsDebug() {
    var classification by remember { mutableStateOf<ActivityType?>(null) }


    Column {
        TextButton(onClick = {
            ActivityTypeDecisionTree.decisionTree?.let {
                classification = it.classifyNow()
            }
        }) {
            Text("Classify")
        }

        Text(classification?.toString() ?: "")

        val tree = ActivityTypeDecisionTree.decisionTree
        if (tree != null) {
            DecisionTreeVisualization(
                tree,
                remember {
                    listOf(
                        "Time of day" to mapOf(
                            0 to "02:00 - 06:00",
                            1 to "06:00 - 10:00",
                            2 to "10:00 - 14:00",
                            3 to "14:00 - 18:00",
                            4 to "18:00 - 22:00",
                            5 to "22:00 - 02:00"
                        ),
                        "Day of week" to (1..7).associateWith {
                            DayOfWeek.of((it as Int + 5) % 7 + 1)
                                .getDisplayName(TextStyle.FULL, Locale.getDefault())
                        }
                    )
                }
            )
        }
    }
}