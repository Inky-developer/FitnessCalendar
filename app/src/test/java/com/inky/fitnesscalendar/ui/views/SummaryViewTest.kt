package com.inky.fitnesscalendar.ui.views

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.testUtils.TestApp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class SummaryViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `test empty summary view does not crash`() {
        composeTestRule.setContent {
            TestApp {
                SummaryView(
                    filter = ActivityFilter(),
                    onBack = {},
                    onNavigateFilter = {},
                    onEditFilter = {},
                    onNavigateActivity = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.waitUntilExactlyOneExists(hasText("No activities yet"))
        println(composeTestRule.onRoot().printToString())
    }
}