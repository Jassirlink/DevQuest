package com.devquest.app

import android.animation.ValueAnimator
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.*
import androidx.lifecycle.compose.*
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import com.devquest.design.*
import com.devquest.feature.forge.Forge
import com.devquest.feature.home.Home
import com.devquest.feature.learn.Learn
import com.devquest.feature.lesson.LessonReader
import com.devquest.feature.onboarding.Onboarding
import com.devquest.feature.practice.*
import com.devquest.feature.profile.Profile
import com.devquest.feature.progress.Progress
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@HiltAndroidApp class QuestApplication : Application()

@Serializable data object HomeRoute

@Serializable data class Screen(val kind: String = "home", val id: String = "")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: QuestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            val dark =
                state.preferences.theme == "Dark" ||
                    (state.preferences.theme == "System" && isSystemInDarkTheme())
            SideEffect {
                val bars =
                    if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                        )
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            }
            QuestTheme(state.preferences.theme) { QuestApp(state, vm::act) }
        }
    }
}

@Composable
fun QuestApp(state: UiState, act: (Action) -> Unit) {
    val p = state.preferences
    val s = state.data
    val lifecycle = LocalLifecycleOwner.current
    var systemMotion by remember { mutableStateOf(ValueAnimator.areAnimatorsEnabled()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        systemMotion = ValueAnimator.areAnimatorsEnabled()
    }
    val reduced = p.reducedMotion || !systemMotion
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current =
        if (entry?.destination?.hasRoute<Screen>() == true) entry!!.toRoute<Screen>() else Screen()
    LaunchedEffect(current, lifecycle, p.onboarded) {
        if (
            p.onboarded &&
                current.kind in listOf("lesson", "quiz", "interview", "challenge", "forge")
        )
            lifecycle.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    delay(5000)
                    act(Action.Study(5, current.kind))
                }
            }
    }
    LaunchedEffect(state.pendingQuiz, p.onboarded) {
        if (p.onboarded)
            state.pendingQuiz?.let {
                nav.navigate(Screen("quiz", it))
                act(Action.Navigated)
            }
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (state.loading) {
            Page("Preparing your quest") { CircularProgressIndicator() }
        } else if (s.content.lessons.isEmpty()) {
            Page("Content unavailable") {
                Text(state.error ?: "No local lessons were loaded.")
                Action("Retry") { act(Action.RetryLoad) }
            }
        } else if (!p.onboarded)
            Box(Modifier.safeDrawingPadding()) {
                Onboarding(p.copy(reducedMotion = reduced)) {
                    act(Action.Settings(it.copy(reducedMotion = p.reducedMotion)))
                }
            }
        else
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar {
                        listOf(
                                "home" to "Home",
                                "learn" to "Learn",
                                "practice" to "Practice",
                                "profile" to "Profile",
                            )
                            .forEach { (key, label) ->
                                NavigationBarItem(
                                    selected = current.kind == key,
                                    onClick = {
                                        if (key == "home")
                                            nav.navigate(HomeRoute) {
                                                popUpTo<HomeRoute>()
                                                launchSingleTop = true
                                            }
                                        else
                                            nav.navigate(Screen(key)) {
                                                popUpTo<HomeRoute>()
                                                launchSingleTop = true
                                            }
                                    },
                                    icon = {
                                        Text(
                                            when (key) {
                                                "home" -> "⌂"
                                                "learn" -> "◇"
                                                "practice" -> "◎"
                                                else -> "◉"
                                            }
                                        )
                                    },
                                    label = { Text(label) },
                                )
                            }
                    }
                },
            ) { padding ->
                Column(Modifier.padding(padding).fillMaxSize()) {
                    if (current.kind !in listOf("home", "learn", "practice", "profile"))
                        TextButton(onClick = { nav.popBackStack() }) { Text("← Back") }
                    NavHost(nav, HomeRoute, Modifier.weight(1f)) {
                        composable<HomeRoute> {
                            Home(
                                s,
                                p,
                                { nav.navigate(Screen(it)) },
                                { nav.navigate(Screen("lesson", it)) },
                            )
                        }
                        composable<Screen> { back ->
                            val route = back.toRoute<Screen>()
                            val open: (String) -> Unit = { nav.navigate(Screen(it)) }
                            val lesson: (String) -> Unit = { nav.navigate(Screen("lesson", it)) }
                            when (route.kind) {
                                "home" -> Home(s, p, open, lesson)
                                "learn" ->
                                    Learn(s, null, { nav.navigate(Screen("path", it)) }, lesson)
                                "path" ->
                                    Learn(s, route.id, { nav.navigate(Screen("path", it)) }, lesson)
                                "lesson" ->
                                    key(route.id) {
                                        LessonReader(
                                            s,
                                            route.id,
                                            reduced,
                                            { act(Action.Bookmark(route.id)) },
                                            { act(Action.Complete(route.id)) },
                                            { act(Action.Position(route.id, it)) },
                                            lesson,
                                        )
                                    }
                                "forge" -> Forge(reduced)
                                "practice" ->
                                    Practice(
                                        s,
                                        { act(Action.StartQuiz(it)) },
                                        { nav.navigate(Screen("quiz", it)) },
                                        { open("interview") },
                                        { nav.navigate(Screen("challenge", it)) },
                                    )
                                "quiz" ->
                                    Quiz(
                                        s,
                                        route.id,
                                        { q, i -> act(Action.Answer(route.id, q, i)) },
                                        { act(Action.FinishQuiz(route.id)) },
                                        { act(Action.StartQuiz(it)) },
                                    )
                                "interview" ->
                                    Interview(s) { id, q, c, t ->
                                        act(Action.Interview(id, q, c, t))
                                    }
                                "challenge" ->
                                    Challenge(s, route.id) { code, review ->
                                        act(Action.Draft(route.id, code, review))
                                    }
                                "progress" -> Progress(s)
                                "profile" ->
                                    Profile(
                                        s,
                                        p,
                                        { act(Action.Settings(it)) },
                                        { act(Action.Reset) },
                                        lesson,
                                    )
                            }
                        }
                    }
                }
            }
        if (state.error != null && s.content.lessons.isNotEmpty())
            AlertDialog(
                onDismissRequest = { act(Action.DismissError) },
                title = { Text("Something needs attention") },
                text = { Text(state.error) },
                confirmButton = {
                    TextButton(onClick = { act(Action.DismissError) }) { Text("OK") }
                },
            )
    }
}
