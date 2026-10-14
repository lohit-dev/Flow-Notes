package com.king_grey.notes_app.ui.screens.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.king_grey.notes_app.ui.screens.folders.CheckStrokeIcon
import com.king_grey.notes_app.ui.screens.folders.CloseIcon
import com.king_grey.notes_app.ui.screens.folders.UndoIcon
import com.king_grey.notes_app.ui.screens.folders.strokeIcon
import com.king_grey.notes_app.ui.screens.shared.screenType

private enum class Tool { Pen, Marker, Eraser }

/**
 * Full screen drawing for a note. Strokes are kept as 0..1 coordinates of the paper, so the note
 * can replay them at any size. Color 0 means "ink", the theme's text color.
 *
 * Done returns every stroke; Cancel and back return nothing. The mockup's fourth tool (the sort
 * icon) has no action in the mockup either and is left out.
 */
@Composable
fun NoteDrawingScreen(
    initial: List<DrawingStroke>,
    onCancel: () -> Unit,
    onDone: (List<DrawingStroke>) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val strokes = remember { mutableStateListOf<DrawingStroke>().also { it.addAll(initial) } }
    val redoStack = remember { mutableStateListOf<DrawingStroke>() }
    var tool by remember { mutableStateOf(Tool.Pen) }
    var ink by remember { mutableStateOf(0L) }
    var width by remember { mutableStateOf(3f) }
    // The stroke being drawn right now, kept apart so only the live part redraws.
    var live by remember { mutableStateOf<DrawingStroke?>(null) }

    BackHandler(onBack = onCancel)

    val swatches = listOf(0L, 0xFFE53935L, 0xFF1E88E5L, 0xFF43A047L, 0xFFFB8C00L)

    Surface(modifier = Modifier.fillMaxSize(), color = colors.surface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            // ── Top row ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onCancel) {
                    Icon(CloseIcon, contentDescription = "Cancel", modifier = Modifier.size(24.dp))
                }
                Text(
                    "Drawing",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    style = screenType(22f, FontWeight.Bold),
                    color = colors.onSurface,
                )
                IconButton(
                    enabled = strokes.isNotEmpty(),
                    onClick = { redoStack.add(strokes.removeAt(strokes.lastIndex)) },
                ) { Icon(UndoIcon, contentDescription = "Undo", modifier = Modifier.size(24.dp)) }
                IconButton(
                    enabled = redoStack.isNotEmpty(),
                    onClick = { strokes.add(redoStack.removeAt(redoStack.lastIndex)) },
                ) { Icon(RedoIcon, contentDescription = "Redo", modifier = Modifier.size(24.dp)) }
            }

            // ── Paper ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                var paper by remember { mutableStateOf(IntSize.Zero) }
                Box(
                    modifier = Modifier
                        .aspectRatio(DrawingAspect)
                        .clip(RoundedCornerShape(28.dp))
                        .background(colors.surfaceContainerLow)
                        .border(1.dp, colors.outlineVariant, RoundedCornerShape(28.dp))
                        .pointerInput(tool, ink, width) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val w = size.width.toFloat().coerceAtLeast(1f)
                                val h = size.height.toFloat().coerceAtLeast(1f)
                                paper = IntSize(size.width, size.height)
                                redoStack.clear()

                                fun norm(o: Offset) =
                                    (o.x / w).coerceIn(0f, 1f) to (o.y / h).coerceIn(0f, 1f)

                                if (tool == Tool.Eraser) {
                                    fun erase(o: Offset) {
                                        val (nx, ny) = norm(o)
                                        // Touched strokes: any point within ~16px of the finger.
                                        val reachX = 16f / w
                                        val reachY = 16f / h
                                        strokes.removeAll { s ->
                                            s.xs.indices.any { i ->
                                                kotlin.math.abs(s.xs[i] - nx) < reachX && kotlin.math.abs(
                                                    s.ys[i] - ny
                                                ) < reachY
                                            }
                                        }
                                    }
                                    erase(down.position)
                                    down.consume()
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Main)
                                        val change = event.changes.firstOrNull() ?: break
                                        if (!change.pressed) break
                                        erase(change.position)
                                        change.consume()
                                    }
                                    return@awaitEachGesture
                                }

                                val xs = ArrayList<Float>()
                                val ys = ArrayList<Float>()
                                fun add(o: Offset) {
                                    val (nx, ny) = norm(o)
                                    xs.add(nx)
                                    ys.add(ny)
                                }

                                fun snapshot() = DrawingStroke(
                                    xs = xs.toList(),
                                    ys = ys.toList(),
                                    color = ink,
                                    width = width,
                                    highlighter = tool == Tool.Marker,
                                )

                                add(down.position)
                                live = snapshot()
                                down.consume()
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    val change = event.changes.firstOrNull() ?: break
                                    if (!change.pressed) break
                                    add(change.position)
                                    live = snapshot()
                                    change.consume()
                                }
                                strokes.add(snapshot())
                                live = null
                            }
                        },
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawStrokes(strokes.toList(), colors.onSurface)
                        live?.let { drawStrokes(listOf(it), colors.onSurface) }
                    }
                }
            }

            // ── Tools and Done ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    color = colors.surfaceContainerHigh,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ToolButton(PenToolIcon, "Pen", tool == Tool.Pen) { tool = Tool.Pen }
                        ToolButton(MarkerToolIcon, "Marker", tool == Tool.Marker) {
                            tool = Tool.Marker
                        }
                        ToolButton(EraserToolIcon, "Eraser", tool == Tool.Eraser) {
                            tool = Tool.Eraser
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { onDone(strokes.toList()) },
                    shape = RoundedCornerShape(20.dp),
                    color = colors.primary,
                    contentColor = colors.onPrimary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            CheckStrokeIcon,
                            contentDescription = "Done",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // ── Colors and width ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    swatches.forEach { c ->
                        val selected = ink == c
                        val fill = if (c == 0L) colors.onSurface else Color(c.toInt())
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(fill)
                                .then(
                                    if (selected) Modifier.border(
                                        3.dp,
                                        colors.primary,
                                        CircleShape
                                    ) else Modifier,
                                )
                                .clickable(enabled = tool != Tool.Eraser) { ink = c },
                        )
                    }
                }
                // The value-based Slider: `width` is the single source of truth, and the pen
                // gesture above re-reads it (pointerInput is keyed on `width`).
                Slider(
                    value = width,
                    onValueChange = { width = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    enabled = tool != Tool.Eraser,
                    valueRange = 1f..12f,
                )
            }
        }
    }
}

@Composable
private fun ToolButton(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colors.primaryContainer else Color.Transparent,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp))
        }
    }
}

private val RedoIcon by lazy { strokeIcon("Redo", "M16 5l4 4-4 4M20 9H10a5 5 0 0 0 0 10h6") }
private val PenToolIcon by lazy { strokeIcon("PenTool", "M4 20l1-4L16 5l3 3L8 19zM14 7l3 3") }
private val MarkerToolIcon by lazy {
    strokeIcon(
        "MarkerTool",
        "M9 3l6 6-8 8H3v-4zM13 5l6 6M3 21h18"
    )
}
private val EraserToolIcon by lazy {
    strokeIcon(
        "EraserTool",
        "M7 21h13M5 13l8-8 6 6-8 8H8zM9 9l6 6"
    )
}
