package com.inky.fitnesscalendar.ui.views.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.ui.components.debug.DecisionTreeVisualization
import com.inky.fitnesscalendar.util.prediction.decision_tree.DecisionTree
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun SettingsDebug() {
    var classification by rememberSaveable { mutableStateOf<DecisionTrees.PredictionResult?>(null) }
    var selectedKindIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TextButton(
                onClick = { classification = DecisionTrees.classifyNow() }
            ) {
                Text("Classify")
            }

            Text(classification?.toString() ?: "")

            TabRow(selectedTabIndex = selectedKindIndex) {
                for ((index, kind) in TreeKind.entries.withIndex()) {
                    Tab(
                        text = { Text(text = kind.toString()) },
                        selected = index == selectedKindIndex,
                        onClick = { selectedKindIndex = index }
                    )
                }
            }

            AnimatedContent(targetState = selectedKindIndex, label = "TreeKind") { index ->
                val tree = remember(index) { TreeKind.entries[index].getTree() }
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
                                    (it as DayOfWeek).getDisplayName(
                                        TextStyle.SHORT,
                                        Locale.getDefault()
                                    )
                                },
                            )
                        }
                    )
                }
            }
        }
    }
}

private enum class TreeKind {
    ActivityType,
    Vehicle,
    Place;

    fun getTree(): DecisionTree<out Any>? = when (this) {
        ActivityType -> DecisionTrees.activityTypePredictor.tree
        Vehicle -> DecisionTrees.vehiclePredictor.tree
        Place -> DecisionTrees.placePredictor.tree
    }
}