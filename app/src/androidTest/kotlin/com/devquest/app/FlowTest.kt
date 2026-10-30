package com.devquest.app

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.devquest.design.QuestTheme
import com.devquest.feature.onboarding.Onboarding
import com.devquest.feature.practice.Quiz
import com.devquest.model.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class FlowTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun onboardingReachesGoalSelection() {
        var result: Preferences? = null
        compose.setContent {
            QuestTheme { Onboarding(Preferences(reducedMotion = true)) { result = it } }
        }
        compose.onNodeWithText("Skip").performScrollTo().performClick()
        compose.onNodeWithText("Get Started").performScrollTo().performClick()
        compose.runOnIdle {
            assertTrue(result!!.onboarded)
            assertTrue(result!!.goals.isNotEmpty())
        }
    }

    @Test
    fun quizHidesAnswerThenShowsFeedback() {
        val q =
            Question(
                "q",
                "kotlin",
                "single",
                "Foundation",
                "Which answer?",
                listOf("Choice A", "Choice B"),
                1,
                "Explanation after submit",
            )
        var snapshot by
            mutableStateOf(
                Snapshot(
                    content = Content(questions = listOf(q)),
                    attempts = listOf(QuizAttempt("a", listOf("q"), emptyList(), false)),
                )
            )
        compose.setContent {
            QuestTheme {
                Quiz(
                    snapshot,
                    "a",
                    { id, selected ->
                        snapshot =
                            snapshot.copy(
                                attempts =
                                    listOf(
                                        snapshot.attempts
                                            .first()
                                            .copy(
                                                answers =
                                                    listOf(QuizAnswer(id, selected, selected == 1))
                                            )
                                    )
                            )
                    },
                    {
                        snapshot =
                            snapshot.copy(
                                attempts = listOf(snapshot.attempts.first().copy(finished = true))
                            )
                    },
                    {},
                )
            }
        }
        compose.onNodeWithText("Explanation after submit").assertDoesNotExist()
        compose.onNodeWithText("○ Choice B").performClick()
        compose.onNodeWithText("Submit answer").performScrollTo().performClick()
        compose.onNodeWithText("Explanation after submit").assertExists()
        compose.onNodeWithText("See results").performScrollTo().performClick()
        compose.onNodeWithText("Session complete").assertExists()
    }

    @Test
    fun homeNavigatesToActualLesson() {
        val lesson = Lesson("l", "c", "A real lesson", 5, 0, listOf(Block("text", "Read")))
        val state =
            UiState(
                loading = false,
                data = Snapshot(content = Content(lessons = listOf(lesson))),
                preferences = Preferences(onboarded = true, reducedMotion = true),
            )
        compose.setContent { QuestTheme { QuestApp(state, {}) } }
        compose.onNodeWithText("Continue learning").performScrollTo().performClick()
        compose.onNodeWithText("FIELD NOTES • 5 MIN").assertExists()
        compose.onNodeWithText("Read").assertExists()
    }
}
