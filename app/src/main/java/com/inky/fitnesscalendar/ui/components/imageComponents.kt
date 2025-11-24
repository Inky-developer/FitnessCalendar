package com.inky.fitnesscalendar.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.util.NonEmptyList
import com.inky.fitnesscalendar.util.asNonEmptyOrNull
import com.inky.fitnesscalendar.util.copyFileToStorage
import com.inky.fitnesscalendar.util.getOrCreateImagesDir

const val IMAGE_ASPECT_RATIO: Float = 4 / 3f

@Composable
fun ActivityImages(
    images: NonEmptyList<ImageName>,
    modifier: Modifier = Modifier,
    onClick: (ImageName) -> Unit = {},
    onState: ((ImageName, AsyncImagePainter.State) -> Unit)? = null,
) {
    val imageScale = if (images.size == 1) 1f else 0.9f
    BoxWithConstraints(modifier = modifier) {
        val imageWidth = this.maxWidth * imageScale
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(images) { image ->
                ActivityImage(
                    uri = image.getImageUri(),
                    modifier = Modifier.width(imageWidth),
                    onClick = { onClick(image) },
                    onState = { state -> onState?.let { it(image, state) } }
                )
            }
        }
    }
}

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
        contentScale = ContentScale.Crop,
        modifier = modifier
            .aspectRatio(IMAGE_ASPECT_RATIO)
            .clip(MaterialTheme.shapes.large)
            .clickable { onClick() }
    )
}

enum class ImageLimit {
    Single,
    Multiple
}

/**
 * A menu button that displays an image picker.
 * @param imageLimit: Whether multiple images or just one image can be picked
 * @param onImages: Callback for when images have been picked
 * @param onDismissMenu: Callback for when the menu should get dismissed
 */
@Composable
fun SelectImageDropdownMenuItem(
    imageLimit: ImageLimit,
    onImages: (NonEmptyList<ImageName>) -> Unit,
    onDismissMenu: () -> Unit,
) {
    val selectedImages = rememberSaveable { mutableStateListOf<ImageName>() }

    LaunchedEffect(selectedImages.size) {
        selectedImages.asNonEmptyOrNull()?.let {
            onImages(it)
            onDismissMenu()
        }
    }

    val launcher = when (imageLimit) {
        ImageLimit.Single -> rememberImagePickerLauncher(onName = {
            selectedImages.add(it)
        })

        ImageLimit.Multiple -> rememberMultipleImagePickerLauncher(onImages = {
            selectedImages.addAll(it)
        })
    }

    val itemLabel = when (imageLimit) {
        ImageLimit.Single -> R.string.add_image
        ImageLimit.Multiple -> R.string.add_images
    }

    DropdownMenuItem(
        text = { Text(stringResource(itemLabel)) },
        leadingIcon = {
            Icon(
                painterResource(R.drawable.outline_add_image_24),
                stringResource(R.string.add_image)
            )
        },
        onClick = {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    )
}

@Composable
private fun rememberMultipleImagePickerLauncher(
    onImages: (NonEmptyList<ImageName>) -> Unit,
    context: Context = LocalContext.current
) = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
    val imageDir = context.getOrCreateImagesDir()
    val imageNames =
        uris.mapNotNull { context.copyFileToStorage(it, imageDir)?.name?.let { ImageName(it) } }
    imageNames.asNonEmptyOrNull()?.let { onImages(it) }
}

@Composable
private fun rememberImagePickerLauncher(
    onName: (ImageName) -> Unit,
    context: Context = LocalContext.current
) =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val name = context.copyFileToStorage(uri, context.getOrCreateImagesDir())?.name
            if (name != null) {
                onName(ImageName(name))
            }
        }
    }