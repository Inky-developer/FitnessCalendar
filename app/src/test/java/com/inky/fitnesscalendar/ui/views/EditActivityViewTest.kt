package com.inky.fitnesscalendar.ui.views

import android.os.Build
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.inky.fitnesscalendar.MainApp
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ContentColor
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.testUtils.MockApplication
import com.inky.fitnesscalendar.util.toDate
import com.inky.fitnesscalendar.util.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalTestApi::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@RunWith(RobolectricTestRunner::class)
class EditActivityViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * This tests that the save button can be clicked when the activity type does not have an end time,
     * even if the end time is invalid, because it is before the start time
     */
    @Test
    fun test_can_save_activity_without_end_time() {
        var success = false

        val context = ApplicationProvider.getApplicationContext<MainApp>()

        val start = LocalDateTime.now()
        val end = start.minusDays(1)

        val richActivity = RichActivity(
            activity = Activity(
                uid = 420,
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
                color = ContentColor.Color1,
                archived = false
            ),
            place = null,
            images = emptyList()
        )
        composeTestRule.setContent {
            MockApplication {
                NewActivity(
                    richActivity = richActivity,
                    onSave = { success = true },
                    onNavigateBack = {},
                    onNavigateNewPlace = {},
                    isTest = true
                )
            }
        }

        composeTestRule.waitUntilExactlyOneExists(hasTestTag("input-description"))
        // Initially, the confirm button should not exist, because no inputs have been made
        composeTestRule.onNodeWithTag("button-confirm").assertDoesNotExist()
        composeTestRule.onNodeWithTag("input-description").performTextInput("Foo")

        // The input should now be visible, because an input has been made
        composeTestRule.onNodeWithTag("button-confirm").assertIsEnabled().performClick()
        assertTrue("The save button should have been clicked", success)
    }

    /**
     * Tests that creating an activity with an initial day set works correctly
     */
    @Test
    fun test_initial_day_works() {
        var hasBeenClicked = false

        val start = LocalDateTime.of(2024, 11, 1, 23, 30)
        val end = start.plusHours(1)

        val richActivity = RichActivity(
            activity = Activity(
                uid = 69,
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
                color = ContentColor.Color1,
                archived = false
            ),
            place = null,
            images = emptyList()
        )
        composeTestRule.setContent {
            MockApplication {
                NewActivity(
                    richActivity = richActivity,
                    onSave = {
                        assertEquals(
                            LocalDateTime.of(2024, 12, 31, 23, 30),
                            it.activity.startTime.toLocalDateTime(),
                        )
                        assertEquals(
                            LocalDateTime.of(2025, 1, 1, 0, 30),
                            it.activity.endTime.toLocalDateTime()
                        )
                        hasBeenClicked = true
                    },
                    onNavigateBack = {},
                    onNavigateNewPlace = {},
                    isTest = true,
                    initialDay = EpochDay(day = LocalDate.of(2024, 12, 31).toEpochDay())
                )
            }
        }

        composeTestRule.waitUntilExactlyOneExists(hasTestTag("input-description"))
        // Initially, the confirm button should not exist, because no inputs have been made
        composeTestRule.onNodeWithTag("button-confirm").assertDoesNotExist()
        composeTestRule.onNodeWithTag("input-description").performTextInput("Foo")

        // The input should now be visible, because an input has been made
        composeTestRule.onNodeWithTag("button-confirm").assertIsEnabled().performClick()
        assertTrue("The save button should have been clicked", hasBeenClicked)
    }
}