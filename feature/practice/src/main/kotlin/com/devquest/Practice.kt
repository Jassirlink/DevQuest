package com.devquest.feature.practice

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.devquest.design.*
import com.devquest.domain.QuizEngine
import com.devquest.model.*
import java.util.UUID

@Composable
fun Practice(
    s: Snapshot,
    start: (List<String>) -> Unit,
    resume: (String) -> Unit,
    interview: () -> Unit,
    challenge: (String) -> Unit,
) {
    var topic by rememberSaveable { mutableStateOf("all") }
    var difficulty by rememberSaveable { mutableStateOf("All") }
    Page("The practice ground", "BUILD YOUR CONFIDENCE") {
        Section("Topic filter")
        FilterChip(topic == "all", { topic = "all" }, label = { Text("All topics") })
        s.content.paths.forEach { p ->
            FilterChip(topic == p.id, { topic = p.id }, label = { Text(p.title) })
        }
        Row {
            listOf("All", "Foundation", "Intermediate").forEach { d ->
                FilterChip(difficulty == d, { difficulty = d }, label = { Text(d) })
            }
        }
        val questions =
            s.content.questions.filter {
                (topic == "all" || it.pathId == topic) &&
                    (difficulty == "All" || it.difficulty == difficulty)
            }
        Panel("Quiz arena", "Single choice, code output and bug hunts.") {
            Text("${questions.size} matching questions")
            Action("Start quiz", questions.isNotEmpty()) { start(questions.take(8).map { it.id }) }
        }
        Panel("Mock interview", "Offline prompts, model answers and honest self-assessment.") {
            Action("Practice interview", onClick = interview)
        }
        Section("Coding challenges • self-review")
        s.content.challenges.forEach { c ->
            Panel(c.title, c.difficulty, onClick = { challenge(c.id) }) {
                Text(
                    if (s.drafts.any { it.challengeId == c.id && it.reviewed }) "Reviewed ✓"
                    else "Draft, compare, learn"
                )
            }
        }
        Section("Session history")
        if (s.attempts.isEmpty() && s.interviews.isEmpty()) Text("Your sessions will appear here.")
        s.attempts.reversed().forEach { a ->
            Panel(
                if (a.finished) "Quiz • ${QuizEngine.accuracy(a.answers)}%" else "Quiz in progress",
                "${a.answers.size} / ${a.questionIds.size} answered",
                onClick = { resume(a.id) },
            ) {}
        }
        s.interviews.reversed().forEach {
            Text("Interview • confidence ${it.confidence}/5 • ${it.seconds}s")
        }
    }
}

@Composable
fun Quiz(
    s: Snapshot,
    id: String,
    submit: (String, Int) -> Unit,
    finish: () -> Unit,
    retry: (List<String>) -> Unit,
) {
    val a = s.attempts.find { it.id == id }
    if (a == null) {
        Page("Preparing quiz") { CircularProgressIndicator() }
        return
    }
    var index by
        rememberSaveable(id) {
            mutableIntStateOf(a.answers.size.coerceAtMost(a.questionIds.lastIndex))
        }
    var selection by rememberSaveable(id, index) { mutableIntStateOf(-1) }
    if (a.finished) {
        Page("Session complete", "QUIZ RESULTS") {
            Panel(
                "${QuizEngine.accuracy(a.answers)}% accuracy",
                "${a.answers.count { it.correct }} correct out of ${a.answers.size}",
            ) {
                Text("XP is awarded only on each question's first scored attempt.")
            }
            a.answers.forEach { answer ->
                val q = s.content.questions.first { it.id == answer.questionId }
                Panel((if (answer.correct) "✓ " else "× ") + q.prompt) {
                    Text("Your answer: ${q.options[answer.selected]}")
                    Text("Correct: ${q.options[q.correct]}")
                    Text(q.explanation)
                }
            }
            val missed = QuizEngine.missed(a)
            if (missed.isNotEmpty()) Action("Retry missed questions") { retry(missed) }
        }
    } else {
        val q = s.content.questions.first { it.id == a.questionIds[index] }
        val answer = a.answers.find { it.questionId == q.id }
        Page(
            "Question ${index+1} / ${a.questionIds.size}",
            "QUIZ ARENA • ${q.difficulty.uppercase()}",
        ) {
            Text(q.prompt, style = MaterialTheme.typography.headlineSmall)
            q.options.forEachIndexed { i, option ->
                OutlinedButton(
                    onClick = { selection = i },
                    enabled = answer == null,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) {
                    Text((if ((answer?.selected ?: selection) == i) "● " else "○ ") + option)
                }
            }
            if (answer == null) Action("Submit answer", selection >= 0) { submit(q.id, selection) }
            else {
                Panel(if (answer.correct) "Correct" else "Keep learning", q.explanation) {
                    Text("Answer: ${q.options[q.correct]}")
                }
                Action(if (index == a.questionIds.lastIndex) "See results" else "Next question") {
                    if (index == a.questionIds.lastIndex) finish() else index++
                }
            }
        }
    }
}

