package com.inky.fitnesscalendar.util.gpx

import android.content.Context
import android.util.Xml
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.Track
import org.xmlpull.v1.XmlSerializer
import java.io.Writer
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class GpxWriter {
    companion object {
        fun write(activity: RichActivity, track: Track, context: Context, writer: Writer) {
            val serializer = Serializer(writer)
            return serializer.document {
                tag("gpx") {
                    attribute("creator", context.getString(R.string.app_name))
                    attribute("version", "1.1")
                    attribute("xmlns", "http://www.topografix.com/GPX/1/1")

                    tag("trk") {
                        writeTrack(activity, track)
                    }
                }
            }
        }
    }
}

private fun TagScope.writeTrack(activity: RichActivity, track: Track) {
    tag("name") { text(activity.type.name) }
    tag("desc") { text(activity.activity.description) }
    tag("type") { text(activity.type.name) }
    tag("trkseg") {
        for (point in track.points) {
            tag("trkpt") {
                attribute("lat", point.coordinate.latitude.toString())
                attribute("lon", point.coordinate.longitude.toString())

                tag("time") {
                    text(
                        point.time.toInstant().atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    )
                }

                if (point.elevation != null) {
                    tag("ele") { text(point.elevation.meters.toString()) }
                }

                // TODO: Include other attributes like temperature and heart rate
            }
        }
    }
}

private class Serializer(writer: Writer) {
    private val serializer: XmlSerializer = Xml.newSerializer().apply {
        setOutput(writer)
    }

    inline fun document(inner: DocumentScope.() -> Unit) {
        val scope = DocumentScope(serializer)
        serializer.startDocument("UTF-8", true)
        inner(scope)
        serializer.endDocument()
    }
}

private open class DocumentScope(val serializer: XmlSerializer) {
    inline fun tag(name: String, inner: TagScope.() -> Unit) {
        val scope = TagScope(serializer)
        serializer.startTag(null, name)
        inner(scope)
        serializer.endTag(null, name)
    }
}

private class TagScope(serializer: XmlSerializer) : DocumentScope(serializer) {
    fun attribute(name: String, value: String) {
        serializer.attribute(null, name, value)
    }

    fun text(text: String) {
        serializer.text(text)
    }
}