package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.inky.fitnesscalendar.R

@Composable
fun DescriptionTextInput(
    description: String,
    onDescription: (String) -> Unit,
    maxLines: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier
) {
    TextField(
        value = description,
        onValueChange = onDescription,
        placeholder = { Text(stringResource(R.string.placeholder_description)) },
        keyboardOptions = remember { KeyboardOptions(capitalization = KeyboardCapitalization.Sentences) },
        colors = TextFieldDefaults.colors(unfocusedContainerColor = optionGroupDefaultBackground()),
        shape = MaterialTheme.shapes.small,
        maxLines = maxLines,
        modifier = modifier.fillMaxWidth()
    )
}