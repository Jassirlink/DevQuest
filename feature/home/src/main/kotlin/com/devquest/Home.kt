package com.devquest.feature.home

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.devquest.common.CalendarRules
import com.devquest.design.*
import com.devquest.domain.ProgressRules
import com.devquest.model.*
import java.time.LocalDate

@Composable
fun Home(s: Snapshot, p: Preferences, open: (String) -> Unit, lesson: (String) -> Unit) {
    val xp = s.xp.sumOf { it.amount }
    val today = LocalDate.now()
    val seconds = s.activities.filter { it.date == today.toString() }.sumOf { it.seconds }
    val next =
        s.content.lessons.firstOrNull { l ->
            s.progress.none { it.lessonId == l.id && it.completed }
        } ?: s.content.lessons.firstOrNull()
    Page("Welcome, ${p.nickname}.", "YOUR NEXT CHAPTER") {
        Text("Learn. Build. Conquer.", style = MaterialTheme.typography.titleMedium)
        Panel(
            "Level ${ProgressRules.level(xp)}",
            "$xp XP • ${CalendarRules.streak(s.activities.map { LocalDate.parse(it.date) }.toSet(),today)} active-day streak",
        ) {
            Text("Today: ${seconds/60} / ${p.goal} minutes")
            LinearProgressIndicator(progress = { (seconds / (p.goal * 60f)).coerceIn(0f, 1f) })
        }
        if (next != null)
            Panel("Continue learning", next.title, onClick = { lesson(next.id) }) {
                Text("${next.minutes} min • Open lesson →")
            }
        Panel(
            "Knowledge Forge",
            "Turn abstract systems into structures you can explore.",
            onClick = { open("forge") },
        ) {
            Text("Architecture • State • Concurrency", color = MaterialTheme.colorScheme.primary)
        }
        Section("Skill constellation")
        com.devquest.motion.Constellation(s.progress.count { it.completed }, s.content.lessons.size)
        s.content.paths.forEach { path ->
            val ids =
                s.content.lessons
                    .filter { l ->
                        s.content.chapters.any { it.id == l.chapterId && it.pathId == path.id }
                    }
                    .map { it.id }
            val done = s.progress.count { it.completed && it.lessonId in ids }
            Text("${path.title}  $done / ${ids.size}")
            LinearProgressIndicator(progress = { ProgressRules.completion(done, ids.size) })
        }
        Action("Daily practice") { open("practice") }
        Action("View progress") { open("progress") }
        Section("Recent achievements")
        if (s.unlocked.isEmpty())
            Text("Your first spark is ahead. Complete a lesson to earn 20 XP.")
        s.content.achievements
            .filter { it.id in s.unlocked }
            .forEach { Text("✦ ${it.title} — ${it.description}") }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun HomePreview() {
    QuestTheme { Home(Snapshot(), Preferences(), {}, {}) }
}
