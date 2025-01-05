package com.inky.fitnesscalendar.ui.views

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.inky.fitnesscalendar.db.entities.CoordinateRect
import com.inky.fitnesscalendar.db.entities.CoordinateRect.Companion.calculateBounds
import com.inky.fitnesscalendar.db.entities.Track
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.view_model.BaseViewModel
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun MapView(viewModel: BaseViewModel = hiltViewModel(), activityId: Int) {
    val track by remember(activityId) { viewModel.repository.getTrackByActivity(activityId) }
        .collectAsState(null)

    track?.let {
        InteractiveMap(it)
    }
}

@Composable
fun InteractiveMap(trackSvg: Track, modifier: Modifier = Modifier) {
    val mapView = rememberMapViewWithLifecycle(trackSvg)

    Surface(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Remembers a MapView and gives it the lifecycle of the current LifecycleOwner
 */
@Composable
private fun rememberMapViewWithLifecycle(track: Track): MapView {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapView = remember(track) {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isNestedScrollingEnabled = true
            ViewCompat.setNestedScrollingEnabled(this, true)


            scope.launch {
                val map = awaitMap()
                val styleUrl = loadStyleUrl(context)
                map.setStyle(styleUrl) { style ->
                    val coordinates =
                        track.points.joinToString(",") { "[${it.coordinate.longitude},${it.coordinate.latitude}]" }
                    val geojson =
                        """
                            {
                                "type": "Feature",
                                "properties": {},
                                "geometry": {
                                    "type": "Polygon",
                                    "coordinates": [
                                        [$coordinates]
                                    ]
                                }
                            }
                            """.trimIndent()

                    // Create feature object from the GeoJSON we declared.
                    val trackFeature = Feature.fromJson(geojson)
                    // Create a GeoJson Source from our feature.
                    val geojsonSource =
                        GeoJsonSource("track", trackFeature)
                    // Add the source to the style
                    style.addSource(geojsonSource)
                    // Create a layer with the desired style for our source.
                    val layer = LineLayer("track", "track")
                        .withProperties(
                            PropertyFactory.lineCap(Property.LINE_CAP_SQUARE),
                            PropertyFactory.lineJoin(Property.LINE_JOIN_MITER),
                            PropertyFactory.lineOpacity(0.7f),
                            PropertyFactory.lineWidth(4f),
                            PropertyFactory.lineColor("#0094ff")
                        )
                    // Add the layer at the end
                    style.addLayer(layer)
                }
                track.points.map { it.coordinate }.calculateBounds()?.let { bounds ->
                    map.getCameraForLatLngBounds(bounds.toLatLngBounds())
                        ?.let { map.cameraPosition = it }
                }
            }
        }
    }

    // Makes MapView follow the lifecycle of this composable
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> throw IllegalStateException()
        }
    }

private suspend inline fun MapView.awaitMap(): MapLibreMap =
    suspendCoroutine { continuation ->
        getMapAsync {
            continuation.resume(it)
        }
    }

private fun CoordinateRect.toLatLngBounds() =
    LatLngBounds.from(latitudeMax, longitudeMax, latitudeMin, longitudeMin)

private suspend fun loadStyleUrl(context: Context): String {
    val accessToken = Preference.PREF_JAWG_API_KEY.get(context)
    return STYLE_URL_WITHOUT_ACCESS_TOKEN + accessToken
}

private const val STYLE_URL_WITHOUT_ACCESS_TOKEN =
    "https://api.jawg.io/styles/jawg-terrain.json?access-token="