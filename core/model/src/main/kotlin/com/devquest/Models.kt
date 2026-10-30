package com.devquest.model

import kotlinx.serialization.Serializable

@Serializable
data class LearningPath(
    val id: String,
    val title: String,
    val description: String,
    val tags: List<String>,
)

@Serializable
data class Chapter(val id: String, val pathId: String, val title: String, val order: Int)

@Serializable data class Block(val kind: String, val text: String)

@Serializable
data class Lesson(
    val id: String,
    val chapterId: String,
    val title: String,
    val minutes: Int,
    val order: Int,
    val blocks: List<Block>,
)

@Serializable
data class Question(
    val id: String,
    val pathId: String,
    val kind: String,
    val difficulty: String,
    val prompt: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String,
)

@Serializable
data class CodingChallenge(
    val id: String,
    val title: String,
    val difficulty: String,
    val description: String,
    val constraints: String,
    val example: String,
    val hints: List<String>,
    val solution: String,
)

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val xpRequired: Int,
)

@Serializable
data class Content(
    val version: Int = 1,
    val paths: List<LearningPath> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val lessons: List<Lesson> = emptyList(),
    val questions: List<Question> = emptyList(),
    val challenges: List<CodingChallenge> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
)

data class LessonProgress(val lessonId: String, val completed: Boolean, val position: Int)

data class QuizAnswer(val questionId: String, val selected: Int, val correct: Boolean)

data class QuizAttempt(
    val id: String,
    val questionIds: List<String>,
    val answers: List<QuizAnswer>,
    val finished: Boolean,
)

data class CodingDraft(val challengeId: String, val code: String, val reviewed: Boolean)

data class InterviewSession(
    val id: String,
    val questionId: String,
    val confidence: Int,
    val seconds: Int,
)

data class StudyActivity(val id: String, val date: String, val seconds: Int, val category: String)

data class XpEvent(val id: String, val amount: Int, val date: String)

data class Snapshot(
    val content: Content = Content(),
    val progress: List<LessonProgress> = emptyList(),
    val bookmarks: Set<String> = emptySet(),
    val attempts: List<QuizAttempt> = emptyList(),
    val drafts: List<CodingDraft> = emptyList(),
    val interviews: List<InterviewSession> = emptyList(),
    val activities: List<StudyActivity> = emptyList(),
    val xp: List<XpEvent> = emptyList(),
    val unlocked: Set<String> = emptySet(),
)

data class Preferences(
    val nickname: String = "Explorer",
    val goal: Int = 20,
    val theme: String = "Dark",
    val reducedMotion: Boolean = false,
    val onboarded: Boolean = false,
    val goals: Set<String> = emptySet(),
)

data class SceneNode(val id: String, val title: String, val description: String)

data class Scene(
    val title: String,
    val nodes: List<SceneNode>,
    val edges: List<Pair<String, String>>,
)

object Scenes {
    val all =
        listOf(
            Scene(
                "Clean Architecture",
                listOf(
                    SceneNode(
                        "ui",
                        "Presentation",
                        "Renders immutable UI state and sends user actions. Depends on domain contracts.",
                    ),
                    SceneNode(
                        "domain",
                        "Domain",
                        "Pure Kotlin rules and repository contracts. No Android dependencies.",
                    ),
                    SceneNode(
                        "data",
                        "Data",
                        "Implements domain contracts with Room and local assets. Dependencies point inward.",
                    ),
                ),
                listOf("ui" to "domain", "data" to "domain"),
            ),
            Scene(
                "MVVM State Pipeline",
                listOf(
                    SceneNode("repo", "Repository", "Observes the source of truth as Flow."),
                    SceneNode("vm", "ViewModel", "Maps domain data into immutable StateFlow."),
                    SceneNode(
                        "compose",
                        "Compose UI",
                        "Collects state with lifecycle awareness; sends actions back.",
                    ),
                ),
                listOf("repo" to "vm", "vm" to "compose"),
            ),
            Scene(
                "Coroutine Timeline",
                listOf(
                    SceneNode("launch", "Launch", "Creates a child Job in its parent scope."),
                    SceneNode(
                        "suspend",
                        "Suspend",
                        "Releases the thread while waiting; another task can run.",
                    ),
                    SceneNode("resume", "Resume", "Continues after suspension on its dispatcher."),
                    SceneNode(
                        "cancel",
                        "Cancel",
                        "Cancellation is cooperative; cleanup belongs in finally.",
                    ),
                ),
                listOf("launch" to "suspend", "suspend" to "resume", "resume" to "cancel"),
            ),
            Scene(
                "Compose State",
                listOf(
                    SceneNode("state", "State", "A snapshot state write invalidates readers."),
                    SceneNode("scope", "Recompose", "Compose re-executes affected restart scopes."),
                    SceneNode(
                        "draw",
                        "Render",
                        "Layout or drawing runs only as needed. Stable inputs enable skipping.",
                    ),
                ),
                listOf("state" to "scope", "scope" to "draw"),
            ),
        )
}
