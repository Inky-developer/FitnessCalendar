package com.inky.fitnesscalendar.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.util.copyFileToStorage
import com.inky.fitnesscalendar.util.getOrCreateImagesDir

@Composable
fun ActivityImage(
    uri: Uri,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
) {
    AsyncImage(
        model = uri,
        contentDescription = stringResource(R.string.user_uploaded_image),
        onState = onState,
        contentScale = ContentScale.FillWidth,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 256.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable { onClick() }
    )
}

/**
 * A menu button that displays an image picker.
 * @param imagePickerLauncher: The launcher to run when the item is clicked. This launcher should be initialized unconditionally with [rememberImagePickerLauncher]
 * @param onDismissMenu: Callback for when the menu should get dismissed
 */
@Composable
fun SelectImageDropdownMenuItem(
    imagePickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    onDismissMenu: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.add_image)) },
        leadingIcon = {
            Icon(
                painterResource(R.drawable.outline_add_image_24),
                stringResource(R.string.add_image)
            )
        },
        onClick = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
            onDismissMenu()
        }
    )
}

@Composable
fun rememberImagePickerLauncher(onUri: (Uri) -> Unit, context: Context = LocalContext.current) =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        println(uri)
        if (uri != null) {
            val actualUri = context.copyFileToStorage(
                uri,
                context.getOrCreateImagesDir()
            )
            println(uri)
            if (actualUri != null) {
                onUri(actualUri)
            }
        }
    }