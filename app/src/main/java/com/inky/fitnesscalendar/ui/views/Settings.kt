package com.inky.fitnesscalendar.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.data.ActivityType
import com.inky.fitnesscalendar.di.ActivityTypeDecisionTree
import com.inky.fitnesscalendar.view_model.SettingsViewModel

@Composable
fun Settings() {
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
    }
}