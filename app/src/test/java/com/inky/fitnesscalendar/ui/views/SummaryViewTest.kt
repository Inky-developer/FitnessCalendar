package com.inky.fitnesscalendar.ui.views

import android.os.Build
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.test.core.app.ApplicationProvider
import com.inky.fitnesscalendar.MainApp
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.testUtils.TestApp
import com.inky.fitnesscalendar.testUtils.mockDatabaseRepository
import com.inky.fitnesscalendar.view_model.BaseViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@RunWith(RobolectricTestRunner::class)
class SummaryViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `test empty summary view does not crash`() {
        val context = ApplicationProvider.getApplicationContext<MainApp>()

        val databaseRepository = mockDatabaseRepository(context)
        val viewModel = BaseViewModel(
            context = context,
            repository = databaseRepository
        )

        composeTestRule.setContent {
            TestApp {
                SummaryView(
                    viewModel = viewModel,
                    filter = ActivityFilter(),
                    onBack = {},
                    onNavigateFilter = {},
                    onEditFilter = {})
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.waitUntilExactlyOneExists(hasText("No activities yet"))
        println(composeTestRule.onRoot().printToString())
    }
}