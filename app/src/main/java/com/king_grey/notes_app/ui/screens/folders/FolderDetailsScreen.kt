package com.king_grey.notes_app.ui.screens.folders

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.king_grey.notes_app.ui.screens.shared.FlowNotesLargeTopAppBar
import com.king_grey.notes_app.ui.screens.shared.Pebble
import com.king_grey.notes_app.ui.screens.shared.screenType
import com.king_grey.notes_app.ui.theme.FlowMotion
import com.king_grey.notes_app.ui.theme.FlowNotesTheme
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Models
// ─────────────────────────────────────────────────────────────────────────────

/** One note row inside a folder. [dateLabel] is already formatted ("Yesterday", "10/09/2026"). */
data class FolderNote(
    val id: Long,
    val title: String,
    val dateLabel: String,
    val preview: String,
    val pinned: Boolean = false,
)

/** The folder being looked at. Colors and names come from the same models as the Folders list. */
data class FolderDetailUiState(
    val folder: FolderItem,
    val notes: List<FolderNote> = emptyList(),
    /** Names of the other folders, used to reject a duplicate on rename. */
    val otherFolderNames: List<String> = emptyList(),
)

private enum class DetailDialog { None, Rename, Delete, Color }

// ─────────────────────────────────────────────────────────────────────────────
// Folder detail screen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * One folder: back + ⋮ in the top bar, the notes as a segmented list, a compose FAB, and the
 * folder's own dialogs (rename, color, delete). Sorting is the host's: [onSortNotes] should open
 * the same sort sheet Settings uses.
 *
 * After a delete the host should pop back to Folders; [onDeleteFolder] gets whether the notes
 * go too (to Recently deleted) or stay (moved to All notes).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FolderDetailScreen(
    state: FolderDetailUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenNote: (FolderNote) -> Unit = {},
    onNewNote: () -> Unit = {},
    onRename: (String) -> Unit = {},
    onColorChange: (FolderColor) -> Unit = {},
    onSortNotes: () -> Unit = {},
    onDeleteFolder: (deleteNotes: Boolean) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val colors = MaterialTheme.colorScheme
    val folder = state.folder

    var menuOpen by remember { mutableStateOf(false) }
    var dialog by rememberSaveable { mutableStateOf(DetailDialog.None) }
    val closeDialog = { dialog = DetailDialog.None }

    when (dialog) {
        DetailDialog.Rename -> FolderNameDialog(
            title = "Rename folder",
            confirmLabel = "Save",
            initialName = folder.name,
            existingNames = state.otherFolderNames,
            onDismiss = closeDialog,
            onConfirm = {
                onRename(it)
                closeDialog()
            },
        )

        DetailDialog.Delete -> DeleteFolderDialog(
            folder = folder.copy(noteCount = state.notes.size),
            onDismiss = closeDialog,
            onDelete = {
                closeDialog()
                onDeleteFolder(it)
            },
        )

        DetailDialog.Color -> FolderColorSheet(
            selected = folder.color,
            onDone = {
                onColorChange(it)
                closeDialog()
            },
            onDismiss = closeDialog,
        )

        DetailDialog.None -> Unit
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            FlowNotesLargeTopAppBar(
                title = folder.name,
                subtitle = noteCountLabel(state.notes.size),
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton(onBack) },
                actions = {
                    Box {
                        IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(48.dp)) {
                            Icon(MoreIcon, contentDescription = "Folder options")
                        }
                        FolderMenu(
                            expanded = menuOpen,
                            onDismiss = { menuOpen = false },
                            onRename = { menuOpen = false; dialog = DetailDialog.Rename },
                            onColor = { menuOpen = false; dialog = DetailDialog.Color },
                            onSort = { menuOpen = false; onSortNotes() },
                            onDelete = { menuOpen = false; dialog = DetailDialog.Delete },
                        )
                    }
                },
            )
        },
        // The empty folder has no FAB: its own New note button is the action.
        floatingActionButton = { if (state.notes.isNotEmpty()) ComposeFab(onNewNote) },
    ) { padding ->
        if (state.notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding()),
                contentAlignment = Alignment.Center,
            ) {
                EmptyBlock(
                    art = MaterialShapes.Pebble.toShape(),
                    accent = MaterialShapes.Clover4Leaf.toShape(),
                    icon = ComposeIcon,
                    title = "Nothing in ${folder.name} yet",
                    text = "Notes you add to this folder show up here.",
                    actionLabel = "New note in ${folder.name}",
                    actionIcon = ComposeIcon,
                    onAction = onNewNote,
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
                    bottom = padding.calculateBottomPadding() + 112.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(state.notes.size, key = { state.notes[it].id }) { index ->
                    val note = state.notes[index]
                    NoteRow(
                        title = note.title,
                        dateLabel = note.dateLabel,
                        preview = note.preview,
                        pinned = note.pinned,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = index,
                            count = state.notes.size
                        ),
                        onClick = { onOpenNote(note) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

private fun noteCountLabel(count: Int): String = if (count == 1) "1 note" else "$count notes"

/** .menu: 236dp card, 48dp items, a divider, then the red Delete. */
@Composable
private fun FolderMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onColor: () -> Unit,
    onSort: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 236.dp),
        shape = RoundedCornerShape(20.dp),
        containerColor = colors.surfaceContainerHigh,
    ) {
        MenuRow("Rename folder", PenIcon, onRename)
        MenuRow("Change color", SortIcon, onColor)
        MenuRow("Sort notes", ListIcon, onSort)
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            color = colors.outlineVariant,
        )
        MenuRow("Delete folder", TrashIcon, onDelete, danger = true)
    }
}

