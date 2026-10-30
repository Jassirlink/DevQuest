package com.devquest.feature.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.devquest.common.CalendarRules
import com.devquest.design.*
import com.devquest.domain.QuizEngine
import com.devquest.model.*
import java.time.LocalDate

@Composable
private fun Bars(values: List<Pair<String, Int>>, label: String) {
    val color = MaterialTheme.colorScheme.primary
    val description = values.joinToString { "${it.first}: ${it.second}" }
    Canvas(
        Modifier.fillMaxWidth().height(140.dp).semantics {
            contentDescription = "$label. $description"
        }
    ) {
        val max = (values.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
        val step = size.width / values.size.coerceAtLeast(1)
        values.forEachIndexed { i, v ->
            val h = size.height * v.second / max
            drawRoundRect(
                color,
                Offset(i * step + 4, size.height - h),
                Size((step - 8).coerceAtLeast(1f), h),
                CornerRadius(5f),
            )
        }
    }
    Text(description, style = MaterialTheme.typography.bodySmall)
}

@Composable
fun Progress(s: Snapshot) {
    val today = LocalDate.now()
    val dates = (6 downTo 0).map { today.minusDays(it.toLong()) }
    Page("Your momentum", "PROGRESS, EARNED") {
        Panel(
            "${s.xp.sumOf { it.amount }} XP",
            "${CalendarRules.streak(s.activities.map {LocalDate.parse(it.date)}.toSet(),today)} day streak • ${s.activities.map {it.date}.distinct().size} active days",
        ) {}
        if (s.activities.isEmpty())
            Text("No study time yet. Time is recorded while a lesson or practice screen is active.")
        Panel("Study minutes • last 7 days") {
            Bars(
                dates.map { d ->
                    d.dayOfMonth.toString() to
                        (s.activities.filter { it.date == d.toString() }.sumOf { it.seconds } / 60)
                },
                "Study minutes",
            )
        }
        Panel("XP • last 7 days") {
            Bars(
                dates.map { d ->
                    d.dayOfMonth.toString() to
                        s.xp.filter { it.date == d.toString() }.sumOf { it.amount }
                },
                "XP history",
            )
        }
        Section("Lessons completed by path")
        s.content.paths.forEach { p ->
            val chapters = s.content.chapters.filter { it.pathId == p.id }.map { it.id }
            val ids = s.content.lessons.filter { it.chapterId in chapters }.map { it.id }
            Text(
                "${p.title}: ${s.progress.count {it.completed && it.lessonId in ids}} / ${ids.size}"
            )
        }
        Panel(
            "Quiz accuracy",
            if (s.attempts.isEmpty()) "No attempts yet"
            else
                "${QuizEngine.accuracy(s.attempts.flatMap {it.answers})}% across ${s.attempts.sumOf {it.answers.size}} answers",
        ) {
            s.attempts
                .filter { it.finished }
                .takeLast(8)
                .forEach {
                    Text("${QuizEngine.accuracy(it.answers)}% • ${it.answers.size} questions")
                }
        }
        Section("Achievement collection")
        s.content.achievements.forEach {
            Panel((if (it.id in s.unlocked) "✦ " else "◇ ") + it.title, it.description)
        }
    }
}
