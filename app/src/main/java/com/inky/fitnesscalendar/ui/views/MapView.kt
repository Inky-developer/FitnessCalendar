package com.inky.fitnesscalendar.ui.views

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.inky.fitnesscalendar.data.gpx.TrackSvg
import com.inky.fitnesscalendar.ui.components.TrackView
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.view_model.BaseViewModel
import kotlinx.coroutines.flow.map
import kotlin.math.max
import kotlin.math.min

@Composable
fun MapView(viewModel: BaseViewModel = hiltViewModel(), activityId: Int, onBack: () -> Unit) {
    val trackSvg by remember(activityId) {
        viewModel.repository.getActivity(activityId).map { it.activity.trackPreview?.toTrackSvg() }
    }.collectAsState(null)

    if (trackSvg != null) {
        MapView(trackSvg!!, onBack)
    }
}

@Composable
private fun MapView(trackSvg: TrackSvg, onBack: () -> Unit) {
    // ToDo: Implement proper zooming
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        zoom *= zoomChange
        zoom = min(max(zoom, 0.5f), 3f)
        offset += panChange
    }

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .fillMaxSize()
            .transformable(transformState)
    ) {
        TrackView(
            trackSvg,
            color = Color.Black,
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxSize()
                .sharedBounds(SharedContentKey.Map)
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}