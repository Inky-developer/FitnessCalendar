package tools.fastlane

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inky.fitnesscalendar.MainActivity
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.cleanstatusbar.BluetoothState
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.cleanstatusbar.MobileDataType


@RunWith(AndroidJUnit4::class)
class FalconScreenshots {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testTakeScreenshot() {
        composeTestRule.onRoot().assertExists()
        Screengrab.screenshot("initial_view")

        composeTestRule.onNodeWithTag("Today").assertExists().performClick()
        composeTestRule.waitUntil {
            composeTestRule.onNode(hasText("No notes on that day")).isDisplayed()
        }
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil {
            composeTestRule.onNode(hasText("No notes on that day")).isDisplayed()
        }
        composeTestRule.waitForIdle()
        Screengrab.screenshot("day_view")

        composeTestRule.onNodeWithContentDescription("Menu").assertExists().performClick()
        composeTestRule.onNodeWithText("Activity Log").assertExists().performClick()
        composeTestRule.waitUntil {
            composeTestRule.onAllNodes(hasTestTag("ActivityCard")).onFirst().isDisplayed()
        }
        composeTestRule.waitUntilDoesNotExist(hasText("No activities yet", substring = true))
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil {
            composeTestRule.onAllNodes(hasTestTag("ActivityCard")).onLast().isDisplayed()
        }
        composeTestRule.waitUntilDoesNotExist(hasText("No activities yet", substring = true))
        composeTestRule.waitForIdle()
        Screengrab.screenshot("activity_log")

        composeTestRule.onNodeWithContentDescription("Menu").assertExists().performClick()
        composeTestRule.onNodeWithText("Statistics").assertExists().performClick()
        composeTestRule.onNode(hasText("Days") and hasTestTag("DateFilterChip")).assertExists()
            .performClick()
        composeTestRule.waitForIdle()
        Screengrab.screenshot("statistics")
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun beforeAll() {
            Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

            CleanStatusBar().apply {
                setMobileNetworkDataType(MobileDataType.LTE)
                setBluetoothState(BluetoothState.DISCONNECTED)
            }.enable()
        }

        @JvmStatic
        @AfterClass
        fun afterAll() {
            CleanStatusBar.disable()
        }
    }
}