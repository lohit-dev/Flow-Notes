package com.king_grey.notes_app.ui.screens.folders

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import com.king_grey.notes_app.ui.screens.notes.NoteBlock
import com.king_grey.notes_app.ui.screens.notes.ReadOnlyBlocks
import com.king_grey.notes_app.ui.screens.shared.FlowNotesLargeTopAppBar
import com.king_grey.notes_app.ui.screens.shared.screenType
import com.king_grey.notes_app.ui.theme.FlowMotion
import com.king_grey.notes_app.ui.theme.FlowNotesTheme

// ─────────────────────────────────────────────────────────────────────────────
// Models
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A note waiting in Recently deleted. [daysLeft] counts down from 30. [dateLabel] is the full
 * "15 September 2026 at 6:30 PM" shown on the read-only note, [body] has its paragraphs
 * separated by a blank line.
 */
data class DeletedNote(
    val id: Long,
    val title: String,
    val daysLeft: Int,
    val dateLabel: String = "",
    val body: String = "",
    /** The full content for the read-only note. When empty, [body] is shown as plain paragraphs. */
    val blocks: List<NoteBlock> = emptyList(),
)

data class RecentlyDeletedUiState(val notes: List<DeletedNote> = emptyList())

private enum class DeletedDialog { None, EmptyAll, DeleteSelected }

private fun deletesIn(days: Int): String = when {
    days <= 0 -> "Deletes today"
    days == 1 -> "Deletes in 1 day"
    else -> "Deletes in $days days"
}

private fun notesLabel(count: Int): String = if (count == 1) "1 note" else "$count notes"

