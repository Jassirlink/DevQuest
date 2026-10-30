package com.devquest.database

import androidx.room.*

@Entity(tableName = "paths") data class PathRow(@PrimaryKey val id: String, val payload: String)

@Entity(
    tableName = "chapters",
    foreignKeys =
        [
            ForeignKey(
                entity = PathRow::class,
                parentColumns = ["id"],
                childColumns = ["pathId"],
                onDelete = ForeignKey.RESTRICT,
            )
        ],
    indices = [Index("pathId")],
)
data class ChapterRow(@PrimaryKey val id: String, val pathId: String, val payload: String)

@Entity(
    tableName = "lessons",
    foreignKeys =
        [
            ForeignKey(
                entity = ChapterRow::class,
                parentColumns = ["id"],
                childColumns = ["chapterId"],
                onDelete = ForeignKey.RESTRICT,
            )
        ],
    indices = [Index("chapterId")],
)
data class LessonRow(@PrimaryKey val id: String, val chapterId: String, val payload: String)

@Entity(
    tableName = "questions",
    foreignKeys =
        [
            ForeignKey(
                entity = PathRow::class,
                parentColumns = ["id"],
                childColumns = ["pathId"],
                onDelete = ForeignKey.RESTRICT,
            )
        ],
    indices = [Index("pathId")],
)
data class QuestionRow(@PrimaryKey val id: String, val pathId: String, val payload: String)

@Entity(tableName = "challenges")
data class ChallengeRow(@PrimaryKey val id: String, val payload: String)

@Entity(tableName = "achievements")
data class AchievementRow(@PrimaryKey val id: String, val payload: String)

@Entity(tableName = "metadata")
data class MetadataRow(@PrimaryKey val id: String, val version: Int)

@Entity(
    tableName = "lesson_progress",
    foreignKeys =
        [ForeignKey(entity = LessonRow::class, parentColumns = ["id"], childColumns = ["lessonId"])],
)
data class ProgressRow(
    @PrimaryKey val lessonId: String,
    val completed: Boolean = false,
    val position: Int = 0,
)

@Entity(
    tableName = "bookmarks",
    foreignKeys =
        [ForeignKey(entity = LessonRow::class, parentColumns = ["id"], childColumns = ["lessonId"])],
)
data class BookmarkRow(@PrimaryKey val lessonId: String)

@Entity(tableName = "quiz_attempts")
data class AttemptRow(
    @PrimaryKey val id: String,
    val questionIds: String,
    val finished: Boolean = false,
)

