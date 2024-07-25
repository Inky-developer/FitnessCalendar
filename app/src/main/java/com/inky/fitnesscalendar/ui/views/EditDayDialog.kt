package com.inky.fitnesscalendar.ui.views

import android.os.Parcelable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.components.FeelSelector
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.view_model.BaseViewModel
import kotlinx.parcelize.Parcelize

@Composable
fun EditDayDialog(
    viewModel: BaseViewModel = hiltViewModel(),
    epochDay: EpochDay,
    onNavigateBack: () -> Unit
) {
    val day by viewModel.repository.getDay(epochDay).collectAsState(initial = null)
    when (val dayValue = day) {
        null -> {
            CircularProgressIndicator()
        }

        else -> {
            EditDayDialog(viewModel = viewModel, day = dayValue, onNavigateBack = onNavigateBack)
        }
    }
}

@Composable
fun EditDayDialog(
    viewModel: BaseViewModel,
    day: Day,
    onNavigateBack: () -> Unit
) {
    var state by rememberSaveable(day) { mutableStateOf(EditDayState(day)) }

    BaseEditDialog(
        title = stringResource(R.string.edit_day),
        onNavigateBack = onNavigateBack,
        onSave = {
            viewModel.saveDay(state.toDay(day.day))
            onNavigateBack()
        },
        actions = {}
    ) {
        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .weight(1f, fill = false)
        ) {
            OptionGroup(
                label = stringResource(R.string.select_feel),
                selectionLabel = state.feel?.let { stringResource(it.nameId) }) {
                FeelSelector(
                    feel = state.feel,
                    onChange = { state = state.copy(feel = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
            }

            TextField(
                value = state.description,
                onValueChange = { state = state.copy(description = it) },
                placeholder = { Text(stringResource(R.string.placeholder_description)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 8,
                keyboardOptions = remember { KeyboardOptions(capitalization = KeyboardCapitalization.Sentences) },
                colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = MaterialTheme.shapes.small
            )
        }
    }
}

@Parcelize
data class EditDayState(val feel: Feel? = null, val description: String = "") : Parcelable {
    constructor(day: Day) : this(feel = day.feel, description = day.description)

    fun toDay(epochDay: EpochDay) = Day(day = epochDay, description = description, feel = feel)
}