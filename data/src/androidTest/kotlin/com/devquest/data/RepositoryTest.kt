package com.devquest.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devquest.database.QuestDatabase
import java.time.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryTest {
    private lateinit var db: QuestDatabase
    private lateinit var repo: LocalRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, QuestDatabase::class.java).build()
        repo =
            LocalRepository(
                db,
                context,
                Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneId.of("Asia/Kolkata")),
            )
    }

    @After
    fun close() {
        db.close()
    }

    @Test
    fun seedIsIdempotentAndContentComplete() = runTest {
        repo.seed()
        repo.seed()
        val s = repo.snapshot.first()
        assertEquals(10, s.content.paths.size)
        assertTrue(s.content.lessons.size >= 24)
        assertTrue(s.content.questions.size >= 40)
        assertEquals(s.content.lessons.size, s.content.lessons.map { it.id }.distinct().size)
    }

    @Test
    fun concurrentCompletionRewardsOnlyOnceAndFlowsUpdate() = runTest {
        repo.seed()
        coroutineScope { repeat(8) { launch { repo.completeLesson("lesson-1") } } }
        val s = repo.snapshot.first { it.progress.any { p -> p.completed } }
        assertEquals(20, s.xp.sumOf { it.amount })
        assertEquals(1, s.progress.count { it.completed })
        assertTrue("spark" in s.unlocked)
    }

    @Test
    fun firstWrongAttemptNeverEarnsRetryXp() = runTest {
        repo.seed()
        val q = repo.snapshot.first().content.questions.first()
        val id = repo.startQuiz(listOf(q.id))
        repo.answer(id, q.id, (q.correct + 1) % q.options.size)
        repo.answer(id, q.id, q.correct)
        repo.finishQuiz(id)
        val retry = repo.startQuiz(listOf(q.id))
        repo.answer(retry, q.id, q.correct)
        repo.finishQuiz(retry)
        assertEquals(0, repo.snapshot.first().xp.sumOf { it.amount })
        assertEquals(2, repo.snapshot.first().attempts.size)
    }

    @Test
    fun correctAnswerRewardSurvivesRepositoryRecreation() = runTest {
        repo.seed()
        val q = repo.snapshot.first().content.questions.first()
        val id = repo.startQuiz(listOf(q.id))
        repo.answer(id, q.id, q.correct)
        val rebuilt =
            LocalRepository(
                db,
                InstrumentationRegistry.getInstrumentation().targetContext,
                Clock.systemUTC(),
            )
        rebuilt.answer(id, q.id, q.correct)
        assertEquals(5, rebuilt.snapshot.first().xp.sumOf { it.amount })
    }

    @Test
    fun bookmarksDraftsInterviewsAndReset() = runTest {
        repo.seed()
        repo.bookmark("lesson-1")
        repo.readingPosition("lesson-1", 200)
        repo.saveDraft("dedup", "fun x() = 1", true)
        repo.saveDraft("dedup", "fun x() = 2", true)
        repo.interview("session", "q1", 4, 60)
        repo.interview("session", "q1", 4, 60)
        val s = repo.snapshot.first()
        assertTrue("lesson-1" in s.bookmarks)
        assertEquals(200, s.progress.first().position)
        assertEquals("fun x() = 2", s.drafts.first().code)
        assertEquals(30, s.xp.sumOf { it.amount })
        repo.bookmark("lesson-1")
        assertTrue(repo.snapshot.first().bookmarks.isEmpty())
        repo.reset()
        val reset = repo.snapshot.first()
        assertTrue(reset.xp.isEmpty())
        assertTrue(reset.drafts.isEmpty())
        assertEquals(25, reset.content.lessons.size)
    }

    @Test
    fun studyLedgerIsIdempotent() = runTest {
        repo.seed()
        repo.study("s", 30, "lesson")
        repo.study("s", 30, "lesson")
        assertEquals(30, repo.snapshot.first().activities.sumOf { it.seconds })
    }

    @Test
    fun flowInvalidatesAfterCommit() = runTest {
        repo.seed()
        val emissions =
            kotlinx.coroutines.channels.Channel<com.devquest.model.Snapshot>(
                kotlinx.coroutines.channels.Channel.UNLIMITED
            )
        val job = backgroundScope.launch { repo.snapshot.collect { emissions.send(it) } }
        emissions.receive()
        repo.bookmark("lesson-2")
        val updated =
            withContext(Dispatchers.Default) {
                withTimeout(10_000) {
                    var next = emissions.receive()
                    while ("lesson-2" !in next.bookmarks) next = emissions.receive()
                    next
                }
            }
        assertTrue("lesson-2" in updated.bookmarks)
        job.cancel()
    }

    @Test
    fun preferencesPersistAcrossRepositoryInstances() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val first = LocalSettings(context)
        first.update(
            com.devquest.model.Preferences(
                nickname = "Ada",
                goal = 35,
                theme = "Light",
                reducedMotion = true,
                onboarded = true,
            )
        )
        val restored = LocalSettings(context).preferences.first()
        assertEquals("Ada", restored.nickname)
        assertEquals(35, restored.goal)
        assertTrue(restored.reducedMotion)
        assertTrue(restored.onboarded)
    }
}
