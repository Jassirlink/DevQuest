package com.devquest.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.devquest.design.*
import com.devquest.model.*
import com.devquest.motion.ForgeScene

@Composable
fun Onboarding(preferences: Preferences, save: (Preferences) -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var goals by rememberSaveable { mutableStateOf(listOf("Build Android apps")) }
    Page(
        if (page == 3) "Choose your quest"
        else
            listOf("Learn the foundations.", "Practice with purpose.", "Master by building.")[page],
        "DEVQUEST • LEARN. BUILD. CONQUER.",
    ) {
        Text(
            if (page == 3) "Select what you want to work toward. You can explore every path."
            else
                listOf(
                    "Unfold complex ideas into clear, connected layers.",
                    "Turn curiosity into confidence with deliberate practice.",
                    "Earn your progress by understanding and applying concepts.",
                )[page]
        )
        if (page == 2) com.devquest.motion.AchievementEmblem(preferences.reducedMotion)
        else if (page < 2)
            ForgeScene(Scenes.all[page], preferences.reducedMotion, architecture = page == 0)
        else
            listOf("Build Android apps", "Prepare for interviews", "Deepen Kotlin skills")
                .forEach { goal ->
                    FilterChip(
                        selected = goal in goals,
                        onClick = { goals = if (goal in goals) goals - goal else goals + goal },
                        label = { Text(goal) },
                    )
                }
        Row {
            if (page > 0) TextButton(onClick = { page-- }) { Text("Back") }
            if (page < 3) TextButton(onClick = { page = 3 }) { Text("Skip") }
            Action(if (page == 3) "Get Started" else "Next") {
                if (page < 3) page++
                else save(preferences.copy(onboarded = true, goals = goals.toSet()))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun OnboardingPreview() {
    QuestTheme { Onboarding(Preferences(reducedMotion = true), {}) }
}
