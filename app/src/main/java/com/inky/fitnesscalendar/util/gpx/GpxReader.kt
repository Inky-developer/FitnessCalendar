package com.inky.fitnesscalendar.util.gpx

import android.util.Log
import android.util.Xml
import com.inky.fitnesscalendar.data.gpx.Coordinate
import com.inky.fitnesscalendar.data.gpx.GpxTrack
import com.inky.fitnesscalendar.data.gpx.GpxTrackPoint
import com.inky.fitnesscalendar.data.measure.Elevation
import com.inky.fitnesscalendar.data.measure.HeartFrequency
import com.inky.fitnesscalendar.data.measure.Temperature
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.InputStream
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Date


private const val TAG = "GpxReader"

private const val TRACK_POINT_EXTENSIONS_GARMIN = "ns3:TrackPointExtension"
private const val EXTENSION_GARMIN_TEMPERATURE = "ns3:atemp"
private const val EXTENSION_GARMIN_HEART_RATE = "ns3:hr"

class GpxReader(val tracks: List<GpxTrack>) {
    companion object {
        fun read(inputStream: InputStream): GpxReader? {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)

            try {
                parser.setInput(inputStream, null)
                parser.nextTag()
                return readFeed(parser)
            } catch (e: XmlPullParserException) {
                Log.e(TAG, "Could not parse feed: $e")
                return null
            }
        }

        @Throws(XmlPullParserException::class)
        private fun readFeed(parser: XmlPullParser): GpxReader {
            val tracks = readTag(parser, "gpx") {
                when (it.name) {
                    "trk" -> readTrack(it)

                    else -> {
                        skipTag(it)
                        null
                    }
                }
            }

            if (tracks.isEmpty()) {
                throw XmlPullParserException("Gpx file contained no track")
            }
            return GpxReader(tracks)
        }

        @Throws(XmlPullParserException::class)
        private fun readTrack(parser: XmlPullParser): GpxTrack {
            var trackName = ""
            var trackDescription = ""
            var trackType = ""

            val trackPoints = readTag(parser, "trk") {
                when (it.name) {
                    "name" -> {
                        trackName = readText(it, "name")
                        null
                    }

                    "desc" -> {
                        trackDescription = readText(it, "desc")
                        null
                    }

                    "type" -> {
                        trackType = readText(it, "type")
                        null
                    }

                    "trkseg" -> readTrackSegment(it)

                    else -> {
                        skipTag(it)
                        null
                    }
                }
            }

            if (trackDescription.isNotBlank()) {
                trackName += "\n\n" + trackDescription
            }
            return GpxTrack(name = trackName, type = trackType, points = trackPoints.flatten())
        }

        @Throws(XmlPullParserException::class)
        private fun readTrackSegment(parser: XmlPullParser): List<GpxTrackPoint> {
            return readTag(parser, "trkseg") {
                when (it.name) {
                    "trkpt" -> {
                        val lat = it.getAttributeValue(null, "lat")
                        val long = it.getAttributeValue(null, "lon")
                        val coordinate = try {
                            Coordinate(lat.toDouble(), long.toDouble())
                        } catch (e: NumberFormatException) {
                            throw XmlPullParserException("Cannot parse coordinate (lat=$lat, lon=$long)")
                        }

                        val attributes = TrackPointAttributes()
                        readTag(it, "trkpt") { inner ->
                            when (inner.name) {
                                "time" -> {
                                    if (attributes.time != null) {
                                        throw XmlPullParserException("Track point contains multiple times")
                                    }
                                    attributes.time = readTime(inner)
                                }

                                "ele" -> {
                                    if (attributes.elevation != null) {
                                        throw XmlPullParserException("Track point contains multiple elevations")
                                    }
                                    attributes.elevation = readElevation(inner)
                                }

                                "extensions" -> {
                                    readTrackPointExtensions(parser, attributes)
                                }

                                else -> skipTag(inner)
                            }
                        }

                        GpxTrackPoint(
                            coordinate = coordinate,
                            time = attributes.time
                                ?: throw XmlPullParserException("Track point without required tag time"),
                            elevation = attributes.elevation,
                            heartFrequency = attributes.heartRate,
                            temperature = attributes.temperature
                        )
                    }

                    else -> {
                        skipTag(it)
                        null
                    }
                }
            }
        }

