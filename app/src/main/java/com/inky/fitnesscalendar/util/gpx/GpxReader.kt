package com.inky.fitnesscalendar.util.gpx

import android.util.Log
import android.util.Xml
import com.inky.fitnesscalendar.data.gpx.Coordinate
import com.inky.fitnesscalendar.data.gpx.GpxTrack
import com.inky.fitnesscalendar.data.gpx.GpxTrackPoint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.InputStream
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Date


private const val TAG = "GpxReader"

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
            var trackType = ""

            val trackPoints = readTag(parser, "trk") {
                when (it.name) {
                    "name" -> {
                        trackName = readText(it, "name")
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

                        val times = readTag(it, "trkpt") { inner ->
                            when (inner.name) {
                                "time" -> readTime(inner)

                                else -> {
                                    skipTag(inner)
                                    null
                                }
                            }
                        }

                        if (times.isEmpty()) {
                            throw XmlPullParserException("Trackpoint without required tag time")
                        }

                        GpxTrackPoint(coordinate, times[0])
                    }

                    else -> {
                        skipTag(it)
                        null
                    }
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

}