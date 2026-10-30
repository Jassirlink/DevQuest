package com.devquest.data

import android.content.Context
import androidx.room.withTransaction
import com.devquest.common.CalendarRules
import com.devquest.database.*
import com.devquest.datastore.PreferencesStore
import com.devquest.domain.*
import com.devquest.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class LocalRepository
@Inject
constructor(
    private val db: QuestDatabase,
    @ApplicationContext private val context: Context,
    private val clock: Clock,
) : QuestRepository {
    private val dao = db.dao()
    private val json = Json { ignoreUnknownKeys = true }
    override val snapshot: Flow<Snapshot> =
        db.invalidationTracker
            .createFlow(
                "paths",
                "chapters",
                "lessons",
                "questions",
                "challenges",
                "achievements",
                "lesson_progress",
                "bookmarks",
                "quiz_attempts",
                "quiz_answers",
                "coding_drafts",
                "interviews",
                "activities",
                "xp_events",
                "achievement_unlocks",
            )
            .map { read() }
            .flowOn(Dispatchers.IO)

    private suspend fun read(): Snapshot =
        db.withTransaction {
            val answers = dao.quiz_answers()
            Snapshot(
                Content(
                    dao.metadata()?.version ?: 1,
                    dao.paths().map { json.decodeFromString<LearningPath>(it.payload) },
                    dao.chapters().map { json.decodeFromString<Chapter>(it.payload) },
                    dao.lessons()
                        .map { json.decodeFromString<Lesson>(it.payload) }
                        .sortedBy { it.order },
                    dao.questions().map { json.decodeFromString<Question>(it.payload) },
                    dao.challenges().map { json.decodeFromString<CodingChallenge>(it.payload) },
                    dao.achievements().map { json.decodeFromString<Achievement>(it.payload) },
                ),
                dao.lesson_progress().map {
                    LessonProgress(it.lessonId, it.completed, it.position)
                },
                dao.bookmarks().map { it.lessonId }.toSet(),
                dao.quiz_attempts().map { a ->
                    QuizAttempt(
                        a.id,
                        json.decodeFromString(a.questionIds),
                        answers
                            .filter { it.attemptId == a.id }
                            .map { QuizAnswer(it.questionId, it.selected, it.correct) },
                        a.finished,
                    )
                },
                dao.coding_drafts().map { CodingDraft(it.challengeId, it.code, it.reviewed) },
                dao.interviews().map {
                    InterviewSession(it.id, it.questionId, it.confidence, it.seconds)
                },
                dao.activities().map { StudyActivity(it.id, it.date, it.seconds, it.category) },
                dao.xp_events().map { XpEvent(it.id, it.amount, it.date) },
                dao.achievement_unlocks().map { it.achievementId }.toSet(),
            )
        }

    override suspend fun seed() =
        withContext(Dispatchers.IO) {
            val content =
                context.assets.open("content.json").bufferedReader().use {
                    json.decodeFromString<Content>(it.readText())
                }
            require(content.questions.all { it.correct in it.options.indices })
            db.withTransaction {
                if ((dao.metadata()?.version ?: 0) < content.version) {
                    content.paths.forEach { dao.insert(PathRow(it.id, json.encodeToString(it))) }
                    content.chapters.forEach {
                        dao.insert(ChapterRow(it.id, it.pathId, json.encodeToString(it)))
                    }
                    content.lessons.forEach {
                        dao.insert(LessonRow(it.id, it.chapterId, json.encodeToString(it)))
                    }
                    content.questions.forEach {
                        dao.insert(QuestionRow(it.id, it.pathId, json.encodeToString(it)))
                    }
                    content.challenges.forEach {
                        dao.insert(ChallengeRow(it.id, json.encodeToString(it)))
                    }
                    content.achievements.forEach {
                        dao.insert(AchievementRow(it.id, json.encodeToString(it)))
                    }
                    dao.metadata(MetadataRow("content", content.version))
                }
            }
        }

    private suspend fun reward(key: String, amount: Int) {
        if (dao.insert(XpRow(key, amount, CalendarRules.today(clock))) != -1L)
            dao.insert(ActivityRow("reward:$key", CalendarRules.today(clock), 0, "completion"))
        val total = dao.xp_events().sumOf { it.amount }
        dao.achievements()
            .map { json.decodeFromString<Achievement>(it.payload) }
            .filter { total >= it.xpRequired }
            .forEach { dao.insert(UnlockRow(it.id)) }
    }

    override suspend fun completeLesson(id: String) {
        db.withTransaction {
            require(dao.lessons().any { it.id == id })
            val p = dao.lesson_progress().find { it.lessonId == id }
            dao.progress(ProgressRow(id, true, p?.position ?: 0))
            reward("lesson:$id", 20)
        }
    }

    override suspend fun readingPosition(id: String, position: Int) {
        db.withTransaction {
            val p = dao.lesson_progress().find { it.lessonId == id }
            dao.progress(ProgressRow(id, p?.completed ?: false, position.coerceAtLeast(0)))
        }
    }

    override suspend fun bookmark(id: String) {
        db.withTransaction {
            if (dao.bookmarks().any { it.lessonId == id }) dao.deleteBookmark(id)
            else dao.insert(BookmarkRow(id))
        }
    }

    override suspend fun startQuiz(ids: List<String>): String {
        val id = UUID.randomUUID().toString()
        db.withTransaction {
            require(
                ids.isNotEmpty() &&
                    ids.distinct().size == ids.size &&
                    ids.all { q -> dao.questions().any { it.id == q } }
            )
            dao.insert(AttemptRow(id, json.encodeToString(ids)))
        }
        return id
    }

    override suspend fun answer(attemptId: String, questionId: String, selected: Int) {
        db.withTransaction {
            val attempt = dao.quiz_attempts().first { it.id == attemptId }
            require(!attempt.finished)
            require(questionId in json.decodeFromString<List<String>>(attempt.questionIds))
            val question =
                json.decodeFromString<Question>(
                    dao.questions().first { it.id == questionId }.payload
                )
            val correct = QuizEngine.grade(question, selected)
            if (dao.insert(AnswerRow(attemptId, questionId, selected, correct)) != -1L)
                reward("question:$questionId", if (correct) 5 else 0)
        }
    }

    override suspend fun finishQuiz(id: String) {
        db.withTransaction {
            val a = dao.quiz_attempts().first { it.id == id }
            require(
                dao.quiz_answers().count { it.attemptId == id } ==
                    json.decodeFromString<List<String>>(a.questionIds).size
            )
            dao.finish(id)
        }
    }

    override suspend fun saveDraft(id: String, code: String, reviewed: Boolean) {
        db.withTransaction {
            val previous = dao.coding_drafts().find { it.challengeId == id }
            dao.draft(DraftRow(id, code, reviewed || previous?.reviewed == true))
            if (reviewed) reward("challenge:$id", 15)
        }
    }

    override suspend fun interview(id: String, questionId: String, confidence: Int, seconds: Int) {
        db.withTransaction {
            require(confidence in 1..5)
            if (
                dao.insert(InterviewRow(id, questionId, confidence, seconds.coerceAtLeast(0))) !=
                    -1L
            )
                reward("interview:$id", 15)
        }
    }

    override suspend fun study(id: String, seconds: Int, category: String) {
        if (seconds > 0)
            db.withTransaction {
                CalendarRules.buckets(clock.instant(), seconds, clock.zone).forEach {
                    (date, duration) ->
                    if (duration > 0) dao.insert(ActivityRow("$id:$date", date, duration, category))
                }
            }
    }

    override suspend fun reset() {
        db.withTransaction {
            dao.clear_quiz_answers()
            dao.clear_quiz_attempts()
            dao.clear_lesson_progress()
            dao.clear_bookmarks()
            dao.clear_coding_drafts()
            dao.clear_interviews()
            dao.clear_activities()
            dao.clear_xp_events()
            dao.clear_achievement_unlocks()
        }
    }
}

@Singleton
class LocalSettings @Inject constructor(@ApplicationContext context: Context) : SettingsRepository {
    private val store = PreferencesStore(context)
    override val preferences = store.flow

    override suspend fun update(preferences: Preferences) = store.update(preferences)
}