// ─────────────────────────────────────────────────────────────────────────────
// Recently deleted list
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The list of deleted notes. Each row has a restore button; tapping the row opens the read-only
 * note ([onOpenNote], see [DeletedNoteScreen]). The ⋮ menu has "Select notes" (multi-select with
 * Restore / Delete forever at the bottom) and "Empty recently deleted".
 *
 * Select mode and the confirm dialog are local state; restoring and deleting go out as
 * callbacks with the affected ids. Deleting forever is always confirmed here first.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecentlyDeletedScreen(
    state: RecentlyDeletedUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenNote: (DeletedNote) -> Unit = {},
    onRestore: (ids: List<Long>) -> Unit = {},
    onDeleteForever: (ids: List<Long>) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val colors = MaterialTheme.colorScheme
    val notes = state.notes

    var menuOpen by remember { mutableStateOf(false) }
    var selecting by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf<Set<Long>>(emptySet()) }
    var dialog by rememberSaveable { mutableStateOf(DeletedDialog.None) }

    // Rows can vanish (restored elsewhere): keep the selection to what still exists.
    LaunchedEffect(notes) {
        val ids = notes.map { it.id }.toSet()
        if (!ids.containsAll(selected)) selected = selected.intersect(ids)
        if (notes.isEmpty()) {
            selecting = false
            selected = emptySet()
        }
    }

    fun exitSelect() {
        selecting = false
        selected = emptySet()
    }

    BackHandler(enabled = selecting) { exitSelect() }

    when (dialog) {
        DeletedDialog.EmptyAll -> ConfirmForeverDialog(
            title = "Delete ${notesLabel(notes.size)} forever?",
            text = "They can’t be restored after this.",
            onDismiss = { dialog = DeletedDialog.None },
            onConfirm = {
                dialog = DeletedDialog.None
                onDeleteForever(notes.map { it.id })
            },
        )

        DeletedDialog.DeleteSelected -> ConfirmForeverDialog(
            title = "Delete ${notesLabel(selected.size)} forever?",
            text = "They can’t be restored after this.",
            onDismiss = { dialog = DeletedDialog.None },
            onConfirm = {
                dialog = DeletedDialog.None
                onDeleteForever(selected.toList())
                exitSelect()
            },
        )

        DeletedDialog.None -> Unit
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            FlowNotesLargeTopAppBar(
                title = "Recently deleted",
                subtitle = if (selecting) "${selected.size} selected" else notesLabel(notes.size),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (selecting) {
                        IconButton(onClick = ::exitSelect, modifier = Modifier.size(48.dp)) {
                            Icon(CloseIcon, contentDescription = "Cancel selection")
                        }
                    } else {
                        BackButton(onBack)
                    }
                },
                actions = {
                    if (selecting) {
                        IconButton(
                            onClick = { selected = notes.map { it.id }.toSet() },
                            modifier = Modifier.size(48.dp),
                        ) { Icon(SelectAllIcon, contentDescription = "Select all") }
                    } else {
                        Box {
                            IconButton(
                                onClick = { if (notes.isNotEmpty()) menuOpen = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(MoreIcon, contentDescription = "More options")
                            }
                            DropdownMenu(
                                expanded = menuOpen && notes.isNotEmpty(),
                                onDismissRequest = { menuOpen = false },
                                shape = RoundedCornerShape(20.dp),
                                containerColor = colors.surfaceContainerHigh,
                            ) {
                                MenuRow("Select notes", SelectAllIcon, {
                                    menuOpen = false
                                    selecting = true
                                })
                                MenuRow("Empty recently deleted", TrashIcon, {
                                    menuOpen = false
                                    dialog = DeletedDialog.EmptyAll
                                }, danger = true)
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (selecting) {
                RestoreDeletePair(
                    enabled = selected.isNotEmpty(),
                    onRestore = {
                        onRestore(selected.toList())
                        exitSelect()
                    },
                    onDelete = { dialog = DeletedDialog.DeleteSelected },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 30.dp),
                )
            }
        },
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding()),
                contentAlignment = Alignment.Center,
            ) {
                EmptyBlock(
                    art = MaterialShapes.Clover4Leaf.toShape(),
                    accent = MaterialShapes.Cookie9Sided.toShape(),
                    icon = TrashIcon,
                    title = "Nothing deleted",
                    text = "Deleted notes wait here for 30 days, then they are gone for good.",
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 4.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
            ) {
                item(key = "info") {
                    InfoBanner(
                        text = "Notes are deleted for good after 30 days.",
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                items(notes.size, key = { notes[it].id }) { index ->
                    val note = notes[index]
                    val shapes = ListItemDefaults.segmentedShapes(index = index, count = notes.size)
                    DeletedRow(
                        note = note,
                        shapes = shapes,
                        selecting = selecting,
                        selected = note.id in selected,
                        onClick = {
                            if (selecting) {
                                selected =
                                    if (note.id in selected) selected - note.id else selected + note.id
                            } else {
                                onOpenNote(note)
                            }
                        },
                        onRestore = { onRestore(listOf(note.id)) },
                        modifier = Modifier
                            .padding(top = if (index == 0) 0.dp else 2.dp)
                            .animateItem(),
                    )
                }
            }
        }
    }
}

/** .li.ni in Recently deleted: title, "Deletes in N days", and a primary-colored restore button. */
@Composable
private fun DeletedRow(
    note: DeletedNote,
    shapes: ListItemShapes,
    selecting: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        modifier = modifier,
        leadingContent = if (selecting) {
            { SelectDot(selected) }
        } else null,
        trailingContent = if (!selecting) {
            {
                // -8dp end so the 48dp button's icon lines up with the row's 16dp padding
                IconButton(
                    onClick = onRestore,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        UndoIcon,
                        contentDescription = "Restore ${note.title}",
                        tint = colors.primary,
                    )
                }
            }
        } else null,
        supportingContent = {
            Text(
                text = deletesIn(note.daysLeft),
                style = screenType(14f, FontWeight.Normal, lineHeight = 20f),
                color = colors.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = if (selected) colors.primaryContainer else colors.surfaceContainer,
        ),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = if (selecting) 16.dp else 8.dp,
            top = 14.dp,
            bottom = 14.dp,
        ),
    ) { RowName(note.title) }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Morph.toShape(progress: Float): Shape {
    return remember(this, progress) {
        object : Shape {
            private val matrix = Matrix()
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                matrix.reset()
                matrix.scale(size.width / 2f, size.height / 2f)
                matrix.translate(1f, 1f)
                val path = toPath(progress = progress).asComposePath()
                path.transform(matrix)
                return Outline.Generic(path)
            }
        }
    }
}

/** .sk: the 24dp selection circle. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SelectDot(selected: Boolean) {
    val colors = MaterialTheme.colorScheme
    // The circle morphs into a scalloped cookie when selected, with a springy pop.
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }
    val progress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = FlowMotion.spatial(),
        label = "select-morph",
    )
    val shape = morph.toShape(progress.coerceIn(0f, 1f))
    val fill by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = FlowMotion.effects(),
        label = "select-fill",
    )
    val ring by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.onSurfaceVariant,
        animationSpec = FlowMotion.effects(),
        label = "select-ring",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = FlowMotion.bouncy(),
        label = "select-scale",
    )
    Box(
        modifier = Modifier
            .size(24.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(fill)
            .border(2.dp, ring, shape),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = scaleIn(
                FlowMotion.bouncy(),
                initialScale = 0.2f
            ) + fadeIn(FlowMotion.effects()),
            exit = scaleOut(
                FlowMotion.fastSpatial(),
                targetScale = 0.2f
            ) + fadeOut(FlowMotion.effects()),
        ) {
            Icon(
                CheckStrokeIcon,
                contentDescription = "Selected",
                modifier = Modifier.size(16.dp),
                tint = colors.onPrimary,
            )
        }
    }
}

/** .info: a tonal banner, 20dp corners, icon and one line. */
@Composable
internal fun InfoBanner(text: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            InfoIcon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = colors.onSecondaryContainer
        )
        Text(
            text = text,
            style = screenType(14f, FontWeight.Normal, lineHeight = 17f),
            color = colors.onSecondaryContainer,
        )
    }
}

