package com.devquest.feature.lesson

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devquest.design.*
import com.devquest.model.*
import com.devquest.motion.ForgeScene

@Composable
fun LessonReader(
    s: Snapshot,
    id: String,
    reduced: Boolean,
    bookmark: () -> Unit,
    complete: () -> Unit,
    position: (Int) -> Unit,
    next: (String) -> Unit,
) {
    val lesson = s.content.lessons.find { it.id == id } ?: return
    val scroll = rememberScrollState(s.progress.find { it.lessonId == id }?.position ?: 0)
    DisposableEffect(id) { onDispose { position(scroll.value) } }
    val siblings = s.content.lessons.filter { it.chapterId == lesson.chapterId }
    Column(
        Modifier.fillMaxSize().verticalScroll(scroll).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text("FIELD NOTES • ${lesson.minutes} MIN", color = MaterialTheme.colorScheme.primary)
        Text(lesson.title, style = MaterialTheme.typography.headlineLarge)
        Text("Chapter: ${siblings.indexOf(lesson)+1} of ${siblings.size}")
        TextButton(onClick = bookmark) {
            Text(if (id in s.bookmarks) "Remove bookmark" else "Bookmark lesson")
        }
        lesson.blocks.forEach { b ->
            when (b.kind) {
                "code" -> Code(b.text)
                "exercise" -> Panel("Try it yourself", b.text)
                else -> Text(b.text, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Panel("Concept card", "Trace dependencies and explain each boundary.") {
            ForgeScene(
                Scenes.all[
                        if (lesson.chapterId.startsWith("coroutines")) 2
                        else if (lesson.chapterId.startsWith("compose")) 3 else 0],
                reduced,
                architecture =
                    !lesson.chapterId.startsWith("coroutines") &&
                        !lesson.chapterId.startsWith("compose"),
            )
        }
        Action(
            if (s.progress.any { it.lessonId == id && it.completed }) "Completed ✓"
            else "Mark as completed",
            enabled = s.progress.none { it.lessonId == id && it.completed },
            onClick = complete,
        )
        val following = s.content.lessons.getOrNull(s.content.lessons.indexOf(lesson) + 1)
        if (following != null) Action("Next: ${following.title}") { next(following.id) }
    }
}
