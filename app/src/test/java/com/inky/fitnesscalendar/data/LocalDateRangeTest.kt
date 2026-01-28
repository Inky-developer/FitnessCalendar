package com.inky.fitnesscalendar.data

import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@RunWith(RobolectricTestRunner::class)
class LocalDateRangeTest {
    @Test
    fun smokeTest() {
        assertEquals(
            LocalDateRange(LocalDateTime.of(2025, 1, 1, 0, 0)..<LocalDateTime.of(2025, 1, 2, 0, 0)),
            LocalDateRange.dayOf(LocalDate.of(2025, 1, 1))
        )
        assertEquals(
            LocalDateRange(
                LocalDateTime.of(2024, 12, 30, 0, 0)..<LocalDateTime.of(
                    2025,
                    1,
                    6,
                    0,
                    0
                )
            ),
            LocalDateRange.weekOf(LocalDate.of(2025, 1, 1), locale = Locale.UK)
        )
        assertEquals(
            LocalDateRange(LocalDateTime.of(2025, 1, 1, 0, 0)..<LocalDateTime.of(2025, 2, 1, 0, 0)),
            LocalDateRange.monthOf(LocalDate.of(2025, 1, 1))
        )
        assertEquals(
            LocalDateRange(LocalDateTime.of(2025, 1, 1, 0, 0)..<LocalDateTime.of(2026, 1, 1, 0, 0)),
            LocalDateRange.yearOf(LocalDate.of(2025, 1, 1))
        )
    }
}