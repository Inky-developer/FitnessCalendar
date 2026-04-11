package com.inky.fitnesscalendar.ui.views

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.inky.fitnesscalendar.data.gpx.TrackSvg
import com.inky.fitnesscalendar.ui.components.Map
import com.inky.fitnesscalendar.view_model.BaseViewModel
import kotlinx.coroutines.flow.map
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions

@Composable
fun MapView(viewModel: BaseViewModel = hiltViewModel(), activityId: Int, onBack: () -> Unit) {
    val trackSvg by remember(activityId) {
        viewModel.repository.getTrackByActivity(activityId)
            .map {
                it?.points?.let { points ->
                    TrackSvg.fromPoints(points.map { point -> point.coordinate })
                }
            }
    }.collectAsState(null)

    if (trackSvg != null) {
        MapView(trackSvg!!, onBack)
    }
}

@Composable
private fun MapView(trackSvg: TrackSvg, onBack: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Map(
            trackSvg = trackSvg,
            options = MapOptions(
                ornamentOptions = OrnamentOptions(
                    padding = WindowInsets.safeContent.asPaddingValues(),
                    isAttributionEnabled = false
                )
            ),
        )
    }
}