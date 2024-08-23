package com.inky.fitnesscalendar.db.entities

import com.inky.fitnesscalendar.data.gpx.Coordinate
import com.inky.fitnesscalendar.util.gpx.simplify
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackTest {

    @Test
    fun testSimplify() {
        val trackPoints = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(1.0, 1.0),
            Coordinate(2.0, -1.0),
            Coordinate(3.0, -1.5)
        )
        val simplifiedTrack = simplify(trackPoints, maxNumPoints = 3)

        assertEquals(
            listOf(
                Coordinate(0.0, 0.0),
                Coordinate(1.0, 1.0),
                Coordinate(3.0, -1.5)
            ),
            simplifiedTrack,
        )
    }
}