@Composable
fun Interview(s: Snapshot, save: (String, String, Int, Int) -> Unit) {
    var topic by rememberSaveable { mutableStateOf("all") }
    var qid by rememberSaveable { mutableStateOf(s.content.questions.firstOrNull()?.id ?: "") }
    var id by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    var revealed by rememberSaveable { mutableStateOf(false) }
    var confidence by rememberSaveable { mutableIntStateOf(3) }
    var started by rememberSaveable { mutableLongStateOf(0L) }
    var elapsed by rememberSaveable { mutableIntStateOf(0) }
    val saved = s.interviews.any { it.id == id }
    val owner = LocalLifecycleOwner.current
    val q = s.content.questions.find { it.id == qid } ?: return
    LaunchedEffect(started, revealed, saved) {
        if (started > 0 && !revealed && !saved)
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    elapsed =
                        ((SystemClock.elapsedRealtime() - started) / 1000).toInt().coerceAtLeast(0)
                    kotlinx.coroutines.delay(1000)
                }
            }
    }
    Page("Think it through", "MOCK INTERVIEW • OFFLINE") {
        Text("Explain your reasoning aloud or in your own notes. No recording or AI evaluation.")
        FilterChip(topic == "all", { topic = "all" }, label = { Text("All topics") })
        s.content.paths.forEach { p ->
            FilterChip(topic == p.id, { topic = p.id }, label = { Text(p.title) })
        }
        Action("New random prompt") {
            val pool = s.content.questions.filter { topic == "all" || it.pathId == topic }
            if (pool.isNotEmpty()) {
                qid = pool.random().id
                id = UUID.randomUUID().toString()
                revealed = false
                started = 0
                elapsed = 0
            }
        }
        Panel(q.prompt) {
            Text("Time: ${elapsed}s")
            if (!revealed) {
                Action("Start stopwatch", started == 0L) { started = SystemClock.elapsedRealtime() }
                Action("Reveal model answer") { revealed = true }
            } else {
                Text(q.options[q.correct])
                Text(q.explanation)
                Text("Follow-up: explain a concrete example and one failure mode.")
            }
        }
        if (revealed) {
            Text("Confidence: $confidence / 5")
            Slider(
                confidence.toFloat(),
                { confidence = it.toInt() },
                valueRange = 1f..5f,
                modifier =
                    Modifier.semantics { contentDescription = "Interview confidence, one to five" },
                steps = 3,
            )
            Action(if (saved) "Session saved ✓" else "Save interview", !saved) {
                save(id, qid, confidence, elapsed)
            }
        }
    }
}

@Composable
fun Challenge(s: Snapshot, id: String, save: (String, Boolean) -> Unit) {
    val c = s.content.challenges.find { it.id == id } ?: return
    val draft = s.drafts.find { it.challengeId == id }
    var code by
        rememberSaveable(id) {
            mutableStateOf(draft?.code ?: "fun solve() {\n    // Write your solution here\n}")
        }
    var hints by rememberSaveable(id) { mutableIntStateOf(0) }
    var reference by rememberSaveable(id) { mutableStateOf(false) }
    Page(c.title, "CODING WORKSHOP • ${c.difficulty.uppercase()}") {
        Text(c.description)
        Text(c.constraints)
        Panel("Example", c.example)
        Text(
            "Self-review only: this offline release does not compile, execute or judge Kotlin code.",
            color = MaterialTheme.colorScheme.tertiary,
        )
        OutlinedTextField(
            code,
            { code = it.take(20000) },
            Modifier.fillMaxWidth().heightIn(min = 220.dp),
            label = { Text("Kotlin draft") },
        )
        Action("Save draft") { save(code, false) }
        if (draft?.code == code) Text("Draft saved locally")
        Action("Reveal hint", hints < c.hints.size) { hints++ }
        c.hints.take(hints).forEach { Text(it) }
        Action("Compare reference solution") { reference = !reference }
        if (reference) {
            Code(c.solution)
            Text(
                "Compare empty inputs, constraints and complexity. You decide whether your solution is complete."
            )
            Action(
                if (draft?.reviewed == true) "Self-review completed ✓" else "Complete self-review",
                draft?.reviewed != true,
            ) {
                save(code, true)
            }
        }
    }
}
