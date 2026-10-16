package com.king_grey.notes_app.ui.screens.folders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.king_grey.notes_app.ui.screens.shared.FlowNotesLargeTopAppBar
import com.king_grey.notes_app.ui.screens.shared.screenType
import com.king_grey.notes_app.ui.theme.FlowMotion
import com.king_grey.notes_app.ui.theme.FlowNotesTheme

// ─────────────────────────────────────────────────────────────────────────────
// Models
// ─────────────────────────────────────────────────────────────────────────────

/** The 8 folder colors from the mockup's "Folder color" sheet (c1.c4 have a dark variant). */
enum class FolderColor(val label: String, val light: Color, val dark: Color) {
    Violet("Violet", Color(0xFF5B57E8), Color(0xFFA5A2FF)),
    Green("Green", Color(0xFF2F9E4F), Color(0xFF6FD58C)),
    Amber("Amber", Color(0xFFE08A00), Color(0xFFFFB74D)),
    Rose("Rose", Color(0xFFE0356B), Color(0xFFFF8AB3)),
    Teal("Teal", Color(0xFF00897B), Color(0xFF00897B)),
    Purple("Purple", Color(0xFF8E24AA), Color(0xFF8E24AA)),
    Brown("Brown", Color(0xFF6D4C41), Color(0xFF6D4C41)),
    Slate("Slate", Color(0xFF546E7A), Color(0xFF546E7A)),
}

data class FolderItem(
    val id: Long,
    val name: String,
    val color: FolderColor,
    val noteCount: Int,
)

/**
 * The list is in display order. "All notes" and "Recently deleted" are fixed rows, so they are not
 * part of [folders]; only their counts come in.
 */
data class FoldersUiState(
    val folders: List<FolderItem> = emptyList(),
    val allNotesCount: Int = 0,
    val deletedCount: Int = 0,
)

const val FolderNameMax = 30

private enum class FolderDialog { None, Create, Rename, Delete }

// Row sizes from the mockup (.li / .lead / .li.ed)
private val LeadSize = 44.dp
private val LeadShape =
    RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 22.dp)
internal val RowPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
private val EditRowPadding = PaddingValues(start = 12.dp, end = 10.dp, top = 12.dp, bottom = 12.dp)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Folders tab. Normal mode: All notes, the folders, Recently deleted, and a New folder FAB.
 * Edit mode (the Edit / Done button): rename (pencil), delete (minus) and drag to reorder.
 *
 * Edit mode and the three dialogs (new, rename, delete) are local screen state. Everything that
 * changes data goes out through callbacks. Reordering is live: [onMoveFolder] is called while the
 * handle is dragged, and [onReorderFinished] once when it is let go (persist the order there).
 *
 * The bottom navigation bar belongs to the host Scaffold.
 */
