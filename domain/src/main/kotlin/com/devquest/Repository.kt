package com.devquest.domain

import com.devquest.model.*
import kotlinx.coroutines.flow.Flow

interface QuestRepository {
    val snapshot: Flow<Snapshot>

    suspend fun seed()

    suspend fun completeLesson(id: String)

    suspend fun readingPosition(id: String, position: Int)

    suspend fun bookmark(id: String)

    suspend fun startQuiz(ids: List<String>): String

    suspend fun answer(attemptId: String, questionId: String, selected: Int)

    suspend fun finishQuiz(id: String)

    suspend fun saveDraft(id: String, code: String, reviewed: Boolean)

    suspend fun interview(id: String, questionId: String, confidence: Int, seconds: Int)

    suspend fun study(id: String, seconds: Int, category: String)

    suspend fun reset()
}

interface SettingsRepository {
    val preferences: Flow<Preferences>

    suspend fun update(preferences: Preferences)
}

object QuizEngine {
    fun grade(question: Question, selected: Int): Boolean {
        require(selected in question.options.indices)
        return selected == question.correct
    }

    fun missed(attempt: QuizAttempt): List<String> =
        attempt.answers.filterNot { it.correct }.map { it.questionId }

    fun accuracy(answers: List<QuizAnswer>): Int =
        if (answers.isEmpty()) 0 else answers.count { it.correct } * 100 / answers.size
}

class CompleteLesson(private val repository: QuestRepository) {
    suspend operator fun invoke(id: String) = repository.completeLesson(id)
}

object ProgressRules {
    fun completion(done: Int, total: Int): Float =
        if (total == 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f)

    fun level(xp: Int) = xp.coerceAtLeast(0) / 100 + 1
}
