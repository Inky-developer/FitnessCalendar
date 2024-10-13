package com.inky.fitnesscalendar.util.gpx

import android.os.Build
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.approvaltests.Approvals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

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
        Approvals.verify(Json.encodeToString(reader?.tracks))
    }
}