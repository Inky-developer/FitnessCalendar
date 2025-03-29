package com.inky.fitnesscalendar.ui.views

import android.os.Build
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.inky.fitnesscalendar.MainApp
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.repository.ImportRepository
import com.inky.fitnesscalendar.testUtils.mockDatabaseRepository
import com.inky.fitnesscalendar.ui.ImportView
import com.inky.fitnesscalendar.ui.util.ProvideDatabaseValues
import com.inky.fitnesscalendar.view_model.ImportViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.approvaltests.Approvals
import org.approvaltests.utils.WithTimeZone
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog


@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@RunWith(RobolectricTestRunner::class)
class ImportViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Test the import workflow:
     * - Load some tracks
     * - Go through each track and select an activity type (here biking)
     * - Verify that import errors are displayed
     * - Verify that the tracks were imported
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun test_can_import_activities() {
        val context = ApplicationProvider.getApplicationContext<MainApp>()

        val databaseRepository = mockDatabaseRepository(context)
        val viewModel = ImportViewModel(
            context = context,
            dbRepository = databaseRepository,
            importRepository = ImportRepository(databaseRepository)
        )

        val testFileNames = listOf(
            TestFile("/gpx/valid_track.gpx", false),
            TestFile("/gpx/invalid_track_no_track_points.gpx", true)
        )
        val testFiles = testFileNames.map { testFile ->
            ImportViewTest::class.java.getResource(testFile.path)!!.openStream()
                .use { it.readBytes().toString(Charsets.UTF_8) }
        }

        runBlocking {
            viewModel.loadTestFiles(testFiles)
        }

        assertEquals("Both tracks should have been imported", 2, viewModel.tracks.value.size)

        composeTestRule.setContent {
            ProvideDatabaseValues(databaseRepository) {
                ImportView(viewModel)
            }
        }

        ShadowLog.stream = System.out

        composeTestRule.apply {
            waitUntilExactlyOneExists(hasTestTag("import_list"))

            val listItems = onAllNodesWithTag("import_list_item").assertCountEquals(2)
            for ((index, _) in listItems.fetchSemanticsNodes().withIndex()) {
                val node = listItems[index]

                // Select the activity type for biking for this activity
                node.performClick()
                onNodeWithText("🚴").performClick()
                onNodeWithText("Save").performClick()
                waitForIdle()

                if (testFileNames[index].isError) {
                    node.assertTextContains("An error occurred.", substring = true)
                } else {
                    node.assertTextContains("🚴", substring = true)
                }
            }

            runBlocking {
                assertEquals(
                    "Initially, there should be no activities",
                    0,
                    databaseRepository.getActivities(ActivityFilter()).first().size
                )
            }

            onNodeWithText("Import").performClick()

            waitUntil(timeoutMillis = 5000) {
                runBlocking {
                    databaseRepository.getActivities(ActivityFilter()).first().isNotEmpty()
                }
            }
        }
        runBlocking {
            WithTimeZone("UTC").use {
                Approvals.verifyAll(
                    databaseRepository.getActivities(ActivityFilter()).first().toTypedArray()
                ) { it.toString() }
            }
        }

    }

    data class TestFile(val path: String, val isError: Boolean)
}