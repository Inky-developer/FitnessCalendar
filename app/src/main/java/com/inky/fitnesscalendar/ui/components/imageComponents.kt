package com.inky.fitnesscalendar.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.db.entities.UserImage
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.util.NonEmptyList
import com.inky.fitnesscalendar.util.asNonEmptyOrNull
import com.inky.fitnesscalendar.util.copyFileToStorage
import com.inky.fitnesscalendar.util.getOrCreateImagesDir
import com.inky.fitnesscalendar.util.writeBitmapToStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val IMAGE_ASPECT_RATIO: Float = 4 / 3f

@Composable
fun ActivityImages(
    images: NonEmptyList<UserImage>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    onClick: (ImageName) -> Unit = {},
    onLongClick: ((ImageName) -> Unit)? = null,
    onState: ((ImageName, AsyncImagePainter.State) -> Unit)? = null,
) {
    val imageScale = if (images.size == 1) 1f else 0.9f
    BoxWithConstraints(modifier = modifier) {
        val imageWidth = this.maxWidth * imageScale
        LazyRow(state = state, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(images) { image ->
                ActivityImage(
                    uri = image.name.getImageUri(),
                    modifier = Modifier.width(imageWidth),
                    horizontalBias = image.horizontalBias,
                    verticalBias = image.verticalBias,
                    onClick = { onClick(image.name) },
                    onLongClick = onLongClick?.let { { it(image.name) } },
                    onState = { state -> onState?.let { it(image.name, state) } }
                )
            }
        }
    }
}

@Composable
fun ActivityImage(
    uri: Uri,
    modifier: Modifier = Modifier,
    horizontalBias: Float = 0f,
    verticalBias: Float = 0f,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
) {
    val clickEnabled = onClick != null || onLongClick != null
    AsyncImage(
        model = uri,
        contentDescription = stringResource(R.string.user_uploaded_image),
        onState = onState,
        contentScale = ContentScale.Crop,
        alignment = BiasAlignment(horizontalBias, verticalBias),
        modifier = modifier
            .aspectRatio(IMAGE_ASPECT_RATIO)
            .clip(MaterialTheme.shapes.large) then
                if (clickEnabled) Modifier.combinedClickable(
                    onClick = onClick ?: {},
                    onLongClick = onLongClick
                ) else Modifier
    )
}

