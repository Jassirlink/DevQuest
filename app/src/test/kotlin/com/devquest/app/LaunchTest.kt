package com.devquest.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = QuestApplication::class)
class LaunchTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun productionGraphSeedsOnboardsAndPersistsCompletionThroughRecreation() {
        compose.waitUntil(20_000) {
            compose.onAllNodesWithText("Learn the foundations.").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Skip").performScrollTo().performClick()
        compose.onNodeWithText("Get Started").performScrollTo().performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Welcome, Explorer.").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Continue learning").performScrollTo().performClick()
        compose.onNodeWithText("Mark as completed").performScrollTo().performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Completed ✓").fetchSemanticsNodes().isNotEmpty()
        }
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Completed ✓").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
