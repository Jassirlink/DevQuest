package com.devquest.feature.forge

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.devquest.design.*
import com.devquest.model.Scenes
import com.devquest.motion.ForgeScene

@Composable
fun Forge(reduced: Boolean) {
    var scene by rememberSaveable { mutableIntStateOf(0) }
    Page("Knowledge Forge", "IDEAS, MADE TANGIBLE") {
        Text("Inspect the structure. Follow the flow. Build a mental model.")
        Scenes.all.forEachIndexed { i, s ->
            FilterChip(selected = i == scene, onClick = { scene = i }, label = { Text(s.title) })
        }
        Panel(Scenes.all[scene].title) {
            ForgeScene(Scenes.all[scene], reduced, architecture = scene == 0)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ForgePreview() {
    QuestTheme { Forge(true) }
}
