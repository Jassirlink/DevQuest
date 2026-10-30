package com.devquest.motion

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.devquest.model.Scene
import kotlinx.coroutines.delay

object MotionMath {
    fun tilt(value: Float): Float = if (value.isFinite()) value.coerceIn(-25f, 25f) else 0f

    fun advance(value: Float, seconds: Float): Float =
        (value + seconds.coerceIn(0f, 0.1f) / 5f).coerceIn(0f, 1f)

    fun phase(value: Float): Int = (value.coerceIn(0f, 1f) * 4).toInt().coerceAtMost(3)
}

@Composable
fun ForgeScene(scene: Scene, reduced: Boolean, architecture: Boolean = false) {
    var tilt by rememberSaveable(scene.title) { mutableFloatStateOf(0f) }
    var open by rememberSaveable(scene.title) { mutableStateOf(true) }
    var playing by rememberSaveable(scene.title) { mutableStateOf(false) }
    var progress by rememberSaveable(scene.title) { mutableFloatStateOf(0f) }
    var selected by rememberSaveable(scene.title) { mutableIntStateOf(0) }
    var counter by rememberSaveable(scene.title) { mutableIntStateOf(0) }
    val phase by remember { derivedStateOf { MotionMath.phase(progress) } }
    val unfold by
        animateFloatAsState(
            if (open) 1f else 0.15f,
            tween(if (reduced) 0 else 420),
            label = "Unfold",
        )
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(playing, reduced, scene.title, owner) {
        if (playing && !reduced)
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (progress < 1f) {
                    delay(32)
                    progress = MotionMath.advance(progress, 0.032f)
                }
                playing = false
            }
    }
    val accent = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val matrix = remember { Matrix() }
    val h = remember { FloatArray(9) }
    Canvas(
        Modifier.fillMaxWidth()
            .height(230.dp)
            .semantics {
                contentDescription =
                    "${scene.title}: ${scene.nodes.joinToString { it.title }}. Use the controls below to inspect each step."
            }
            .pointerInput(reduced) {
                if (!reduced)
                    detectDragGestures { change, drag ->
                        change.consume()
                        tilt = MotionMath.tilt(tilt + drag.x / 10f)
                    }
            }
    ) {
        if (architecture) {
            for (i in 2 downTo 0) {
                val y = size.height * 0.2f + i * size.height * 0.20f * unfold
                Homography.of(
                    size.width * 0.12f,
                    y,
                    0f,
                    1f,
                    tilt * 0.006f,
                    0f,
                    0.20f,
                    0.55f,
                    0.4f,
                    size.width / 2,
                    size.height / 2,
                    size.width * 3,
                    h,
                )
                withTransform({ transform(Homography.toMatrix(h, matrix)) }) {
                    drawRoundRect(
                        if (i == selected) accent else secondary.copy(alpha = 0.6f),
                        size = Size(size.width * 0.7f, 110f),
                        cornerRadius = CornerRadius(20f),
                    )
                    drawLine(
                        Color(0xFF10172B),
                        Offset(25f, 35f),
                        Offset(size.width * 0.5f, 35f),
                        6f,
                    )
                }
            }
        } else if (scene.title == "Coroutine Timeline") {
            // Two concurrent lanes: A suspends while B continues; cancellation ends the scope.
            val left = size.width * 0.05f
            val width = size.width * 0.9f
            for (lane in 0..1) {
                val y = size.height * (0.3f + lane * 0.35f)
                drawLine(
                    secondary.copy(alpha = 0.25f),
                    Offset(left, y),
                    Offset(left + width, y),
                    3f,
                )
                if (lane == 0) {
                    drawLine(accent, Offset(left, y), Offset(left + width * 0.3f, y), 18f)
                    drawLine(
                        accent,
                        Offset(left + width * 0.6f, y),
                        Offset(left + width * 0.85f, y),
                        18f,
                    )
                } else
                    drawLine(
                        secondary,
                        Offset(left + width * 0.1f, y),
                        Offset(left + width * 0.85f, y),
                        18f,
                    )
                drawCircle(accent, 10.dp.toPx(), Offset(left + width * progress, y))
            }
            val x = left + width * 0.85f
            drawLine(Color(0xFFFFC477), Offset(x, 20f), Offset(x, size.height - 20), 3f)
        } else {
            val count = scene.nodes.size
            for (i in 0 until count) {
                val x = size.width * (i + 1) / (count + 1)
                val y = size.height * (if (i % 2 == 0) 0.35f else 0.65f)
                if (i < count - 1)
                    drawLine(
                        secondary.copy(alpha = 0.5f),
                        Offset(x, y),
                        Offset(
                            size.width * (i + 2) / (count + 1),
                            size.height * (if (i % 2 == 0) 0.65f else 0.35f),
                        ),
                        5f,
                    )
                drawCircle(
                    if (i == selected || i == (progress * (count - 1)).toInt()) accent
                    else secondary.copy(alpha = 0.5f),
                    if (i == selected) 25.dp.toPx() else 18.dp.toPx(),
                    Offset(x, y),
                )
            }
            drawLine(
                accent,
                Offset(0f, size.height - 10),
                Offset(size.width * progress, size.height - 10),
                6f,
            )
        }
    }
    if (scene.title == "Coroutine Timeline")
        Text(
            "Task A: launch → suspend → resume. Task B runs during A's suspension. Amber line: scope cancellation. Current phase: ${scene.nodes[phase].title}."
        )
    if (scene.title == "Compose State") {
        Text("Counter state: $counter. Only readers of this value need to recompose.")
        Button(
            onClick = {
                counter++
                selected = 1
                progress = 0f
                playing = !reduced
            }
        ) {
            Text("Change state +1")
        }
    }
    if (reduced) Text("Reduced motion: static diagram. Select a node to inspect it.")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (architecture)
            Button(onClick = { open = !open }) { Text(if (open) "Fold layers" else "Open layers") }
        else
            Button(
                onClick = {
                    if (progress >= 1f) progress = 0f
                    if (reduced) progress = (progress + 0.25f).coerceAtMost(1f)
                    else playing = !playing
                }
            ) {
                Text(if (reduced) "Step" else if (playing) "Pause" else "Play")
            }
        TextButton(
            onClick = {
                tilt = 0f
                progress = 0f
                playing = false
                selected = 0
            }
        ) {
            Text("Reset view")
        }
    }
    if (!architecture)
        Slider(
            value = progress,
            onValueChange = {
                progress = it
                playing = false
            },
            modifier = Modifier.semantics { contentDescription = "Timeline position" },
        )
    scene.nodes.forEachIndexed { index, node ->
        OutlinedButton(
            onClick = { selected = index },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text((if (selected == index) "● " else "○ ") + node.title)
        }
    }
    Text(
        scene.nodes[selected.coerceAtMost(scene.nodes.lastIndex)].description,
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        "Connections: " +
            scene.edges.joinToString { (a, b) ->
                scene.nodes.first { it.id == a }.title +
                    " → " +
                    scene.nodes.first { it.id == b }.title
            },
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
fun Constellation(completed: Int, total: Int) {
    val color = MaterialTheme.colorScheme.primary
    val dim = MaterialTheme.colorScheme.outline
    Canvas(
        Modifier.fillMaxWidth().height(120.dp).semantics {
            contentDescription = "Skill constellation: $completed of $total lessons completed"
        }
    ) {
        val count = total.coerceAtLeast(1)
        for (i in 0 until count) {
            val x = size.width * (i + 1) / (count + 1)
            val y = size.height * (0.3f + (i % 3) * 0.2f)
            if (i > 0)
                drawLine(
                    dim.copy(alpha = 0.35f),
                    Offset(
                        size.width * i / (count + 1),
                        size.height * (0.3f + ((i - 1) % 3) * 0.2f),
                    ),
                    Offset(x, y),
                    2f,
                )
            drawCircle(
                if (i < completed) color else dim,
                if (i < completed) 6.dp.toPx() else 3.dp.toPx(),
                Offset(x, y),
            )
        }
    }
}

@Composable
fun AchievementEmblem(reduced: Boolean) {
    var assembled by rememberSaveable { mutableStateOf(false) }
    val amount by
        animateFloatAsState(
            if (assembled) 1f else 0f,
            tween(if (reduced) 0 else 500),
            label = "Achievement assembly",
        )
    val color = MaterialTheme.colorScheme.primary
    val second = MaterialTheme.colorScheme.secondary
    Canvas(
        Modifier.fillMaxWidth().height(220.dp).semantics {
            contentDescription =
                if (assembled) "Achievement emblem assembled" else "Six pieces ready to assemble"
        }
    ) {
        val radius = size.minDimension * 0.32f
        for (i in 0..5) {
            val a = (i * 60 - 90) * kotlin.math.PI / 180
            val b = ((i + 1) * 60 - 90) * kotlin.math.PI / 180
            val spread = (1 - amount) * radius * 0.35f
            val cx = center.x + kotlin.math.cos((a + b) / 2).toFloat() * spread
            val cy = center.y + kotlin.math.sin((a + b) / 2).toFloat() * spread
            val path =
                Path().apply {
                    moveTo(cx, cy)
                    lineTo(
                        cx + kotlin.math.cos(a).toFloat() * radius,
                        cy + kotlin.math.sin(a).toFloat() * radius,
                    )
                    lineTo(
                        cx + kotlin.math.cos(b).toFloat() * radius,
                        cy + kotlin.math.sin(b).toFloat() * radius,
                    )
                    close()
                }
            drawPath(path, if (i % 2 == 0) color else second)
        }
        drawCircle(Color(0xFF131B30), radius * 0.35f, center)
    }
    Button(onClick = { assembled = !assembled }) {
        Text(if (assembled) "Explore the pieces" else "Assemble achievement")
    }
}