/** .dpair: Restore (primary) and Delete forever (error container), 2dp apart, 56dp tall. */
@Composable
private fun RestoreDeletePair(
    enabled: Boolean,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        PairButton(
            label = "Restore",
            icon = UndoIcon,
            onClick = onRestore,
            enabled = enabled,
            container = colors.primary,
            content = colors.onPrimary,
            shape = RoundedCornerShape(
                topStart = 28.dp,
                bottomStart = 28.dp,
                topEnd = 6.dp,
                bottomEnd = 6.dp
            ),
            modifier = Modifier.weight(1f),
        )
        PairButton(
            label = "Delete forever",
            icon = TrashIcon,
            onClick = onDelete,
            enabled = enabled,
            container = colors.errorContainer,
            content = colors.onErrorContainer,
            shape = RoundedCornerShape(
                topStart = 6.dp,
                bottomStart = 6.dp,
                topEnd = 28.dp,
                bottomEnd = 28.dp
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PairButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    container: Color,
    content: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 56.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(8.dp))
        Text(label, style = screenType(15f, FontWeight.Bold, lineHeight = 20f))
    }
}

/** Red dialog: title, one line, then Delete forever on top and Cancel under it. */
@Composable
private fun ConfirmForeverDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    DialogFrame(icon = TrashIcon, isError = true, title = title, onDismiss = onDismiss) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, bottom = 22.dp),
            style = screenType(15f, FontWeight.Normal, lineHeight = 22f),
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            DialogButton("Delete forever", onConfirm, colors.error, colors.onError)
            DialogButton(
                "Cancel",
                onDismiss,
                colors.secondaryContainer,
                colors.onSecondaryContainer
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// A deleted note, read only (#f-delnote)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * What opens when a row in Recently deleted is tapped: the note is read only, with a banner and
 * Restore / Delete forever at the bottom. Restoring is [onRestore]; deleting forever asks first.
 * The editor's own ⋮ is shown faded and does nothing, as in the mockup.
 */
@Composable
fun DeletedNoteScreen(
    note: DeletedNote,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRestore: () -> Unit = {},
    onDeleteForever: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    var confirming by rememberSaveable { mutableStateOf(false) }

    if (confirming) {
        ConfirmForeverDialog(
            title = "Delete forever?",
            text = "“${note.title}” will be gone for good.",
            onDismiss = { confirming = false },
            onConfirm = {
                confirming = false
                onDeleteForever()
            },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BackButton(onBack)
                Spacer(Modifier.weight(1f))
                Icon(
                    MoreIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp),
                    tint = colors.onSurface.copy(alpha = 0.4f),
                )
            }
        },
        bottomBar = {
            RestoreDeletePair(
                enabled = true,
                onRestore = onRestore,
                onDelete = { confirming = true },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 30.dp),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                )
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        ) {
            InfoBanner(
                text = "In Recently deleted. Restore it to edit. ${deletesIn(note.daysLeft)}.",
                modifier = Modifier.padding(bottom = 16.dp),
            )
            if (note.dateLabel.isNotEmpty()) {
                Text(
                    text = note.dateLabel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    style = screenType(13f, FontWeight.Normal, lineHeight = 18f),
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                text = note.title,
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .semantics { heading() },
                style = screenType(34f, FontWeight.Black, lineHeight = 37f, letterSpacing = -0.6f),
                color = colors.onSurface,
            )
            if (note.blocks.isNotEmpty()) {
                ReadOnlyBlocks(note.blocks)
            } else {
                note.body.split("\n\n").filter { it.isNotBlank() }.forEach { paragraph ->
                    Text(
                        text = paragraph,
                        modifier = Modifier.padding(bottom = 12.dp),
                        style = screenType(16f, FontWeight.Normal, lineHeight = 25f),
                        color = colors.onSurface,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private val PreviewDeleted = listOf(
    DeletedNote(
        1, "Birthday gift ideas", 27, "15 September 2026 at 6:30 PM",
        "Headphones, a good notebook, a framed print of the old city map.\n\n" +
                "Ask Meera about the bookshop near the station before ordering anything online.",
    ),
    DeletedNote(2, "Reading list", 22),
    DeletedNote(3, "Weekend plans", 18),
    DeletedNote(4, "Kotlin coroutines basics", 12),
    DeletedNote(5, "Book ideas", 4),
)

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun RecentlyDeletedPreview() {
    FlowNotesTheme(dynamicColor = false) {
        RecentlyDeletedScreen(state = RecentlyDeletedUiState(PreviewDeleted))
    }
}

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun RecentlyDeletedEmptyPreview() {
    FlowNotesTheme(dynamicColor = false) {
        RecentlyDeletedScreen(state = RecentlyDeletedUiState())
    }
}

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun DeletedNotePreview() {
    FlowNotesTheme(dynamicColor = false) {
        DeletedNoteScreen(note = PreviewDeleted.first())
    }
}