@Composable
internal fun MenuRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    danger: Boolean = false,
    value: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val tint = if (danger) colors.error else colors.onSurface
    DropdownMenuItem(
        text = {
            Text(
                label,
                style = screenType(15f, FontWeight.Normal, lineHeight = 20f),
                color = tint
            )
        },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = tint
            )
        },
        trailingIcon = value?.let {
            {
                Text(
                    it,
                    style = screenType(12f, FontWeight.Normal, lineHeight = 16f),
                    color = colors.onSurfaceVariant
                )
            }
        },
        onClick = onClick,
        modifier = Modifier.heightIn(min = 48.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Folder color sheet (#f-colorpick)
// ─────────────────────────────────────────────────────────────────────────────

/** 8 swatches in 4 columns. The picked one rounds to 20dp and shows a check. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderColorSheet(
    selected: FolderColor,
    onDone: (FolderColor) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()
    var picked by rememberSaveable { mutableStateOf(selected) }
    fun closeThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 12.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.outlineVariant),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 26.dp),
        ) {
            Text(
                text = "Folder color",
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
                    .semantics { heading() },
                style = screenType(20f, FontWeight.Bold, lineHeight = 24f),
                color = colors.onSurface,
            )
            // .cps: 4 columns, 14dp gaps
            FolderColor.entries.chunked(4).forEach { rowColors ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    rowColors.forEach { c ->
                        Swatch(c, picked == c) { picked = c }
                    }
                }
            }
            Text(
                text = "Used for the folder icon and its tag on notes.",
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 10.dp),
                style = screenType(13f, FontWeight.Normal, lineHeight = 18f),
                color = colors.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                FilledTonalButton(
                    onClick = { closeThen(onDismiss) },
                    modifier = Modifier.height(48.dp),
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer,
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) { Text("Cancel", style = screenType(14f, FontWeight.Bold, lineHeight = 20f)) }
                Button(
                    onClick = { closeThen { onDone(picked) } },
                    modifier = Modifier.height(48.dp),
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) { Text("Done", style = screenType(14f, FontWeight.Bold, lineHeight = 20f)) }
            }
        }
    }
}

@Composable
private fun Swatch(color: FolderColor, selected: Boolean, onClick: () -> Unit) {
    val shape: Shape = if (selected) RoundedCornerShape(20.dp) else CircleShape
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(shape)
            .background(resolve(color))
            .clickable(onClickLabel = color.label, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                CheckStrokeIcon,
                contentDescription = "Selected",
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared pieces (also used by RecentlyDeletedScreen)
// ─────────────────────────────────────────────────────────────────────────────

/** .li.ni: title, then "date  preview" on one line. A pin sits at the end when pinned. */
@Composable
private fun NoteRow(
    title: String,
    dateLabel: String,
    preview: String,
    pinned: Boolean,
    shapes: ListItemShapes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        modifier = modifier,
        trailingContent = if (pinned) {
            {
                Icon(
                    PinIcon,
                    contentDescription = "Pinned",
                    modifier = Modifier.size(20.dp),
                    tint = colors.primary
                )
            }
        } else null,
        supportingContent = {
            Text(
                text = androidx.compose.ui.text.buildAnnotatedString {
                    pushStyle(
                        androidx.compose.ui.text.SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                    )
                    append(dateLabel)
                    pop()
                    append("  ")
                    append(preview)
                },
                style = screenType(14f, FontWeight.Normal, lineHeight = 20f),
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors = rowColors(),
        contentPadding = RowPadding,
    ) { RowName(title) }
}

/** .btn.ib: the 48dp back button. */
@Composable
internal fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Icon(BackIcon, contentDescription = "Back")
    }
}

/** .fab: same 80dp / 26dp primaryContainer button as the Folders tab. */
@Composable
internal fun ComposeFab(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(26.dp),
        containerColor = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
    ) { Icon(ComposeIcon, contentDescription = "New note", modifier = Modifier.size(30.dp)) }
}