@Composable
fun FoldersScreen(
    state: FoldersUiState,
    modifier: Modifier = Modifier,
    onOpenAllNotes: () -> Unit = {},
    onOpenFolder: (FolderItem) -> Unit = {},
    onOpenDeleted: () -> Unit = {},
    onCreateFolder: (name: String) -> Unit = {},
    onRenameFolder: (id: Long, name: String) -> Unit = { _, _ -> },
    onDeleteFolder: (id: Long, deleteNotes: Boolean) -> Unit = { _, _ -> },
    onMoveFolder: (fromId: Long, toId: Long) -> Unit = { _, _ -> },
    onReorderFinished: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val colors = MaterialTheme.colorScheme

    var editing by rememberSaveable { mutableStateOf(false) }
    var dialog by rememberSaveable { mutableStateOf(FolderDialog.None) }
    var dialogFolderId by rememberSaveable { mutableLongStateOf(-1L) }
    val dialogFolder = state.folders.firstOrNull { it.id == dialogFolderId }

    val reorder = remember(listState) { ReorderState(listState) }
    reorder.onMove = onMoveFolder
    reorder.onFinished = onReorderFinished

    // A swap is only allowed again once the list has actually re-laid out.
    LaunchedEffect(reorder) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.key } }
            .collect { reorder.swapping = false }
    }

    // Nothing left to edit: leave edit mode.
    LaunchedEffect(state.folders.isEmpty()) {
        if (state.folders.isEmpty()) editing = false
    }

    val closeDialog = { dialog = FolderDialog.None }

    when (dialog) {
        FolderDialog.Create -> FolderNameDialog(
            title = "New folder",
            confirmLabel = "Create",
            initialName = "",
            existingNames = state.folders.map { it.name },
            onDismiss = closeDialog,
            onConfirm = { name ->
                onCreateFolder(name)
                closeDialog()
            },
        )

        FolderDialog.Rename -> if (dialogFolder != null) {
            FolderNameDialog(
                title = "Rename folder",
                confirmLabel = "Save",
                initialName = dialogFolder.name,
                existingNames = state.folders.filter { it.id != dialogFolder.id }.map { it.name },
                onDismiss = closeDialog,
                onConfirm = { name ->
                    onRenameFolder(dialogFolder.id, name)
                    closeDialog()
                },
            )
        }

        FolderDialog.Delete -> if (dialogFolder != null) {
            DeleteFolderDialog(
                folder = dialogFolder,
                onDismiss = closeDialog,
                onDelete = { deleteNotes ->
                    onDeleteFolder(dialogFolder.id, deleteNotes)
                    closeDialog()
                },
            )
        }

        FolderDialog.None -> Unit
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        // The host Scaffold already handles the bottom (navigation bar) inset.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            FlowNotesLargeTopAppBar(
                title = "Folders",
                subtitle = if (editing) "Rename, reorder or delete" else "On this device",
                scrollBehavior = scrollBehavior,
                actions = {
                    if (editing) {
                        Button(
                            onClick = { editing = false },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .heightIn(min = 40.dp),
                            shapes = ButtonDefaults.shapes(),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                        ) {
                            Text(
                                "Done",
                                style = screenType(14f, FontWeight.Bold, lineHeight = 20f)
                            )
                        }
                    } else {
                        FilledTonalButton(
                            onClick = { editing = true },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .heightIn(min = 40.dp),
                            shapes = ButtonDefaults.shapes(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = colors.secondaryContainer,
                                contentColor = colors.onSecondaryContainer,
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                        ) {
                            Text(
                                "Edit",
                                style = screenType(14f, FontWeight.Bold, lineHeight = 20f)
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            // .fab: 80dp, 26dp corners, primaryContainer. Hidden while editing and in the
            // empty state (the empty state has its own New folder button).
            AnimatedVisibility(
                visible = !editing && state.folders.isNotEmpty(),
                enter = scaleIn(
                    FlowMotion.bouncy(),
                    initialScale = 0.3f
                ) + fadeIn(FlowMotion.effects()),
                exit = scaleOut(
                    FlowMotion.fastSpatial(),
                    targetScale = 0.3f
                ) + fadeOut(FlowMotion.effects()),
            ) {
                FloatingActionButton(
                    onClick = { dialog = FolderDialog.Create },
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(26.dp),
                    containerColor = colors.primaryContainer,
                    contentColor = colors.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
                ) {
                    Icon(
                        FolderPlusIcon,
                        contentDescription = "New folder",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 4.dp,
                // room for the FAB so the last row can scroll clear of it
                bottom = padding.calculateBottomPadding() + 112.dp,
            ),
        ) {
            val folders = state.folders

            // ── All notes + folders (one segmented group) ────────────────────
            item(key = "all") {
                val shapes = ListItemDefaults.segmentedShapes(
                    index = 0,
                    count = 1 + folders.size,
                )
                if (editing) {
                    LockedRow(count = null, shapes = shapes, modifier = Modifier.animateItem())
                } else {
                    FolderRow(
                        name = "All notes",
                        color = FolderColor.Violet,
                        icon = NoteIcon,
                        count = state.allNotesCount,
                        shapes = shapes,
                        onClick = onOpenAllNotes,
                        modifier = Modifier.animateItem(),
                    )
                }
            }

            items(folders.size, key = { folders[it].id }) { index ->
                val folder = folders[index]
                val shapes = ListItemDefaults.segmentedShapes(
                    index = index + 1,
                    count = 1 + folders.size,
                )
                val dragging = reorder.draggedId == folder.id
                val rowModifier = Modifier
                    .padding(top = 2.dp)
                    .let { if (dragging) it else it.animateItem() }
                    .zIndex(if (dragging) 1f else 0f)
                    .graphicsLayer { translationY = if (dragging) reorder.offset else 0f }

                if (editing) {
                    EditFolderRow(
                        folder = folder,
                        shapes = shapes,
                        dragging = dragging,
                        onDelete = {
                            dialogFolderId = folder.id
                            dialog = FolderDialog.Delete
                        },
                        onRename = {
                            dialogFolderId = folder.id
                            dialog = FolderDialog.Rename
                        },
                        onDragStart = { reorder.start(folder.id) },
                        onDrag = { reorder.drag(it) },
                        onDragEnd = { reorder.end() },
                        onMoveUp = if (index > 0) {
                            { onMoveFolder(folder.id, folders[index - 1].id); onReorderFinished() }
                        } else null,
                        onMoveDown = if (index < folders.lastIndex) {
                            { onMoveFolder(folder.id, folders[index + 1].id); onReorderFinished() }
                        } else null,
                        modifier = rowModifier,
                    )
                } else {
                    FolderRow(
                        name = folder.name,
                        color = folder.color,
                        icon = FolderIcon,
                        count = folder.noteCount,
                        shapes = shapes,
                        onClick = { onOpenFolder(folder) },
                        modifier = rowModifier,
                    )
                }
            }

            // ── Recently deleted (its own group, 20dp below) ─────────────────
            if (!editing) {
                item(key = "deleted") {
                    FolderRow(
                        name = "Recently deleted",
                        color = FolderColor.Rose,
                        icon = TrashIcon,
                        count = state.deletedCount,
                        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
                        onClick = onOpenDeleted,
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .animateItem(),
                    )
                }
            }

            // ── Footnote in edit mode ────────────────────────────────────────
            if (editing) {
                item(key = "footnote") {
                    Text(
                        text = "All notes can’t be renamed or deleted.",
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 14.dp)
                            .animateItem(),
                        style = screenType(13f, FontWeight.Normal, lineHeight = 18f),
                        color = colors.onSurfaceVariant,
                    )
                }
            }

            // ── Empty: no folders yet (shown under All notes) ────────────────
            if (folders.isEmpty()) {
                item(key = "empty") {
                    // Center the empty state in what is left under the cards, like the mockup's .fill.
                    val density = LocalDensity.current
                    val remaining by remember {
                        derivedStateOf {
                            val info = listState.layoutInfo
                            val used = info.visibleItemsInfo
                                .filter { it.key != "empty" }
                                .sumOf { it.size }
                            (info.viewportSize.height - info.beforeContentPadding - info.afterContentPadding - used)
                                .coerceAtLeast(0)
                        }
                    }
                    EmptyFolders(
                        onNewFolder = { dialog = FolderDialog.Create },
                        modifier = Modifier
                            .height(with(density) { remaining.toDp() }.coerceAtLeast(320.dp))
                            .animateItem(),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Rows
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun rowColors(): ListItemColors =
    ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

@Composable
internal fun resolve(color: FolderColor): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) color.dark else color.light

/** .lead: 44dp tile, the folder color at 22% over the screen background, icon in the color. */
@Composable
internal fun Lead(color: FolderColor, icon: ImageVector) {
    val tint = resolve(color)
    Box(
        modifier = Modifier
            .size(LeadSize)
            .clip(LeadShape)
            .background(
                tint.copy(alpha = 0.22f).compositeOver(MaterialTheme.colorScheme.background)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = tint)
    }
}

@Composable
internal fun RowName(text: String) {
    Text(
        text = text,
        style = screenType(16f, FontWeight.SemiBold, lineHeight = 22f),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
    )
}

/** Normal mode row: lead, name, count. */
@Composable
private fun FolderRow(
    name: String,
    color: FolderColor,
    icon: ImageVector,
    count: Int,
    shapes: ListItemShapes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        modifier = modifier,
        leadingContent = { Lead(color, icon) },
        trailingContent = {
            Text(
                text = count.toString(),
                style = screenType(14f, FontWeight.Normal, lineHeight = 18f, tabular = true),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = rowColors(),
        contentPadding = RowPadding,
    ) { RowName(name) }
}

/** .li.ed.lk: All notes in edit mode. 60% opacity, a lock, lead aligned with the other rows. */
@Composable
private fun LockedRow(count: Int?, shapes: ListItemShapes, modifier: Modifier = Modifier) {
    ListItem(
        shapes = shapes,
        modifier = modifier.alpha(0.6f),
        leadingContent = { Lead(FolderColor.Violet, NoteIcon) },
        trailingContent = {
            Icon(
                LockIcon,
                contentDescription = "Can’t be changed",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = rowColors(),
        // 52dp = 12 padding + 28 minus button + 12 gap, so the tile lines up with editable rows
        contentPadding = PaddingValues(start = 52.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
    ) { RowName("All notes") }
}

/** .li.ed: red minus, lead, name, pencil, drag handle. */
@Composable
private fun EditFolderRow(
    folder: FolderItem,
    shapes: ListItemShapes,
    dragging: Boolean,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val currentStart by rememberUpdatedState(onDragStart)
    val currentDrag by rememberUpdatedState(onDrag)
    val currentEnd by rememberUpdatedState(onDragEnd)

    ListItem(
        shapes = shapes,
        modifier = modifier.semantics {
            customActions = listOfNotNull(
                onMoveUp?.let { CustomAccessibilityAction("Move up") { it(); true } },
                onMoveDown?.let { CustomAccessibilityAction("Move down") { it(); true } },
            )
        },
        leadingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // .mn: 28dp red circle with a minus
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colors.error)
                        .clickable(
                            onClickLabel = "Delete ${folder.name}",
                            role = Role.Button,
                            onClick = onDelete,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        MinusIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = colors.onError
                    )
                }
                Lead(folder.color, FolderIcon)
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmallIconButton(PenIcon, "Rename ${folder.name}", onClick = onRename)
                // The handle only: dragging anywhere else would fight the list scroll.
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { currentStart() },
                                onDrag = { change, amount ->
                                    change.consume()
                                    currentDrag(amount.y)
                                },
                                onDragEnd = { currentEnd() },
                                onDragCancel = { currentEnd() },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        DragIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = colors.onSurfaceVariant,
                    )
                }
            }
        },
        colors = if (dragging) {
            ListItemDefaults.colors(containerColor = colors.surfaceContainerHigh)
        } else {
            rowColors()
        },
        contentPadding = EditRowPadding,
    ) { RowName(folder.name) }
}

@Composable
internal fun SmallIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    androidx.compose.material3.IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(
            icon,
            contentDescription = description,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reorder (drag handle)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Drag to reorder inside a LazyColumn. Folder rows use their Long id as the item key; every other
 * row uses a String key, so a folder can only swap with another folder.
 */
private class ReorderState(private val listState: LazyListState) {
    var draggedId by mutableStateOf<Long?>(null)
        private set
    var offset by mutableFloatStateOf(0f)
        private set

    /** True between a swap and the next layout, so one drag event cannot swap twice. */
    var swapping = false

    var onMove: (Long, Long) -> Unit = { _, _ -> }
    var onFinished: () -> Unit = {}

    fun start(id: Long) {
        draggedId = id
        offset = 0f
    }

    fun drag(dy: Float) {
        val id = draggedId ?: return
        offset += dy
        if (swapping) return
        val items = listState.layoutInfo.visibleItemsInfo
        val dragged = items.firstOrNull { it.key == id } ?: return
        val center = dragged.offset + dragged.size / 2f + offset
        val target = items.firstOrNull {
            it.key is Long && it.key != id && center >= it.offset && center < it.offset + it.size
        } ?: return
        swapping = true
        onMove(id, target.key as Long)
        // The dragged row now sits in the target's slot; keep it under the finger.
        offset += dragged.offset - target.offset
    }

    fun end() {
        if (draggedId != null) onFinished()
        draggedId = null
        offset = 0f
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state (.es, small variant next to All notes)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyFolders(onNewFolder: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // .art: a flower blob with the icon on it and a small sunny shape on the corner
        Box(modifier = Modifier.size(124.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialShapes.Flower.toShape())
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    FolderPlusIcon,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = colors.onPrimaryContainer,
                )
            }
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .clip(MaterialShapes.Sunny.toShape())
                    .background(colors.tertiaryContainer)
                    .border(3.dp, colors.background, MaterialShapes.Sunny.toShape()),
            )
        }
        Text(
            text = "Sort notes into folders",
            modifier = Modifier.padding(top = 16.dp),
            style = screenType(26f, FontWeight.Black, lineHeight = 29f, letterSpacing = -0.5f),
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Make a folder for work, study or trips, then move notes into it.",
            modifier = Modifier
                .padding(top = 8.dp)
                .widthIn(max = 270.dp),
            style = screenType(15f, FontWeight.Normal, lineHeight = 22f),
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onNewFolder,
            modifier = Modifier
                .padding(top = 20.dp)
                .heightIn(min = 48.dp),
            shapes = ButtonDefaults.shapes(),
            contentPadding = PaddingValues(horizontal = 24.dp),
        ) {
            Icon(FolderPlusIcon, contentDescription = null, modifier = Modifier.size(20.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            Text("New folder", style = screenType(14f, FontWeight.Bold, lineHeight = 20f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialogs (.dlg: 28dp card, blob icon, centered title, stacked full-width buttons)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun DialogFrame(
    icon: ImageVector,
    isError: Boolean,
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(28.dp),
            color = colors.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // .di: a plain 24dp icon in primary (or error), centered
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isError) colors.error else colors.primary,
                )
                // h3: 24sp / 600, margin 16 top, 20 bottom
                Text(
                    text = title,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 20.dp)
                        .semantics { heading() },
                    style = screenType(24f, FontWeight.SemiBold, lineHeight = 32f),
                    color = colors.onSurface,
                    textAlign = TextAlign.Center,
                )
                content()
            }
        }
    }
}

/** .dlg .tbtn: 56dp pill. Primary (or error) on top, tonal Cancel last. */
@Composable
internal fun DialogButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
    ) { Text(text, style = screenType(16f, FontWeight.Bold, lineHeight = 22f)) }
}

/** New folder and Rename folder share this. Name is 1..30 chars and unique. */
@Composable
internal fun FolderNameDialog(
    title: String,
    confirmLabel: String,
    initialName: String,
    existingNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var name by rememberSaveable { mutableStateOf(initialName) }
    val trimmed = name.trim()
    val duplicate = existingNames.any { it.equals(trimmed, ignoreCase = true) } ||
            trimmed.equals("All notes", ignoreCase = true) ||
            trimmed.equals("Recently deleted", ignoreCase = true)
    val valid = trimmed.isNotEmpty() && !duplicate
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) { focus.requestFocus() }

    val submit = {
        if (valid) {
            keyboard?.hide()
            onConfirm(trimmed)
        }
    }

    DialogFrame(
        icon = if (initialName.isEmpty()) FolderPlusIcon else PenIcon,
        isError = false,
        title = title,
        onDismiss = onDismiss,
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= FolderNameMax) name = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focus),
            label = { Text("Name") },
            singleLine = true,
            isError = duplicate,
            shape = RoundedCornerShape(4.dp),
            supportingText = if (duplicate) {
                { Text("A folder with this name already exists") }
            } else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )
        // .cn: counter
        Text(
            text = "${name.length} / $FolderNameMax",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, top = 6.dp),
            style = screenType(12f, FontWeight.Normal, lineHeight = 16f, tabular = true),
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
        // .da: text buttons, end aligned, 8dp apart, 20dp below the counter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = screenType(14f, FontWeight.Bold, lineHeight = 20f))
            }
            TextButton(onClick = { submit() }, enabled = valid) {
                Text(confirmLabel, style = screenType(14f, FontWeight.Bold, lineHeight = 20f))
            }
        }
    }
}