@Composable
fun EditableActivityImages(
    images: NonEmptyList<UserImage>,
    onChange: (UserImage) -> Unit,
    onClick: (ImageName) -> Unit,
    modifier: Modifier = Modifier,
    onState: ((ImageName, AsyncImagePainter.State) -> Unit)? = null,
) {
    var currentEditImage by rememberSaveable { mutableStateOf<UserImage?>(null) }
    val state = rememberLazyListState()

    AnimatedContent(currentEditImage, contentKey = { it?.name }) { adjustingImage ->
        if (adjustingImage != null) {
            AdjustImageViewport(
                image = adjustingImage,
                onChange = { image ->
                    currentEditImage = image
                },
                onDone = {
                    onChange(adjustingImage)
                    currentEditImage = null
                },
                onCancel = {
                    currentEditImage = null
                },
                modifier = modifier,
            )
        } else {
            ActivityImages(
                images = images,
                state = state,
                onClick = onClick,
                onLongClick = { name -> currentEditImage = images.find { it.name == name } },
                onState = onState,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun AdjustImageViewport(
    image: UserImage,
    onChange: (UserImage) -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialImage = remember(image.name) { image }
    var intrinsicSize by remember(image.name) { mutableStateOf<Size?>(null) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    val latestBias = rememberUpdatedState(image.horizontalBias to image.verticalBias)
    val borderColor = MaterialTheme.colorScheme.primary

    BackHandler {
        onCancel()
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            model = image.name.getImageUri(),
            contentDescription = stringResource(R.string.adjust_image_viewport),
            onState = { state ->
                if (state is AsyncImagePainter.State.Success) {
                    val size = state.painter.intrinsicSize
                    if (size != Size.Unspecified) intrinsicSize = size
                }
            },
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(image.horizontalBias, image.verticalBias),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(IMAGE_ASPECT_RATIO)
                .clip(MaterialTheme.shapes.large)
                .border(2.dp, borderColor, MaterialTheme.shapes.large)
                .onSizeChanged { viewportSize = it }
                .pointerInput(image.name, intrinsicSize, viewportSize) {
                    val size = intrinsicSize ?: return@pointerInput
                    if (size.width <= 0 || size.height <= 0) return@pointerInput
                    if (viewportSize.width == 0 || viewportSize.height == 0) return@pointerInput
                    val scale = maxOf(
                        viewportSize.width / size.width,
                        viewportSize.height / size.height,
                    )
                    val dx = (size.width * scale - viewportSize.width).coerceAtLeast(0f)
                    val dy = (size.height * scale - viewportSize.height).coerceAtLeast(0f)
                    var h = 0f
                    var v = 0f
                    detectDragGestures(
                        onDragStart = {
                            h = latestBias.value.first
                            v = latestBias.value.second
                        },
                        onDrag = { change, offset ->
                            change.consume()
                            if (dx > 0f) h = (h - 2f * offset.x / dx).coerceIn(-1f, 1f)
                            if (dy > 0f) v = (v - 2f * offset.y / dy).coerceIn(-1f, 1f)
                            onChange(image.copy(verticalBias = v, horizontalBias = h))
                        }
                    )
                }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            AnimatedVisibility(image != initialImage) {
                FilledTonalIconButton(onClick = { onChange(initialImage) }) {
                    Icons.ResetImage(stringResource(R.string.reset_image_viewport))
                }
            }
            FilledIconButton(onClick = onDone) {
                Icons.Check(stringResource(R.string.done_adjusting_image))
            }
        }
    }
}

enum class ImageLimit {
    Single,
    Multiple
}

enum class MediaType {
    Image,
    Video;

    companion object {
        fun fromUri(context: Context, uri: Uri): MediaType? {
            val type = context.contentResolver.getType(uri) ?: return null
            return when {
                type.startsWith("image/") -> Image
                type.startsWith("video/") -> Video
                else -> null
            }
        }
    }
}

data class SelectedMediaState(
    val imageUris: List<Uri>,
    val videoUris: List<Uri>
) {
    companion object {
        fun fromUris(context: Context, uris: List<Uri>): SelectedMediaState {
            val imageUris = mutableListOf<Uri>()
            val videoUris = mutableListOf<Uri>()
            for (uri in uris) {
                when (MediaType.fromUri(context, uri)) {
                    MediaType.Image -> imageUris.add(uri)
                    MediaType.Video -> videoUris.add(uri)
                    null -> {}
                }
            }

            return SelectedMediaState(imageUris, videoUris)
        }
    }
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
    val context = LocalContext.current
    var receivedUris by rememberSaveable { mutableStateOf<SelectedMediaState?>(null) }

    LaunchedEffect(receivedUris) {
        if (receivedUris != null && receivedUris?.videoUris?.isEmpty() == true) {
            receivedUris?.imageUris?.let { uris ->
                val storedImages = saveImageUris(context, uris)
                storedImages.asNonEmptyOrNull()?.let {
                    onImages(it)
                    onDismissMenu()
                }
            }
        }
    }

    val launcher = when (imageLimit) {
        ImageLimit.Single -> rememberImagePickerLauncher(onUri = {
            receivedUris = SelectedMediaState.fromUris(context, listOf(it))
        })

        ImageLimit.Multiple -> rememberMultipleImagePickerLauncher(onUris = {
            receivedUris = SelectedMediaState.fromUris(context, it)
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
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
        }
    )

    val videoUris = receivedUris?.videoUris?.asNonEmptyOrNull()
    if (videoUris != null) {
        ExtractImageFromVideoPopup(videoUris, onFramesSelected = { frameUris ->
            receivedUris = receivedUris?.run {
                SelectedMediaState(
                    imageUris = imageUris + frameUris,
                    videoUris = emptyList()
                )
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun ExtractImageFromVideoPopup(
    videoUris: NonEmptyList<Uri>,
    onFramesSelected: (List<Uri>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var saving by rememberSaveable { mutableStateOf(false) }
    var savedFrames by rememberSaveable(videoUris) { mutableStateOf(emptyList<Uri?>()) }
    val videoUri = videoUris[savedFrames.size]

    val retriever = remember(videoUri) {
        MediaMetadataRetriever().apply {
            setDataSource(context, videoUri)
        }
    }
    val lastFrameIndex = remember(videoUri) {
        val numFrames =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                ?.toInt() ?: 1
        numFrames - 1
    }
    var frameIndex by rememberSaveable(videoUri) { mutableIntStateOf(0) }
    var loadingFrame by rememberSaveable(videoUri) { mutableStateOf(true) }
    var frameBitmap by rememberSaveable(videoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(videoUri) {
        snapshotFlow { frameIndex }.debounce(50).collectLatest { frameIndex ->
            loadingFrame = true
            try {
                frameBitmap = retriever.getFrameAtIndex(frameIndex)
            } finally {
                loadingFrame = false
            }
        }
    }

    Popup(properties = PopupProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = { Text(stringResource(R.string.select_frame_of_video)) }
                )
            },
            bottomBar = {
                BottomAppBar(
                    floatingActionButton = {
                        AnimatedVisibility(!saving) {
                            FloatingActionButton(
                                onClick = {
                                    if (saving) {
                                        return@FloatingActionButton
                                    }
                                    saving = true
                                    scope.launch(Dispatchers.IO) {
                                        val bitmap = retriever.getFrameAtIndex(frameIndex)
                                        val frameUri =
                                            bitmap?.let { context.writeBitmapToStorage(it)?.uri }
                                        val newSavedFrames = savedFrames + frameUri
                                        saving = false
                                        if (savedFrames.size + 1 < videoUris.size) {
                                            savedFrames = newSavedFrames
                                        } else {
                                            onFramesSelected(newSavedFrames.filterNotNull())
                                        }
                                    }
                                },
                                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                            ) {
                                if (savedFrames.size + 1 < videoUris.size) {
                                    Icons.KeyboardArrowRight(stringResource(R.string.next_video))
                                } else {
                                    Icons.Check(stringResource(R.string.select_frame_of_video))
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            enabled = frameIndex > 0,
                            onClick = {
                                frameIndex = (frameIndex - 1).coerceIn(0, lastFrameIndex)
                            }
                        ) {
                            Icons.KeyboardArrowLeft(stringResource(R.string.previous_frame))
                        }
                        IconButton(
                            enabled = frameIndex < lastFrameIndex,
                            onClick = {
                                frameIndex = (frameIndex + 1).coerceIn(0, lastFrameIndex)
                            }
                        ) {
                            Icons.KeyboardArrowRight(stringResource(R.string.next_frame))
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(innerPadding)
            ) {
                frameBitmap?.let { bitmap ->
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(loadingFrame, enter = fadeIn(), exit = fadeOut()) {
                            CircularProgressIndicator()
                        }
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(-1f)
                        )
                    }

                    Slider(
                        value = frameIndex.toFloat(),
                        valueRange = 0f..lastFrameIndex.toFloat(),
                        onValueChange = { frameIndex = it.roundToInt() },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(Color(0f, 0f, 0f, 0.25f))
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberMultipleImagePickerLauncher(onUris: (NonEmptyList<Uri>) -> Unit) =
    rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        uris.asNonEmptyOrNull()?.let(onUris)
    }

@Composable
private fun rememberImagePickerLauncher(onUri: (Uri) -> Unit) =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            onUri(uri)
        }
    }

private fun saveImageUris(context: Context, uris: List<Uri>): List<ImageName> {
    val imagesDir = context.getOrCreateImagesDir()
    return uris.mapNotNull { uri ->
        context.copyFileToStorage(uri, imagesDir)?.name?.let { ImageName(it) }
    }
}