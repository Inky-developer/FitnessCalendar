package com.inky.fitnesscalendar.util.gpx

import androidx.test.core.app.ApplicationProvider
import com.inky.fitnesscalendar.MainApp
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.Track
import com.inky.fitnesscalendar.testUtils.SportActivityType
import org.approvaltests.Approvals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.time.Instant
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class GpxRoundtripTest {
    @Test
    fun testRoundtrip() {
        val context = ApplicationProvider.getApplicationContext<MainApp>()

        val tracks = GpxReader.read(ByteArrayInputStream(GPX.encodeToByteArray()))?.tracks
        requireNotNull(tracks)

        val gpxTrack = tracks[0]
        val track = Track(uid = 1, activityId = 1, points = gpxTrack.points)
        val activity = Activity(uid = 1, typeId = 1, startTime = Date.from(Instant.now()))
        val richActivity = RichActivity(activity, SportActivityType, null)

        val writer = StringWriter()
        GpxWriter.write(richActivity, track, context, writer)

        val newTracks =
            GpxReader.read(ByteArrayInputStream(writer.toString().encodeToByteArray()))?.tracks
        requireNotNull(newTracks)

        val newGpxTrack = newTracks[0]
        assertEquals(gpxTrack.points, newGpxTrack.points)

        Approvals.verifyAll(
            "gpx tracks names (old and new)",
            arrayOf(gpxTrack.name, newGpxTrack.name)
        )
    }
}