/**
 * "Delete Learning?" with the two outcomes for its notes. An empty folder only needs one button.
 * Deleting the notes moves them to Recently deleted (the host decides), keeping them just moves
 * them to All notes.
 */
@Composable
internal fun DeleteFolderDialog(
    folder: FolderItem,
    onDismiss: () -> Unit,
    onDelete: (deleteNotes: Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val count = folder.noteCount
    DialogFrame(
        icon = TrashIcon,
        isError = true,
        title = "Delete ${folder.name}?",
        onDismiss = onDismiss,
    ) {
        Text(
            text = when (count) {
                0 -> "This folder is empty."
                1 -> "This folder has 1 note. Choose what happens to it."
                else -> "This folder has $count notes. Choose what happens to them."
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, bottom = 22.dp),
            style = screenType(15f, FontWeight.Normal, lineHeight = 22f),
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (count == 0) {
                DialogButton("Delete folder", { onDelete(false) }, colors.error, colors.onError)
            } else {
                DialogButton(
                    "Delete folder, keep notes",
                    { onDelete(false) },
                    colors.error,
                    colors.onError
                )
                DialogButton(
                    "Delete folder and notes",
                    { onDelete(true) },
                    colors.errorContainer,
                    colors.onErrorContainer
                )
            }
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
// Icons (the mockup's 24dp stroke symbols, 2dp round strokes)
// ─────────────────────────────────────────────────────────────────────────────

internal fun strokeIcon(name: String, path: String, width: Float = 2f): ImageVector =
    ImageVector.Builder(
        name = name, defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f,
    ).addPath(
        pathData = addPathNodes(path),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()

/** Filled dots: each is two half circles. */
internal fun dotsIcon(name: String, centers: List<Pair<Float, Float>>, r: Float): ImageVector =
    ImageVector.Builder(
        name = name, defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f,
    ).addPath(
        pathData = addPathNodes(
            centers.joinToString(" ") { (x, y) ->
                "M${x - r},$y a$r,$r 0 1,0 ${2 * r},0 a$r,$r 0 1,0 ${-2 * r},0z"
            },
        ),
        fill = SolidColor(Color.Black),
    ).build()

internal val NoteIcon by lazy {
    strokeIcon(
        "Note",
        "M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8zM14 3v5h5M9 13h6M9 17h4"
    )
}
internal val FolderIcon by lazy {
    strokeIcon(
        "Folder",
        "M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"
    )
}
internal val FolderPlusIcon by lazy {
    strokeIcon(
        "FolderPlus",
        "M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM12 10v5M9.5 12.5h5"
    )
}
internal val TrashIcon by lazy {
    strokeIcon("Trash", "M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v6M14 11v6")
}
internal val PenIcon by lazy {
    strokeIcon("Pen", "m4 20 1-4L16.5 4.5a2.1 2.1 0 0 1 3 3L8 19z")
}
internal val LockIcon by lazy {
    strokeIcon(
        "Lock",
        "M7 11h10a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-6a2 2 0 0 1 2-2zM8 11V8a4 4 0 0 1 8 0v3"
    )
}
internal val MinusIcon by lazy { strokeIcon("Minus", "M5 12h14", width = 2.4f) }
internal val DragIcon by lazy {
    dotsIcon(
        "Drag",
        listOf(9f to 7f, 15f to 7f, 9f to 12f, 15f to 12f, 9f to 17f, 15f to 17f),
        r = 1.6f,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private val PreviewFolders = listOf(
    FolderItem(1, "Personal", FolderColor.Green, 12),
    FolderItem(2, "Work", FolderColor.Amber, 16),
    FolderItem(3, "Learning", FolderColor.Violet, 8),
    FolderItem(4, "Ideas", FolderColor.Rose, 6),
)

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun FoldersScreenPreview() {
    FlowNotesTheme(dynamicColor = false) {
        FoldersScreen(
            state = FoldersUiState(folders = PreviewFolders, allNotesCount = 42, deletedCount = 5),
        )
    }
}

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun FoldersScreenEmptyPreview() {
    FlowNotesTheme(dynamicColor = false) {
        FoldersScreen(state = FoldersUiState(allNotesCount = 42, deletedCount = 5))
    }
}

@Preview(widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun NewFolderDialogPreview() {
    FlowNotesTheme(dynamicColor = false) {
        FolderNameDialog(
            title = "New folder",
            confirmLabel = "Create",
            initialName = "Recipes",
            existingNames = emptyList(),
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun DeleteFolderDialogPreview() {
    FlowNotesTheme(dynamicColor = false) {
        DeleteFolderDialog(folder = PreviewFolders[2], onDismiss = {}, onDelete = {})
    }
}
