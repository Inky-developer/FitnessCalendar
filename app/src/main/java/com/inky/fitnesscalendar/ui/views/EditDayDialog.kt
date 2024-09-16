package com.inky.fitnesscalendar.ui.views

import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import coil.compose.AsyncImagePainter
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.ui.components.ActivityImage
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.components.FeelSelector
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.ui.components.SelectImageDropdownMenuItem
import com.inky.fitnesscalendar.ui.components.optionGroupDefaultBackground
import com.inky.fitnesscalendar.ui.components.rememberImagePickerLauncher
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
    var editState by rememberSaveable(day) { mutableStateOf(EditDayState(day)) }
    var showImageViewer by rememberSaveable { mutableStateOf(false) }

    val imagePickerLauncher =
        rememberImagePickerLauncher(onName = { editState = editState.copy(imageName = it) })

    BaseEditDialog(
        title = stringResource(R.string.edit_day),
        onNavigateBack = onNavigateBack,
        onSave = {
            viewModel.saveDay(editState.toDay(day.day))
            onNavigateBack()
        },
        actions = {
            var showMenu by rememberSaveable { mutableStateOf(false) }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Outlined.Menu, stringResource(R.string.Menu))
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                SelectImageDropdownMenuItem(
                    imagePickerLauncher = imagePickerLauncher,
                    onDismissMenu = { showMenu = false },
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .weight(1f, fill = false)
        ) {
            AnimatedContent(
                targetState = editState.imageName?.getImageUri(),
                label = stringResource(R.string.user_uploaded_image)
            ) { imageUri ->
                if (imageUri != null) {
                    ActivityImage(
                        uri = imageUri,
                        onState = { state ->
                            if (state is AsyncImagePainter.State.Error) {
                                editState = editState.copy(imageName = day.imageName)
                            }
                        },
                        onClick = { showImageViewer = true },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            OptionGroup(
                label = stringResource(R.string.select_feel),
                selectionLabel = editState.feel?.let { stringResource(it.nameId) }) {
                FeelSelector(
                    feel = editState.feel,
                    onChange = { editState = editState.copy(feel = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
            }

            TextField(
                value = editState.description,
                onValueChange = { editState = editState.copy(description = it) },
                placeholder = { Text(stringResource(R.string.placeholder_description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                maxLines = 8,
                keyboardOptions = remember { KeyboardOptions(capitalization = KeyboardCapitalization.Sentences) },
                colors = TextFieldDefaults.colors(unfocusedContainerColor = optionGroupDefaultBackground()),
                shape = MaterialTheme.shapes.small
            )
        }
    }

    val imageUri = editState.imageName?.getImageUri()
    if (showImageViewer && imageUri != null) {
        ImageViewer(
            imageUri = imageUri,
            onDismiss = { showImageViewer = false },
            onDelete = { editState = editState.copy(imageName = null) })
    }
}

@Parcelize
data class EditDayState(
    val feel: Feel? = null,
    val description: String = "",
    val imageName: ImageName? = null,
) : Parcelable {
    constructor(day: Day) : this(
        feel = day.feel,
        description = day.description,
        imageName = day.imageName
    )

    fun toDay(epochDay: EpochDay) =
        Day(day = epochDay, description = description, feel = feel, imageName = imageName)
}