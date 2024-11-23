package com.inky.fitnesscalendar.ui.views

import android.os.Build
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.inky.fitnesscalendar.MainApp
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.testUtils.MockDatabaseValues
import com.inky.fitnesscalendar.testUtils.mockActivityTypes
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@RunWith(RobolectricTestRunner::class)
class RecordActivityDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Test that the record activity composable works and allows creating a recording
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun test_can_create_recording() {
        var success = false

        val context = ApplicationProvider.getApplicationContext<MainApp>()

        composeTestRule.setContent {
            MockDatabaseValues {
                RecordActivity(
                    onStart = {
                        val expectedActivityType = mockActivityTypes.find { it.name == "Sports" }
                        assertEquals(expectedActivityType, it.type)
                        success = true
                    },
                    localizationRepository = LocalizationRepository(context),
                    onNavigateBack = {}
                )
            }
        }

        // Initially, the confirm button should not be enabled, because no activity type is selected
        composeTestRule.onNodeWithTag("button-confirm").assertIsNotEnabled()

        // Select the sports activity type
        composeTestRule.onNodeWithText("S").performClick()

        // Now the button should be enabled
        composeTestRule.onNodeWithTag("button-confirm").assertIsEnabled()
        composeTestRule.onNodeWithTag("button-confirm").performClick()

        assertTrue(success)
    }
}