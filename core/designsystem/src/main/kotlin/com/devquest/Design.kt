package com.devquest.design

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Blue = Color(0xFF8DB4FF)
val Violet = Color(0xFFC0A6FF)
val Amber = Color(0xFFFFD083)
private val Dark =
    darkColorScheme(
        primary = Blue,
        secondary = Violet,
        tertiary = Amber,
        background = Color(0xFF0B1020),
        surface = Color(0xFF131B30),
        surfaceVariant = Color(0xFF202C45),
        onBackground = Color(0xFFEAF0FF),
        onSurface = Color(0xFFEAF0FF),
    )
private val Light =
    lightColorScheme(
        primary = Color(0xFF2456AF),
        secondary = Color(0xFF6840A5),
        tertiary = Color(0xFF825400),
        background = Color(0xFFF4F6FD),
        surface = Color.White,
    )

@Composable
fun QuestTheme(mode: String = "Dark", content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            if (mode == "Light" || mode == "System" && !isSystemInDarkTheme()) Light else Dark,
        shapes = Shapes(medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)),
        content = content,
    )
}

@Composable
fun Page(title: String, eyebrow: String = "DEVQUEST", content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 900.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                eyebrow,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            content()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun Panel(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val modifier =
        Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    Card(modifier, shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (subtitle != null)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            content()
        }
    }
}

@Composable
fun Action(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, Modifier.heightIn(min = 48.dp), enabled = enabled) { Text(label) }
}

@Composable
fun Section(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
fun Code(text: String) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val styled =
        remember(text, primary, tertiary) {
            buildAnnotatedString {
                append(text)
                Regex(
                        "\\b(fun|val|var|class|if|else|return|suspend|interface|when|override|data|sealed)\\b"
                    )
                    .findAll(text)
                    .forEach {
                        addStyle(
                            SpanStyle(color = primary, fontWeight = FontWeight.Bold),
                            it.range.first,
                            it.range.last + 1,
                        )
                    }
                Regex("\"[^\"]*\"").findAll(text).forEach {
                    addStyle(SpanStyle(color = tertiary), it.range.first, it.range.last + 1)
                }
            }
        }
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.background) {
        Text(
            styled,
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(16.dp),
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview
@Composable
private fun PreviewPanel() {
    QuestTheme {
        Page("Keep building.") {
            Panel("Knowledge Forge", "Explore the layers behind the interface") {
                Action("Explore") {}
            }
        }
    }
}

/** Two columns on larger windows, one readable column on compact phones. */
@Composable
fun <T> AdaptiveCards(items: List<T>, content: @Composable (T) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 640.dp) 2 else 1
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { item -> Box(Modifier.weight(1f)) { content(item) } }
                    if (row.size < columns) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