/**
 * The blob with an icon and the small accent shape on its corner. It is alive: it pops in with a
 * spring, the blob turns slowly and breathes while the icon stays upright, and the accent turns
 * the other way and bobs.
 */
@Composable
internal fun EmptyArt(
    art: Shape,
    accent: Shape,
    icon: ImageVector,
    size: Dp,
    iconSize: Dp,
    accentSize: Dp,
    accentOffsetX: Dp,
    accentOffsetY: Dp,
) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val loop = rememberInfiniteTransition(label = "empty-art")
    val spin by loop.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(30_000, easing = LinearEasing)),
        label = "spin",
    )
    val spinBack by loop.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(14_000, easing = LinearEasing)),
        label = "spin-back",
    )
    val breathe by loop.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            tween(2_600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "breathe",
    )
    val bob by loop.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            tween(2_000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "bob",
    )
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, FlowMotion.bouncy()) }

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = enter.value
                scaleY = enter.value
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = spin
                    scaleX = breathe
                    scaleY = breathe
                }
                .clip(art)
                .background(colors.primaryContainer),
        )
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .align(Alignment.Center),
            tint = colors.onPrimaryContainer,
        )
        Box(
            modifier = Modifier
                .size(accentSize)
                .align(Alignment.TopEnd)
                .offset(x = accentOffsetX, y = accentOffsetY)
                .graphicsLayer {
                    rotationZ = spinBack
                    translationY = with(density) { bob.dp.toPx() }
                }
                .clip(accent)
                .background(colors.tertiaryContainer)
                .border(3.dp, colors.background, accent),
        )
    }
}

/**
 * .es: a 168dp blob with an icon on it, a small accent shape on the corner, title, text and an
 * optional pill button. Used by the empty folder and the empty Recently deleted.
 */