        @Throws(XmlPullParserException::class)
        private fun readTrackPointExtensions(
            parser: XmlPullParser,
            attributes: TrackPointAttributes
        ) {
            readTag(parser, "extensions") {
                when (it.name) {
                    TRACK_POINT_EXTENSIONS_GARMIN ->
                        readTrackPointExtensionsGarmin(parser, attributes)

                    else -> skipTag(parser)
                }
            }

        }

        @Throws(XmlPullParserException::class)
        private fun readTrackPointExtensionsGarmin(
            parser: XmlPullParser,
            attributes: TrackPointAttributes
        ) {
            readTag(parser, TRACK_POINT_EXTENSIONS_GARMIN) {
                when (it.name) {
                    EXTENSION_GARMIN_TEMPERATURE -> {
                        val temperatureString = readText(parser, EXTENSION_GARMIN_TEMPERATURE)
                        attributes.temperature = Temperature(
                            celsius = temperatureString.toFloatOrNull()
                                ?: throw XmlPullParserException("Invalid temperature: $temperatureString")
                        )
                    }

                    EXTENSION_GARMIN_HEART_RATE -> {
                        val heartRateString = readText(parser, EXTENSION_GARMIN_HEART_RATE)
                        attributes.heartRate = HeartFrequency(
                            bpm = heartRateString.toFloatOrNull()
                                ?: throw XmlPullParserException("Invalid heart rate: $heartRateString")
                        )
                    }

                    else -> skipTag(it)
                }
            }
        }

        @Throws(XmlPullParserException::class)
        private fun readTime(parser: XmlPullParser): Date {
            val dateString = readText(parser, "time")
            try {
                return Date.from(Instant.parse(dateString))
            } catch (e: DateTimeParseException) {
                throw XmlPullParserException("Invalid date string: $dateString")
            }
        }

        @Throws(XmlPullParserException::class)
        private fun readElevation(parser: XmlPullParser): Elevation {
            val elevationString = readText(parser, "ele")
            val elevation = elevationString.toFloatOrNull()
                ?: throw XmlPullParserException("Invalid elevation: $elevationString")
            return Elevation(meters = elevation)
        }

        @Throws(XmlPullParserException::class)
        private inline fun <T : Any> readTag(
            parser: XmlPullParser,
            tagName: String,
            handler: (XmlPullParser) -> T?
        ): List<T> {
            val result = mutableListOf<T>()

            parser.require(XmlPullParser.START_TAG, null, tagName)
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }

                val handlerResult = handler(parser)
                if (handlerResult != null) {
                    result.add(handlerResult)
                }
            }

            return result
        }

        @Throws(XmlPullParserException::class)
        private fun readText(parser: XmlPullParser, tag: String): String {
            parser.require(XmlPullParser.START_TAG, null, tag)
            val text = if (parser.next() == XmlPullParser.TEXT) {
                val result = parser.text
                parser.nextTag()
                result
            } else {
                ""
            }
            parser.require(XmlPullParser.END_TAG, null, tag)
            return text
        }

        @Throws(XmlPullParserException::class)
        private fun skipTag(parser: XmlPullParser) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                throw XmlPullParserException("Must be at start tag")
            }
            var depth = 1
            while (depth != 0) {
                when (parser.next()) {
                    XmlPullParser.END_TAG -> depth -= 1
                    XmlPullParser.START_TAG -> depth += 1
                }
            }
        }

    }

    private data class TrackPointAttributes(
        var elevation: Elevation? = null,
        var time: Date? = null,
        var temperature: Temperature? = null,
        var heartRate: HeartFrequency? = null
    )

}