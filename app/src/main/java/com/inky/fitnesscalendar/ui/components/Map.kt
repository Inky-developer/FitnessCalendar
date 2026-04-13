package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.layers.BackgroundLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@Composable
fun Map(
    trackSvg: TrackSvg,
    options: MapOptions,
    modifier: Modifier = Modifier,
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

    val cameraState = rememberCameraState()
    LaunchedEffect(trackSvg) {
        val boundingBox = trackSvg.bounds.toMapLibreBoundingBox()
        cameraState.animateTo(boundingBox, padding = PaddingValues(32.dp))
    }

    val uri = localPreferences.current.mapProviderUrl.takeIf { it.isNotBlank() }
    val baseStyle = uri?.let { BaseStyle.Uri(it) } ?: BaseStyle.Empty
    MaplibreMap(
        cameraState = cameraState,
        baseStyle = baseStyle,
        onMapClick = { _, _ -> onClick() },
        options = options,
        modifier = modifier.sharedElement(SharedContentKey.Map)
    ) {
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
}