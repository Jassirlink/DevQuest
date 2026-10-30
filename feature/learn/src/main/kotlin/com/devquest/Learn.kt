package com.devquest.feature.learn

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.devquest.design.*
import com.devquest.model.*

@Composable
fun Learn(s: Snapshot, pathId: String?, path: (String) -> Unit, lesson: (String) -> Unit) {
    val selected = s.content.paths.find { it.id == pathId }
    Page(selected?.title ?: "Choose your path", "LEARNING ATLAS") {
        if (selected == null)
            AdaptiveCards(s.content.paths) { p ->
                val chapters = s.content.chapters.filter { it.pathId == p.id }.map { it.id }
                val lessons = s.content.lessons.filter { it.chapterId in chapters }
                val done =
                    s.progress.count { it.completed && it.lessonId in lessons.map { l -> l.id } }
                val answers =
                    s.attempts
                        .flatMap { it.answers }
                        .filter { a ->
                            s.content.questions.any { it.id == a.questionId && it.pathId == p.id }
                        }
                Panel(p.title, p.description, onClick = { path(p.id) }) {
                    Text("$done / ${lessons.size} lessons • ${lessons.sumOf { it.minutes }} min")
                    Text(
                        "Quiz accuracy: " +
                            if (answers.isEmpty()) "Not practiced"
                            else "${answers.count { it.correct }*100/answers.size}%"
                    )
                    Text(p.tags.joinToString(" · "))
                }
            }
        else {
            Text(selected.description)
            s.content.chapters
                .filter { it.pathId == selected.id }
                .forEach { chapter ->
                    Section(chapter.title)
                    s.content.lessons
                        .filter { it.chapterId == chapter.id }
                        .forEach { l ->
                            Panel(
                                l.title,
                                "${l.minutes} min • " +
                                    if (s.progress.any { it.lessonId == l.id && it.completed })
                                        "Completed"
                                    else "Ready to learn",
                                onClick = { lesson(l.id) },
                            ) {}
                        }
                }
        }
    }
}
