package com.inky.fitnesscalendar.ui.views.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.ui.components.ActivityImage
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.components.ColorSelector
import com.inky.fitnesscalendar.ui.components.DescriptionTextInput
import com.inky.fitnesscalendar.ui.components.ImageLimit
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.ui.components.SelectImageDropdownMenuItem
import com.inky.fitnesscalendar.ui.components.optionGroupDefaultBackground
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.view_model.PlaceListViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun EditPlaceDialog(
    viewModel: PlaceListViewModel = hiltViewModel(),
    initialPlaceId: Int?,
    onDismiss: () -> Unit,
) {
    val place by (initialPlaceId?.let { viewModel.get(it) }
        ?: flowOf(null)).collectAsState(initial = null)
    if (initialPlaceId == null || place != null) {
        EditPlaceDialog(
            initialPlace = place,
            onDismiss = onDismiss,
            onSave = {
                viewModel.save(it)
                onDismiss()
            }
        )
    } else {
        CircularProgressIndicator()
    }
}

@Composable
private fun EditPlaceDialog(initialPlace: Place?, onDismiss: () -> Unit, onSave: (Place) -> Unit) {
    val title = if (initialPlace != null) {
        stringResource(R.string.edit_object, initialPlace.name)
    } else {
        stringResource(R.string.new_place)
    }

    var imageName by rememberSaveable(initialPlace) { mutableStateOf(initialPlace?.imageName) }
    var showImageViewer by rememberSaveable { mutableStateOf(false) }

    var name by rememberSaveable(initialPlace) { mutableStateOf(initialPlace?.name ?: "") }
    var color by rememberSaveable(initialPlace) { mutableStateOf(initialPlace?.color) }
    var description by rememberSaveable(initialPlace) {
        mutableStateOf(
            initialPlace?.description ?: ""
        )
    }

    val saveEnabled by remember {
        derivedStateOf { name.isNotBlank() && color != null }
    }

    BaseEditDialog(
        title = title,
        onNavigateBack = onDismiss,
        onSave = {
            onSave(
                Place(
                    uid = initialPlace?.uid,
                    name = name,
                    color = color!!,
                    imageName = imageName,
                    description = description
                )
            )
        },
        saveEnabled = saveEnabled,
        actions = {
            var showMenu by rememberSaveable { mutableStateOf(false) }
            IconButton(onClick = { showMenu = true }) {
                Icons.MoreOptions(stringResource(R.string.open_context_menu))
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                SelectImageDropdownMenuItem(
                    imageLimit = ImageLimit.Single,
                    onImages = { imageName = it.first() },
                    onDismissMenu = { showMenu = false },
                )
            }
        }
    ) {

        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedContent(
                targetState = imageName?.getImageUri(),
                label = stringResource(R.string.user_uploaded_image)
            ) { imageUri ->
                if (imageUri != null) {
                    ActivityImage(
                        uri = imageUri,
                        onState = { state ->
                            if (state is AsyncImagePainter.State.Error) {
                                imageName = initialPlace?.imageName
                            }
                        },
                        onClick = { showImageViewer = true },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            TextField(
                value = name,
                onValueChange = { name = it },
                leadingIcon = { Icons.Edit(stringResource(R.string.type_name)) },
                placeholder = { Text(stringResource(R.string.name_of_place)) },
                singleLine = true,
                keyboardOptions = remember { KeyboardOptions(capitalization = KeyboardCapitalization.Words) },
                colors = TextFieldDefaults.colors(unfocusedContainerColor = optionGroupDefaultBackground()),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            OptionGroup(
                label = stringResource(R.string.select_color),
                selectionLabel = color?.text(),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                ColorSelector(
                    isSelected = { color == it },
                    onSelect = { color = it }
                )
            }

            DescriptionTextInput(
                description = description,
                onDescription = { description = it },
                maxLines = 3,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }

    val imageUri = imageName?.getImageUri()
    if (showImageViewer && imageUri != null) {
        ImageViewer(
            imageUri = imageUri,
            onDismiss = { showImageViewer = false },
            onDelete = {
                imageName = null
                showImageViewer = false
            }
        )
    }
}