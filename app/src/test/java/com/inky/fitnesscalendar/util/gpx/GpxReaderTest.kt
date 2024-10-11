package com.inky.fitnesscalendar.util.gpx

import android.os.Build
import com.inky.fitnesscalendar.data.gpx.Coordinate
import com.inky.fitnesscalendar.data.gpx.GpxTrackPoint
import com.inky.fitnesscalendar.data.measure.Elevation
import com.inky.fitnesscalendar.data.measure.Temperature
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.time.Instant
import java.util.Date

const val GPX = """
<gpx>
    <metadata></metadata>
    <trk>
        <name>Cool track</name>
        <desc>Was a nice track</desc>
        <type>TrackType</type>
        <trkseg>
            <trkpt lat="0.0" lon="0.0">
                <ele>0.0</ele>
                <time>2024-10-11T17:22:00.000Z</time>
                <extensions>
                    <ns3:TrackPointExtension>
                        <ns3:atemp>0.0</ns3:atemp>
                    </ns3:TrackPointExtension>
                </extensions>
            </trkpt>
            
            <trkpt lat="1.0" lon="0.0">
                <ele>10.0</ele>
                <time>2024-10-11T17:23:00.000Z</time>
                <extensions>
                    <ns3:TrackPointExtension>
                        <ns3:atemp>20.0</ns3:atemp>
                    </ns3:TrackPointExtension>
                </extensions>
            </trkpt>
            
            <trkpt lat="1.0" lon="-2.0">
                <ele>-50.0</ele>
                <time>2024-10-11T17:24:00.000Z</time>
                <extensions>
                    <ns3:TrackPointExtension>
                        <ns3:atemp>-10.0</ns3:atemp>
                    </ns3:TrackPointExtension>
                </extensions>
            </trkpt>
        </trkseg>
    </trk>
</gpx>
"""

@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@RunWith(RobolectricTestRunner::class)
class GpxReaderTest {
    @Test
    fun testParse() {
        val reader = GpxReader.read(ByteArrayInputStream(GPX.encodeToByteArray()))
        assertNotNull(reader)
        assertEquals(1, reader!!.tracks.size)

        val track = reader.tracks[0]
        assertEquals("Cool track\n\nWas a nice track", track.name)
        assertEquals("TrackType", track.type)

        val trackPoints = listOf(
            GpxTrackPoint(
                coordinate = Coordinate(latitude = 0.0, longitude = 0.0),
                time = Date.from(Instant.parse("2024-10-11T17:22:00.000Z")),
                elevation = Elevation(meters = 0f),
                heartFrequency = null,
                temperature = Temperature(celsius = 0f)
            ),
            GpxTrackPoint(
                coordinate = Coordinate(latitude = 1.0, longitude = 0.0),
                time = Date.from(Instant.parse("2024-10-11T17:23:00.000Z")),
                elevation = Elevation(meters = 10f),
                heartFrequency = null,
                temperature = Temperature(celsius = 20f)
            ),
            GpxTrackPoint(
                coordinate = Coordinate(latitude = 1.0, longitude = -2.0),
                time = Date.from(Instant.parse("2024-10-11T17:24:00.000Z")),
                elevation = Elevation(meters = -50f),
                heartFrequency = null,
                temperature = Temperature(celsius = -10f)
            ),
        )

        assertEquals(trackPoints, track.points)
    }
}