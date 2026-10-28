package com.inky.fitnesscalendar.db.entities

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class StepsTest {
    @Test
    fun `test that a day consists of a whole number of frames`() {
        @Suppress("SimplifyBooleanWithConstants")
        assert(Steps.SECONDS_PER_DAY % Steps.SECONDS_PER_FRAME == 0)
    }

    @Test
    fun `test that the first moment of a day gets frame 0`() {
        val date = LocalDate.now()
        val steps = Steps(dateTime = date.atStartOfDay(), steps = 0)
        assertEquals(steps.frame, 0)
        assertEquals(steps.day.day, date.toEpochDay())
    }

    @Test
    fun `test that the last moment of a day gets frame NUMBER_OF_FRAMES - 1`() {
        val date = LocalDate.now()
        val steps = Steps(dateTime = date.atTime(LocalTime.MAX), steps = 0)
        assertEquals(steps.frame, Steps.FRAMES_PER_DAY - 1)
        assertEquals(steps.day.day, date.toEpochDay())
    }
}