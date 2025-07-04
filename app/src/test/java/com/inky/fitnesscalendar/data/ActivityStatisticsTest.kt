package com.inky.fitnesscalendar.data

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.inky.fitnesscalendar.MainApp
import com.inky.fitnesscalendar.data.measure.VerticalDistance
import com.inky.fitnesscalendar.data.measure.bpm
import com.inky.fitnesscalendar.data.measure.kilometers
import com.inky.fitnesscalendar.testUtils.mockSportsActivity
import com.inky.fitnesscalendar.view_model.summary_view.SummaryBoxState
import org.approvaltests.Approvals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@RunWith(RobolectricTestRunner::class)
class ActivityStatisticsTest {
    @Test
    fun smokeTest() {
        val context = ApplicationProvider.getApplicationContext<MainApp>()

        val activities = listOf(
            mockSportsActivity(
                durationSeconds = 1000,
                distance = 5.0.kilometers(),
                verticalDistance = VerticalDistance(meters = 0.0),
                averageHeartRate = 200.0.bpm(),
                description = "Activity 1"
            ),
            mockSportsActivity(description = "Activity 2")
        )

        val statistics = ActivityStatistics(activities)
        val summaryBoxState = SummaryBoxState(context, statistics)

        Approvals.verify(summaryBoxState)
    }
}