@Composable
internal fun EmptyBlock(
    art: Shape,
    accent: Shape,
    icon: ImageVector,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onAction: () -> Unit = {},
    compact: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyArt(
            art = art,
            accent = accent,
            icon = icon,
            size = if (compact) 124.dp else 168.dp,
            iconSize = if (compact) 44.dp else 60.dp,
            accentSize = 54.dp,
            accentOffsetX = 4.dp,
            accentOffsetY = (-2).dp,
        )
        Spacer(Modifier.height(if (compact) 16.dp else 22.dp))
        Text(
            text = title,
            style = screenType(26f, FontWeight.Black, lineHeight = 29f, letterSpacing = -0.5f),
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = text,
            modifier = Modifier
                .padding(top = 8.dp)
                .widthIn(max = 270.dp),
            style = screenType(15f, FontWeight.Normal, lineHeight = 22f),
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null) {
            Button(
                onClick = onAction,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .heightIn(min = 48.dp),
                shapes = ButtonDefaults.shapes(),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                if (actionIcon != null) {
                    Icon(actionIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                }
                Text(actionLabel, style = screenType(14f, FontWeight.Bold, lineHeight = 20f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Icons used by these screens (mockup symbols; the shared ones live in FoldersScreen.kt)
// ─────────────────────────────────────────────────────────────────────────────

internal val BackIcon by lazy { strokeIcon("Back", "M19 12H5M11 6l-6 6 6 6") }
internal val MoreIcon by lazy {
    dotsIcon(
        "More",
        listOf(12f to 5f, 12f to 12f, 12f to 19f),
        r = 1.8f
    )
}
internal val PinIcon by lazy { strokeIcon("Pin", "M9 4h6l-1 6 3 3H7l3-3zM12 13v7") }
internal val ComposeIcon by lazy {
    strokeIcon(
        "Compose",
        "M12 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-6M18.4 2.6a2 2 0 0 1 2.9 2.9L12 14.8 8 16l1.2-4z"
    )
}
internal val ListIcon by lazy { strokeIcon("List", "m3 6 2 2 3-3M3 14l2 2 3-3M12 7h9M12 15h9") }
internal val SortIcon by lazy { strokeIcon("Sort", "M4 7h16M7 12h10M10 17h4") }
internal val UndoIcon by lazy { strokeIcon("Undo", "M8 5 4 9l4 4M4 9h10a5 5 0 0 1 0 10H8") }
internal val SelectAllIcon by lazy {
    strokeIcon(
        "SelectAll",
        "m4 12.5 4 4L13 10M10 18l1.5 1.5L20 9"
    )
}
internal val CloseIcon by lazy { strokeIcon("Close", "M6 6l12 12M18 6 6 18") }
internal val CheckStrokeIcon by lazy { strokeIcon("CheckStroke", "m5 12.5 4.5 4.5L19 7.5") }
internal val InfoIcon by lazy {
    strokeIcon("Info", "M21 12a9 9 0 1 1-18 0a9 9 0 1 1 18 0zM12 11v5M12 8v.01")
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private val PreviewNotes = listOf(
    FolderNote(
        1,
        "Swift learning plan",
        "Yesterday",
        "Views, state, bindings and navigation",
        pinned = true
    ),
    FolderNote(2, "SwiftUI state cheat sheet", "Friday", "@State, @Binding, @Observable"),
    FolderNote(
        3,
        "Kotlin coroutines basics",
        "10/09/2026",
        "Scopes, dispatchers and structured concurrency"
    ),
    FolderNote(
        4,
        "Compose layout notes",
        "02/09/2026",
        "Modifier order, constraints, intrinsic sizes"
    ),
)

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun FolderDetailPreview() {
    FlowNotesTheme(dynamicColor = false) {
        FolderDetailScreen(
            state = FolderDetailUiState(
                folder = FolderItem(3, "Learning", FolderColor.Violet, 4),
                notes = PreviewNotes,
            ),
        )
    }
}

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun FolderDetailEmptyPreview() {
    FlowNotesTheme(dynamicColor = false) {
        FolderDetailScreen(
            state = FolderDetailUiState(folder = FolderItem(4, "Ideas", FolderColor.Rose, 0)),
        )
    }
}
