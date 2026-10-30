package com.devquest

import com.devquest.domain.*
import com.devquest.model.*
import org.junit.Assert.*
import org.junit.Test

class RulesTest {
    private val q =
        Question("q", "kotlin", "single", "Foundation", "Answer?", listOf("A", "B"), 1, "B")

    @Test
    fun gradesWithoutRevealingUntilSubmission() {
        assertFalse(QuizEngine.grade(q, 0))
        assertTrue(QuizEngine.grade(q, 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidChoice() {
        QuizEngine.grade(q, 2)
    }

    @Test
    fun retryContainsOnlyMissed() {
        val a =
            QuizAttempt(
                "a",
                listOf("q", "r"),
                listOf(QuizAnswer("q", 0, false), QuizAnswer("r", 1, true)),
                true,
            )
        assertEquals(listOf("q"), QuizEngine.missed(a))
        assertEquals(50, QuizEngine.accuracy(a.answers))
    }

    @Test
    fun zeroProgressAndBounds() {
        assertEquals(0f, ProgressRules.completion(0, 0))
        assertEquals(1f, ProgressRules.completion(3, 2))
        assertEquals(3, ProgressRules.level(200))
    }
}
