package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.gpx.TrackSvg
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.localPreferences
import com.inky.fitnesscalendar.ui.util.sharedElement
import com.inky.fitnesscalendar.ui.util.skipToLookaheadSize
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.layers.BackgroundLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.DisappearingCompassButton
import org.maplibre.compose.overlay.DisappearingScaleBar
import org.maplibre.compose.overlay.LocalCameraPadding
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position


@Composable
fun Map(
    trackSvg: TrackSvg,
    modifier: Modifier = Modifier,
    allowInteractions: Boolean = true,
    onClick: () -> ClickResult = { ClickResult.Pass }
) {
    val lineFeature = remember(trackSvg) {
        val geometry = LineString(trackSvg.points.map {
            Position(
                latitude = it.latitude,
                longitude = it.longitude
            )
        })
        Feature(geometry, null)
    }
    val startPoint = remember(trackSvg) {
        val point = trackSvg.points.first()
        Feature(Point(latitude = point.latitude, longitude = point.longitude), null)
    }
    val endPoint = remember(trackSvg) {
        val point = trackSvg.points.last()
        Feature(Point(latitude = point.latitude, longitude = point.longitude), null)
    }

    val uri = localPreferences.current.mapProviderUrl.takeIf { it.isNotBlank() }
    val baseStyle = uri?.let { BaseStyle.Uri(it) } ?: BaseStyle.Empty
    val mapState = rememberMapState(baseStyle = baseStyle) {
        if (uri == null) {
            BackgroundLayer(
                id = "background",
                color = const(MaterialTheme.colorScheme.primary)
            )
        }

        val lineSource = rememberGeoJsonSource(GeoJsonData.Features(lineFeature))
        LineLayer(
            id = "track-path",
            source = lineSource,
            color = const(Color.Blue),
            width = const(4.dp)
        )

        val locationIcon = image(painterResource(Icons.Location.resourceId), drawAsSdf = true)
        // This hardcoded offset has the purpose of putting the tip of the pin on the coordinate,
        // instead of its center
        val locationIconOffset = const(DpOffset(0.dp, (-12).dp))

        val startSource = rememberGeoJsonSource(GeoJsonData.Features(startPoint))
        SymbolLayer(
            id = "start-point",
            source = startSource,
            iconImage = locationIcon,
            iconOffset = locationIconOffset,
            iconColor = const(colorResource(R.color.start_point)),
        )

        val endSource = rememberGeoJsonSource(GeoJsonData.Features(endPoint))
        SymbolLayer(
            id = "end-point",
            source = endSource,
            iconImage = locationIcon,
            iconOffset = locationIconOffset,
            iconColor = const(colorResource(R.color.end_point)),
        )
    }

    LaunchedEffect(trackSvg) {
        val boundingBox = trackSvg.bounds.toMapLibreBoundingBox()
        mapState.setCameraPosition(
            mapState.cameraForBounds(
                boundingBox,
                padding = PaddingValues(all = 32.dp)
            )
        )
    }

    val baseInteractions = if (allowInteractions) MapInteractions.Standard else MapInteractions.None
    MaplibreMap(
        state = mapState,
        interactions = MapInteractions(from = baseInteractions) {
            callbacks {
                click { onEvent { _ -> onClick() } }
            }
        },
        modifier = Modifier
            .sharedElement(SharedContentKey.Map)
            .skipToLookaheadSize() then modifier
    ) {
        val mapState = checkNotNull(LocalMapState.current)
        Box(
            Modifier
                .fillMaxSize()
                .padding(LocalCameraPadding.current)
                .consumeWindowInsets(LocalCameraPadding.current)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(MapOverlay.Spacing),
        ) {
            DisappearingScaleBar(
                metersPerDp = mapState.viewport?.metersPerDpAtTarget ?: 0.0,
                zoom = mapState.cameraPosition.zoom,
                modifier = Modifier.align(Alignment.TopStart),
            )

            DisappearingCompassButton(modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}