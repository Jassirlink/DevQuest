package com.devquest.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.devquest.design.*
import com.devquest.model.*

@Composable
fun Profile(
    s: Snapshot,
    p: Preferences,
    save: (Preferences) -> Unit,
    reset: () -> Unit,
    lesson: (String) -> Unit,
) {
    var name by rememberSaveable(p.nickname) { mutableStateOf(p.nickname) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    Page("Make it yours", "PROFILE & SETTINGS") {
        OutlinedTextField(
            name,
            { name = it.take(30) },
            label = { Text("Display nickname") },
            modifier = Modifier.fillMaxWidth(),
        )
        Action("Save nickname") { save(p.copy(nickname = name)) }
        Text("Daily goal: ${p.goal} minutes")
        Slider(
            p.goal.toFloat(),
            { save(p.copy(goal = it.toInt())) },
            valueRange = 5f..120f,
            modifier = Modifier.semantics { contentDescription = "Daily study goal in minutes" },
            steps = 22,
        )
        Section("Appearance")
        Row {
            listOf("Dark", "Light", "System").forEach { theme ->
                FilterChip(
                    p.theme == theme,
                    { save(p.copy(theme = theme)) },
                    label = { Text(theme) },
                )
            }
        }
        Row {
            Text("Reduced motion", Modifier.weight(1f))
            Switch(
                p.reducedMotion,
                { save(p.copy(reducedMotion = it)) },
                Modifier.semantics { contentDescription = "Reduced motion" },
            )
        }
        Text(
            "System animation settings are also respected. Reduced motion keeps diagrams static and step controls available."
        )
        Section("Bookmarks")
        if (s.bookmarks.isEmpty()) Text("Save useful lessons from the reader.")
        s.content.lessons
            .filter { it.id in s.bookmarks }
            .forEach { l -> Panel(l.title, onClick = { lesson(l.id) }) {} }
        Section("Achievements")
        s.content.achievements.forEach {
            Text((if (it.id in s.unlocked) "✦ " else "◇ ") + it.title + " • " + it.description)
        }
        Panel("About DevQuest", "Learn. Build. Conquer.") {
            Text("Version 1.0 • Content ${s.content.version} • Android only")
            Text(
                "Offline learning, with no account, tracking or network permission. Coding challenges use self-review; interviews use model answers."
            )
            Text(
                "Projection math adapted from Compose Lab by Dimitris Vlachos (MIT). All DevQuest scenes and learning content are original."
            )
        }
        TextButton(onClick = { confirm = true }) { Text("Reset learning progress") }
    }
    if (confirm)
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Reset all learning progress?") },
            text = {
                Text(
                    "This deletes XP, answers, sessions, drafts, bookmarks and activity. Bundled learning content and settings stay intact."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirm = false
                        reset()
                    }
                ) {
                    Text("Reset progress")
                }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } },
        )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    QuestTheme { Profile(Snapshot(), Preferences(), {}, {}, {}) }
}