@Entity(
    tableName = "quiz_answers",
    primaryKeys = ["attemptId", "questionId"],
    foreignKeys =
        [
            ForeignKey(
                entity = AttemptRow::class,
                parentColumns = ["id"],
                childColumns = ["attemptId"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = QuestionRow::class,
                parentColumns = ["id"],
                childColumns = ["questionId"],
            ),
        ],
    indices = [Index("questionId")],
)
data class AnswerRow(
    val attemptId: String,
    val questionId: String,
    val selected: Int,
    val correct: Boolean,
)

@Entity(
    tableName = "coding_drafts",
    foreignKeys =
        [
            ForeignKey(
                entity = ChallengeRow::class,
                parentColumns = ["id"],
                childColumns = ["challengeId"],
            )
        ],
)
data class DraftRow(@PrimaryKey val challengeId: String, val code: String, val reviewed: Boolean)

@Entity(
    tableName = "interviews",
    foreignKeys =
        [
            ForeignKey(
                entity = QuestionRow::class,
                parentColumns = ["id"],
                childColumns = ["questionId"],
            )
        ],
    indices = [Index("questionId")],
)
data class InterviewRow(
    @PrimaryKey val id: String,
    val questionId: String,
    val confidence: Int,
    val seconds: Int,
)

@Entity(tableName = "activities", indices = [Index("date")])
data class ActivityRow(
    @PrimaryKey val id: String,
    val date: String,
    val seconds: Int,
    val category: String,
)

@Entity(tableName = "xp_events", indices = [Index("date")])
data class XpRow(@PrimaryKey val id: String, val amount: Int, val date: String)

@Entity(
    tableName = "achievement_unlocks",
    foreignKeys =
        [
            ForeignKey(
                entity = AchievementRow::class,
                parentColumns = ["id"],
                childColumns = ["achievementId"],
            )
        ],
)
data class UnlockRow(@PrimaryKey val achievementId: String)

@Dao
abstract class QuestDao {
    @Query("SELECT * FROM paths") abstract suspend fun paths(): List<PathRow>

    @Upsert abstract suspend fun insert(row: PathRow)

    @Query("SELECT * FROM chapters") abstract suspend fun chapters(): List<ChapterRow>

    @Upsert abstract suspend fun insert(row: ChapterRow)

    @Query("SELECT * FROM lessons") abstract suspend fun lessons(): List<LessonRow>

    @Upsert abstract suspend fun insert(row: LessonRow)

    @Query("SELECT * FROM questions") abstract suspend fun questions(): List<QuestionRow>

    @Upsert abstract suspend fun insert(row: QuestionRow)

    @Query("SELECT * FROM challenges") abstract suspend fun challenges(): List<ChallengeRow>

    @Upsert abstract suspend fun insert(row: ChallengeRow)

    @Query("SELECT * FROM achievements") abstract suspend fun achievements(): List<AchievementRow>

    @Upsert abstract suspend fun insert(row: AchievementRow)

    @Query("SELECT * FROM lesson_progress")
    abstract suspend fun lesson_progress(): List<ProgressRow>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(row: ProgressRow): Long

    @Query("DELETE FROM lesson_progress") abstract suspend fun clear_lesson_progress()

    @Query("SELECT * FROM bookmarks") abstract suspend fun bookmarks(): List<BookmarkRow>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(row: BookmarkRow): Long

    @Query("DELETE FROM bookmarks") abstract suspend fun clear_bookmarks()

    @Query("SELECT * FROM quiz_attempts") abstract suspend fun quiz_attempts(): List<AttemptRow>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(row: AttemptRow): Long

    @Query("DELETE FROM quiz_attempts") abstract suspend fun clear_quiz_attempts()

    @Query("SELECT * FROM quiz_answers") abstract suspend fun quiz_answers(): List<AnswerRow>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(row: AnswerRow): Long

    @Query("DELETE FROM quiz_answers") abstract suspend fun clear_quiz_answers()

    @Query("SELECT * FROM coding_drafts") abstract suspend fun coding_drafts(): List<DraftRow>

    @Insert(onConflict = OnConflictStrategy.IGNORE) abstract suspend fun insert(row: DraftRow): Long

    @Query("DELETE FROM coding_drafts") abstract suspend fun clear_coding_drafts()

    @Query("SELECT * FROM interviews") abstract suspend fun interviews(): List<InterviewRow>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(row: InterviewRow): Long

    @Query("DELETE FROM interviews") abstract suspend fun clear_interviews()

    @Query("SELECT * FROM activities") abstract suspend fun activities(): List<ActivityRow>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(row: ActivityRow): Long

    @Query("DELETE FROM activities") abstract suspend fun clear_activities()

    @Query("SELECT * FROM xp_events") abstract suspend fun xp_events(): List<XpRow>

    @Insert(onConflict = OnConflictStrategy.IGNORE) abstract suspend fun insert(row: XpRow): Long

    @Query("DELETE FROM xp_events") abstract suspend fun clear_xp_events()

    @Query("SELECT * FROM achievement_unlocks")
    abstract suspend fun achievement_unlocks(): List<UnlockRow>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(row: UnlockRow): Long

    @Query("DELETE FROM achievement_unlocks") abstract suspend fun clear_achievement_unlocks()

    @Query("SELECT * FROM metadata WHERE id = 'content'")
    abstract suspend fun metadata(): MetadataRow?

    @Upsert abstract suspend fun metadata(row: MetadataRow)

    @Upsert abstract suspend fun progress(row: ProgressRow)

    @Upsert abstract suspend fun draft(row: DraftRow)

    @Query("DELETE FROM bookmarks WHERE lessonId = :id")
    abstract suspend fun deleteBookmark(id: String)

    @Query("UPDATE quiz_attempts SET finished = 1 WHERE id = :id")
    abstract suspend fun finish(id: String)
}

@Database(
    entities =
        [
            PathRow::class,
            ChapterRow::class,
            LessonRow::class,
            QuestionRow::class,
            ChallengeRow::class,
            AchievementRow::class,
            ProgressRow::class,
            BookmarkRow::class,
            AttemptRow::class,
            AnswerRow::class,
            DraftRow::class,
            InterviewRow::class,
            ActivityRow::class,
            XpRow::class,
            UnlockRow::class,
            MetadataRow::class,
        ],
    version = 1,
    exportSchema = true,
)
abstract class QuestDatabase : RoomDatabase() {
    abstract fun dao(): QuestDao
}
