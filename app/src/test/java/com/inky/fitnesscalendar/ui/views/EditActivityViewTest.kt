package com.inky.fitnesscalendar.ui.views

import android.os.Build
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.inky.fitnesscalendar.MainApp
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ContentColor
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.util.toDate
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime

@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@RunWith(RobolectricTestRunner::class)
class EditActivityViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * This tests that the save button can be clicked when the activity type does not have an end time,
     * even if the end time is invalid, because it is before the start time
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun test_can_save_activity_without_end_time() {
        var success = false

        val context = ApplicationProvider.getApplicationContext<MainApp>()

        val start = LocalDateTime.now()
        val end = start.minusDays(1)

        val richActivity = RichActivity(
            activity = Activity(
                typeId = 1,
                startTime = start.toDate(),
                endTime = end.toDate()
            ),
            type = ActivityType(
                uid = 1,
                hasDuration = false,
                activityCategory = ActivityCategory.Sports,
                name = "TestActivityType",
                emoji = ":)",
                color = ContentColor.Color1
            ),
            place = null,
        )
        composeTestRule.setContent {
            NewActivity(
                richActivity = richActivity,
                localizationRepository = LocalizationRepository(context),
                onSave = { success = true },
                onNavigateBack = {},
                onNavigateNewPlace = {},
                isTest = true
            )
        }

        // Initially, the confirm button should not exist, because no inputs have been made
        composeTestRule.onNodeWithTag("button-confirm").assertDoesNotExist()
        composeTestRule.onNodeWithTag("input-description").performTextInput("Foo")

        // The input should now be visible, because an input has been made
        composeTestRule.onNodeWithTag("button-confirm").assertIsEnabled().performClick()
        assertTrue("The save button should have been clicked", success)
    }
}