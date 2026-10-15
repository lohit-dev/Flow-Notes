package com.king_grey.notes_app.ui.screens.notes

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.toShape
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.king_grey.notes_app.ui.screens.folders.ComposeIcon
import com.king_grey.notes_app.ui.screens.folders.CloseIcon
import com.king_grey.notes_app.ui.screens.folders.EmptyBlock
import com.king_grey.notes_app.ui.screens.folders.FolderColor
import com.king_grey.notes_app.ui.screens.folders.FolderIcon
import com.king_grey.notes_app.ui.screens.folders.FolderItem
import com.king_grey.notes_app.ui.screens.folders.FolderNameDialog
import com.king_grey.notes_app.ui.screens.folders.FolderPlusIcon
import com.king_grey.notes_app.ui.screens.folders.ListIcon
import com.king_grey.notes_app.ui.screens.folders.MenuRow
import com.king_grey.notes_app.ui.screens.folders.MoreIcon
import com.king_grey.notes_app.ui.screens.folders.NoteIcon
import com.king_grey.notes_app.ui.screens.folders.PinIcon
import com.king_grey.notes_app.ui.screens.folders.RowName
import com.king_grey.notes_app.ui.screens.folders.RowPadding
import com.king_grey.notes_app.ui.screens.folders.SelectAllIcon
import com.king_grey.notes_app.ui.screens.folders.SelectDot
import com.king_grey.notes_app.ui.screens.folders.TrashIcon
import com.king_grey.notes_app.ui.screens.folders.resolve
import com.king_grey.notes_app.ui.screens.folders.rowColors
import com.king_grey.notes_app.ui.screens.folders.strokeIcon
import com.king_grey.notes_app.ui.screens.settings.NoteSort
import com.king_grey.notes_app.ui.screens.settings.SortOrder
import com.king_grey.notes_app.ui.screens.settings.SortSheet
import com.king_grey.notes_app.ui.screens.shared.screenType
import com.king_grey.notes_app.ui.theme.FlowNotesTheme
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Models
// ─────────────────────────────────────────────────────────────────────────────

/** A note as the list shows it. [dateLabel] is already formatted ("10:42 AM", "Monday", "24/09/2026"). */
data class NoteItem(
    val id: Long,
    val title: String,
    val preview: String,
    val dateLabel: String,
    val folderId: Long? = null,
    val folderName: String? = null,
    val folderColor: FolderColor? = null,
    val pinned: Boolean = false,
)

/** "Pinned", "Today", "Previous 7 days", "Earlier". The view model decides the grouping. */
data class NoteSection(val title: String, val notes: List<NoteItem>)

enum class NotesViewMode { List, Grid }

data class NotesUiState(
    val sections: List<NoteSection> = emptyList(),
    /** Every folder, in the Folders tab order. Drives the filter chips and the move sheet. */
    val folders: List<FolderItem> = emptyList(),
    /** null = All. */
    val selectedFolderId: Long? = null,
    val viewMode: NotesViewMode = NotesViewMode.List,
    val sort: NoteSort = NoteSort.DateEdited,
    val sortOrder: SortOrder = SortOrder.NewestFirst,
)

private enum class NotesSheet { None, Sort, Move }
private enum class NamePrompt { None, NewFolder, NewFolderForMove }

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The Notes tab: a search pill with the ⋮ menu, folder filter chips, notes grouped by date (list)
 * or as cards (grid), and the compose FAB that opens the New menu.
 *
 * Modes that live in this screen: the New menu, select mode (long press a row, or ⋮ → Select
 * notes) with pin / move / share / delete, and the move and sort sheets. Swipe a row right to
 * move it, left to delete it (to Recently deleted, with an Undo snackbar).
 *
 * Tapping the search pill is [onOpenSearch]; the search screen itself is separate. While select
 * mode is on [onSelectionModeChange] is true so the host can hide the bottom navigation bar.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotesScreen(
    state: NotesUiState,
    modifier: Modifier = Modifier,
    onOpenNote: (NoteItem) -> Unit = {},
    onNewNote: () -> Unit = {},
    onNewChecklist: () -> Unit = {},
    onNewFolder: (name: String) -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onFolderFilter: (folderId: Long?) -> Unit = {},
    onViewModeChange: (NotesViewMode) -> Unit = {},
    onSortSave: (NoteSort, SortOrder) -> Unit = { _, _ -> },
    onOpenDeleted: () -> Unit = {},
    onTogglePin: (ids: List<Long>, pin: Boolean) -> Unit = { _, _ -> },
    onMoveNotes: (ids: List<Long>, folderId: Long) -> Unit = { _, _ -> },
    onCreateFolderAndMove: (ids: List<Long>, name: String) -> Unit = { _, _ -> },
    onShareNotes: (ids: List<Long>) -> Unit = {},
    onDeleteNotes: (ids: List<Long>) -> Unit = {},
    onUndoDelete: (ids: List<Long>) -> Unit = {},
    onSelectionModeChange: (Boolean) -> Unit = {},
    /** The host draws the same dim layer over the bottom bar while the New menu is open. */
    onFabOpenChange: (Boolean) -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    val allNotes = remember(state.sections) { state.sections.flatMap { it.notes } }
    val grid = state.viewMode == NotesViewMode.Grid

    var fabOpen by rememberSaveable { mutableStateOf(false) }
    var selecting by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf<Set<Long>>(emptySet()) }
    var sheet by rememberSaveable { mutableStateOf(NotesSheet.None) }
    var prompt by rememberSaveable { mutableStateOf(NamePrompt.None) }
    // A swipe-right opened the move sheet: cancelling it should also leave select mode.
    var moveFromSwipe by rememberSaveable { mutableStateOf(false) }

    // The selection can only hold notes that still exist.
    LaunchedEffect(allNotes) {
        val ids = allNotes.map { it.id }.toSet()
        if (!ids.containsAll(selected)) selected = selected.intersect(ids)
    }
    LaunchedEffect(selecting) { onSelectionModeChange(selecting) }

    fun exitSelect() {
        selecting = false
        selected = emptySet()
    }

    fun toggle(id: Long) {
        selected = if (id in selected) selected - id else selected + id
    }

    fun startSelect(id: Long) {
        selecting = true
        selected = setOf(id)
    }

    /** Delete with an Undo snackbar. */
    fun delete(ids: List<Long>) {
        onDeleteNotes(ids)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                message = if (ids.size == 1) "Note moved to Recently deleted" else "${ids.size} notes moved to Recently deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) onUndoDelete(ids)
        }
    }

    LaunchedEffect(fabOpen) { onFabOpenChange(fabOpen) }
    DisposableEffect(Unit) { onDispose { onFabOpenChange(false) } }

    BackHandler(enabled = selecting || fabOpen) {
        if (fabOpen) fabOpen = false else exitSelect()
    }

    // ── Sheets and dialogs ──────────────────────────────────────────────────
    when (sheet) {
        NotesSheet.Sort -> SortSheet(
            sort = state.sort,
            order = state.sortOrder,
            onSave = onSortSave,
            onDismiss = { sheet = NotesSheet.None },
        )

        NotesSheet.Move -> MoveSheet(
            count = selected.size,
            folders = state.folders,
            initialFolderId = allNotes
                .filter { it.id in selected }
                .map { it.folderId }
                .distinct()
                .singleOrNull(),
            onCancel = {
                sheet = NotesSheet.None
                if (moveFromSwipe) exitSelect()
                moveFromSwipe = false
            },
            onMove = { folderId ->
                moveFromSwipe = false
                sheet = NotesSheet.None
                onMoveNotes(selected.toList(), folderId)
                exitSelect()
            },
            onNewFolder = { prompt = NamePrompt.NewFolderForMove },
        )

        NotesSheet.None -> Unit
    }

    if (prompt != NamePrompt.None) {
        FolderNameDialog(
            title = "New folder",
            confirmLabel = if (prompt == NamePrompt.NewFolderForMove) "Create and move" else "Create",
            initialName = "",
            existingNames = state.folders.map { it.name },
            onDismiss = { prompt = NamePrompt.None },
            onConfirm = { name ->
                if (prompt == NamePrompt.NewFolderForMove) {
                    onCreateFolderAndMove(selected.toList(), name)
                    sheet = NotesSheet.None
                    exitSelect()
                } else {
                    onNewFolder(name)
                }
                prompt = NamePrompt.None
            },
        )
    }

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            AnimatedVisibility(visible = selecting, enter = fadeIn(), exit = fadeOut()) {
                SelectBar(
                    count = selected.size,
                    onClose = ::exitSelect,
                    onSelectAll = {
                        val all = allNotes.map { it.id }.toSet()
                        selected = if (selected.containsAll(all)) emptySet() else all
                    },
                    modifier = Modifier.statusBarsPadding(),
                )
            }
        },
        floatingActionButton = {
            // The empty screen in the mockup has no FAB: its own "New note" button is the action.
            if (!selecting && !(allNotes.isEmpty() && state.folders.isEmpty())) {
                NewFabMenu(
                    open = fabOpen,
                    onOpenChange = { fabOpen = it },
                    onNewFolder = { prompt = NamePrompt.NewFolder },
                    onNewChecklist = onNewChecklist,
                    onNewNote = onNewNote,
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selecting,
                enter = fadeIn() + slideInVertically(tween(200)) { it / 2 },
                exit = fadeOut() + slideOutVertically(tween(150)) { it / 2 },
            ) {
                SelectToolbar(
                    enabled = selected.isNotEmpty(),
                    allPinned = allNotes.filter { it.id in selected }.let { picked -> picked.isNotEmpty() && picked.all { it.pinned } },
                    onPin = { pin ->
                        onTogglePin(selected.toList(), pin)
                        exitSelect()
                    },
                    onMove = { sheet = NotesSheet.Move },
                    onShare = { onShareNotes(selected.toList()) },
                    onDelete = {
                        delete(selected.toList())
                        exitSelect()
                    },
                )
            }
        },
    ) { padding ->
        val top = if (selecting) padding.calculateTopPadding() else statusTop
        val bottom = padding.calculateBottomPadding() + 112.dp

        Box(modifier = Modifier.fillMaxSize()) {
            val header: @Composable () -> Unit = {
                NotesHeader(
                    state = state,
                    selecting = selecting,
                    onOpenSearch = onOpenSearch,
                    onFolderFilter = onFolderFilter,
                    onSelectNotes = { selecting = true },
                    onSort = { sheet = NotesSheet.Sort },
                    onToggleView = {
                        onViewModeChange(if (grid) NotesViewMode.List else NotesViewMode.Grid)
                    },
                    onOpenDeleted = onOpenDeleted,
                )
            }

            if (allNotes.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(Modifier.height(top))
                    header()
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        val folderName = state.folders.firstOrNull { it.id == state.selectedFolderId }?.name
                        EmptyBlock(
                            art = MaterialShapes.Cookie9Sided.toShape(),
                            accent = MaterialShapes.Puffy.toShape(),
                            icon = NoteIcon,
                            title = if (folderName == null) "Your notes live here" else "Nothing in $folderName yet",
                            text = if (folderName == null) {
                                "Write your first note. It stays on this phone and works without internet."
                            } else {
                                "Notes you add to this folder show up here."
                            },
                            actionLabel = "New note",
                            actionIcon = ComposeIcon,
                            onAction = onNewNote,
                            modifier = Modifier.padding(bottom = 20.dp),
                        )
                    }
                }
            } else if (!grid) {
                NotesList(
                    state = state,
                    header = header,
                    selecting = selecting,
                    selected = selected,
                    top = top,
                    bottom = bottom,
                    onOpen = { if (selecting) toggle(it.id) else onOpenNote(it) },
                    onLongPress = { if (!selecting) startSelect(it.id) },
                    onSwipeMove = {
                        startSelect(it.id)
                        moveFromSwipe = true
                        sheet = NotesSheet.Move
                    },
                    onSwipeDelete = { delete(listOf(it.id)) },
                )
            } else {
                NotesGrid(
                    notes = allNotes,
                    header = header,
                    selecting = selecting,
                    selected = selected,
                    top = top,
                    bottom = bottom,
                    onOpen = { if (selecting) toggle(it.id) else onOpenNote(it) },
                    onLongPress = { if (!selecting) startSelect(it.id) },
                )
            }

            // ── New menu: scrim + three pills above the FAB ─────────────────
            AnimatedVisibility(
                visible = fabOpen,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(120)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClickLabel = "Close menu",
                        ) { fabOpen = false },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header: search pill, ⋮ menu, folder chips
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotesHeader(
    state: NotesUiState,
    selecting: Boolean,
    onOpenSearch: () -> Unit,
    onFolderFilter: (Long?) -> Unit,
    onSelectNotes: () -> Unit,
    onSort: () -> Unit,
    onToggleView: () -> Unit,
    onOpenDeleted: () -> Unit,
) {
    if (selecting) return
    val colors = MaterialTheme.colorScheme
    var menuOpen by remember { mutableStateOf(false) }
    val grid = state.viewMode == NotesViewMode.Grid

    Column {
        // .aws: 56dp pill, search icon, placeholder, ⋮ at the end
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surfaceContainerHigh)
                .clickable(onClickLabel = "Search notes", role = Role.Button, onClick = onOpenSearch)
                .padding(start = 18.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(SearchIcon, contentDescription = null, modifier = Modifier.size(24.dp), tint = colors.onSurfaceVariant)
            Text(
                text = "Search notes",
                modifier = Modifier.weight(1f),
                style = screenType(16f, FontWeight.Normal, lineHeight = 22f),
                color = colors.onSurfaceVariant,
            )
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(48.dp)) {
                    Icon(MoreIcon, contentDescription = "More options", tint = colors.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    modifier = Modifier.width(236.dp),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = colors.surfaceContainerHigh,
                ) {
                    MenuRow("Select notes", SelectAllIcon, { menuOpen = false; onSelectNotes() })
                    MenuRow("Sort by", SortLinesIcon, { menuOpen = false; onSort() }, value = state.sort.label)
                    MenuRow(
                        label = if (grid) "List view" else "Grid view",
                        icon = if (grid) ListIcon else GridIcon,
                        onClick = { menuOpen = false; onToggleView() },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        color = colors.outlineVariant,
                    )
                    MenuRow("Recently deleted", TrashIcon, { menuOpen = false; onOpenDeleted() })
                }
            }
        }

        // .cr: All + one chip per folder, scrolls sideways edge to edge
        if (state.folders.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                item(key = "chip-all") {
                    NoteChip("All", state.selectedFolderId == null) { onFolderFilter(null) }
                }
                items(state.folders.size, key = { "chip-${state.folders[it].id}" }) { i ->
                    val folder = state.folders[i]
                    NoteChip(folder.name, state.selectedFolderId == folder.id) { onFolderFilter(folder.id) }
                }
            }
        }
    }
}

/** .chip: 36dp, 8dp corners, outlined. The picked one is tonal, bold, with a check. */
@Composable
internal fun NoteChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        label = {
            Text(
                text = label,
                style = screenType(14f, if (selected) FontWeight.Bold else FontWeight.Medium, lineHeight = 20f),
            )
        },
        leadingIcon = if (selected) {
            { Icon(CheckIcon, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else null,
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.secondaryContainer,
            selectedLabelColor = colors.onSecondaryContainer,
            selectedLeadingIconColor = colors.onSecondaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colors.outlineVariant,
            selectedBorderColor = Color.Transparent,
        ),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// List view
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotesList(
    state: NotesUiState,
    header: @Composable () -> Unit,
    selecting: Boolean,
    selected: Set<Long>,
    top: androidx.compose.ui.unit.Dp,
    bottom: androidx.compose.ui.unit.Dp,
    onOpen: (NoteItem) -> Unit,
    onLongPress: (NoteItem) -> Unit,
    onSwipeMove: (NoteItem) -> Unit,
    onSwipeDelete: (NoteItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = top, bottom = bottom),
    ) {
        item(key = "header") { header() }

        state.sections.filter { it.notes.isNotEmpty() }.forEach { section ->
            item(key = "h-${section.title}") {
                // .h2: 20 / 700, 20dp above, 10dp below
                Text(
                    text = section.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp)
                        .semantics { heading() },
                    style = screenType(20f, FontWeight.Bold, lineHeight = 24f),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            items(section.notes.size, key = { "n-${section.notes[it].id}" }) { index ->
                val note = section.notes[index]
                val count = section.notes.size
                val shape = segmentShape(index, count)
                SwipeNote(
                    enabled = !selecting,
                    shape = shape,
                    onMove = { onSwipeMove(note) },
                    onDelete = { onSwipeDelete(note) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = if (index == 0) 0.dp else 2.dp)
                        .animateItem(),
                ) {
                    NoteRow(
                        note = note,
                        selecting = selecting,
                        selected = note.id in selected,
                        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
                        onClick = { onOpen(note) },
                        onLongClick = { onLongPress(note) },
                    )
                }
            }
        }
    }
}

/** Lets a child use [amount] of the parent's horizontal padding on both sides. */
private fun Modifier.bleed(amount: androidx.compose.ui.unit.Dp): Modifier = layout { measurable, constraints ->
    val extra = amount.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = constraints.minWidth + extra * 2,
            maxWidth = constraints.maxWidth + extra * 2,
        ),
    )
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra, 0) }
}

/** 28dp on the outside of a group, 4dp between rows (same as the `.list` rule). */
internal fun segmentShape(index: Int, count: Int): Shape {
    val outer = 28.dp
    val inner = 4.dp
    return RoundedCornerShape(
        topStart = if (index == 0) outer else inner,
        topEnd = if (index == 0) outer else inner,
        bottomStart = if (index == count - 1) outer else inner,
        bottomEnd = if (index == count - 1) outer else inner,
    )
}

/** .li.ni: title, "date  preview", folder tag; a pin on the end. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun NoteRow(
    note: NoteItem,
    selecting: Boolean,
    selected: Boolean,
    shapes: androidx.compose.material3.ListItemShapes,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    highlight: String = "",
) {
    val colors = MaterialTheme.colorScheme
    SegmentedListItem(
        onClick = onClick,
        onLongClick = onLongClick,
        shapes = shapes,
        leadingContent = if (selecting) {
            { SelectDot(selected) }
        } else null,
        trailingContent = if (note.pinned) {
            { Icon(PinIcon, contentDescription = "Pinned", modifier = Modifier.size(20.dp), tint = colors.primary) }
        } else null,
        supportingContent = {
            Column {
                Text(
                    text = dateAndPreview(note.dateLabel, note.preview, colors.onSurface, highlight),
                    style = screenType(14f, FontWeight.Normal, lineHeight = 20f),
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (note.folderName != null && note.folderColor != null) {
                    Spacer(Modifier.height(8.dp))
                    FolderTag(note.folderName, note.folderColor)
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (selected) colors.primaryContainer else colors.surfaceContainer,
        ),
        contentPadding = RowPadding,
    ) {
        Text(
            text = highlighted(note.title, highlight),
            style = screenType(16f, FontWeight.SemiBold, lineHeight = 22f),
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun dateAndPreview(date: String, preview: String, dateColor: Color, query: String) = buildAnnotatedString {
    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = dateColor)) { append(date) }
    append("  ")
    append(highlighted(preview, query))
}

/**
 * .mark: every case-insensitive occurrence of [query] gets the tertiary container behind it.
 * Plain indexOf, no regex.
 */
@Composable
internal fun highlighted(text: String, query: String) = buildAnnotatedString {
    val colors = MaterialTheme.colorScheme
    val needle = query.trim()
    if (needle.isEmpty()) {
        append(text)
        return@buildAnnotatedString
    }
    var from = 0
    while (from < text.length) {
        val hit = text.indexOf(needle, startIndex = from, ignoreCase = true)
        if (hit < 0) break
        append(text.substring(from, hit))
        withStyle(
            SpanStyle(background = colors.primary.copy(alpha = 0.3f)),
        ) { append(text.substring(hit, hit + needle.length)) }
        from = hit + needle.length
    }
    append(text.substring(from))
}

/** .fl: a small tonal pill with the folder icon (in the folder's color) and its name. */
@Composable
internal fun FolderTag(name: String, color: FolderColor) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.secondaryContainer)
            .padding(start = 8.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(FolderIcon, contentDescription = null, modifier = Modifier.size(14.dp), tint = colors.onSurface)
        Text(
            text = name,
            style = screenType(12f, FontWeight.Normal, lineHeight = 16f),
            color = colors.onSurface,
            maxLines = 1,
        )
    }
}

/**
 * .swr: swipe right to move (primary container, folder-move icon), left to delete (error
 * container, trash). Move snaps back and opens the move sheet; delete lets the row go.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeNote(
    enabled: Boolean,
    shape: Shape,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onMove()
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }

                SwipeToDismissBoxValue.Settled -> false
            }
        },
    )
    SwipeToDismissBox(
        state = state,
        modifier = modifier.clip(shape),
        enableDismissFromStartToEnd = enabled,
        enableDismissFromEndToStart = enabled,
        backgroundContent = {
            val moving = state.dismissDirection == SwipeToDismissBoxValue.StartToEnd
            val deleting = state.dismissDirection == SwipeToDismissBoxValue.EndToStart
            if (moving || deleting) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (moving) colors.primaryContainer else colors.errorContainer)
                        .padding(horizontal = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (moving) Arrangement.Start else Arrangement.End,
                ) {
                    val tint = if (moving) colors.onPrimaryContainer else colors.error
                    Icon(if (moving) MoveIcon else TrashIcon, contentDescription = null, modifier = Modifier.size(22.dp), tint = tint)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = if (moving) "Move" else "Delete",
                        style = screenType(14f, FontWeight.Bold, lineHeight = 20f),
                        color = tint,
                    )
                }
            }
        },
    ) { content() }
}

// ─────────────────────────────────────────────────────────────────────────────
// Grid view (.gcols / .gc)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotesGrid(
    notes: List<NoteItem>,
    header: @Composable () -> Unit,
    selecting: Boolean,
    selected: Set<Long>,
    top: androidx.compose.ui.unit.Dp,
    bottom: androidx.compose.ui.unit.Dp,
    onOpen: (NoteItem) -> Unit,
    onLongPress: (NoteItem) -> Unit,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = top, bottom = bottom),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing = 10.dp,
    ) {
        item(key = "header", span = StaggeredGridItemSpan.FullLine) {
            // The header pads itself by 16dp and the grid already does: undo the grid's.
            Box(modifier = Modifier.bleed(16.dp)) { header() }
        }
        items(notes, key = { "g-${it.id}" }) { note ->
            NoteCard(
                note = note,
                selecting = selecting,
                selected = note.id in selected,
                onClick = { onOpen(note) },
                onLongClick = { onLongPress(note) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    note: NoteItem,
    selecting: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val container = when {
        selected -> colors.primaryContainer
        else -> colors.surfaceContainer
    }
    val content = if (selected) colors.onPrimaryContainer else colors.onSurface
    val muted = if (selected) colors.onPrimaryContainer.copy(alpha = 0.8f) else colors.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(container)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(14.dp),
    ) {
        Column {
            Text(
                text = note.title,
                modifier = Modifier.padding(end = if (note.pinned || selecting) 22.dp else 0.dp),
                style = screenType(16f, FontWeight.SemiBold, lineHeight = 19f),
                color = content,
            )
            Text(
                text = note.preview,
                modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
                style = screenType(13f, FontWeight.Normal, lineHeight = 18f),
                color = muted,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (note.folderName != null && note.folderColor != null) {
                    Box(modifier = Modifier.weight(1f, fill = false)) { FolderTag(note.folderName, note.folderColor) }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = note.dateLabel,
                    style = screenType(12f, FontWeight.Normal, lineHeight = 16f),
                    color = muted,
                    maxLines = 1,
                )
            }
        }
        when {
            selecting -> Box(modifier = Modifier.align(Alignment.TopEnd)) { SelectDot(selected) }
            note.pinned -> Icon(
                PinIcon,
                contentDescription = "Pinned",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp),
                tint = colors.primary,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FAB and the New menu
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The mockup's FAB and New menu, built from the Material 3 Expressive FAB menu: an 80dp compose
 * button that turns into a 56dp primary circle with an X, and three primary-container items
 * (folder, checklist, note) stacked above it. The dim layer is drawn by the screen.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NewFabMenu(
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onNewFolder: () -> Unit,
    onNewChecklist: () -> Unit,
    onNewNote: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    FloatingActionButtonMenu(
        expanded = open,
        button = {
            ToggleFloatingActionButton(
                checked = open,
                onCheckedChange = onOpenChange,
                containerColor = ToggleFloatingActionButtonDefaults.containerColor(
                    initialColor = colors.primaryContainer,
                    finalColor = colors.primary,
                ),
                containerSize = ToggleFloatingActionButtonDefaults.containerSize(
                    initialSize = 80.dp,
                    finalSize = 56.dp,
                ),
                containerCornerRadius = ToggleFloatingActionButtonDefaults.containerCornerRadius(
                    initialSize = 26.dp,
                    finalSize = 28.dp,
                ),
            ) {
                val tint = ToggleFloatingActionButtonDefaults.iconColor(
                    initialColor = colors.onPrimaryContainer,
                    finalColor = colors.onPrimary,
                )(checkedProgress)
                Icon(
                    imageVector = if (checkedProgress > 0.5f) CloseIcon else ComposeIcon,
                    contentDescription = if (open) "Close menu" else "New",
                    modifier = Modifier.size(if (checkedProgress > 0.5f) 24.dp else 30.dp),
                    tint = tint,
                )
            }
        },
    ) {
        FloatingActionButtonMenuItem(
            onClick = { onOpenChange(false); onNewFolder() },
            icon = { Icon(FolderPlusIcon, contentDescription = null, modifier = Modifier.size(24.dp)) },
            text = { Text("New folder", style = screenType(16f, FontWeight.SemiBold, lineHeight = 22f)) },
        )
        FloatingActionButtonMenuItem(
            onClick = { onOpenChange(false); onNewChecklist() },
            icon = { Icon(ListIcon, contentDescription = null, modifier = Modifier.size(24.dp)) },
            text = { Text("New checklist", style = screenType(16f, FontWeight.SemiBold, lineHeight = 22f)) },
        )
        FloatingActionButtonMenuItem(
            onClick = { onOpenChange(false); onNewNote() },
            icon = { Icon(ComposeIcon, contentDescription = null, modifier = Modifier.size(24.dp)) },
            text = { Text("New note", style = screenType(16f, FontWeight.SemiBold, lineHeight = 22f)) },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Select mode
// ─────────────────────────────────────────────────────────────────────────────

/** .tab.sel-bar: close, "N selected", select all. */
@Composable
private fun SelectBar(count: Int, onClose: () -> Unit, onSelectAll: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
            Icon(CloseIcon, contentDescription = "Cancel selection")
        }
        Text(
            text = "$count selected",
            modifier = Modifier.padding(start = 4.dp),
            style = screenType(22f, FontWeight.Bold, lineHeight = 28f),
            color = colors.onSurface,
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSelectAll, modifier = Modifier.size(48.dp)) {
            Icon(SelectAllIcon, contentDescription = "Select all")
        }
    }
}

/** .ftw: a floating pill with pin / move / share, and a red delete button beside it. */
@Composable
private fun SelectToolbar(
    enabled: Boolean,
    allPinned: Boolean,
    onPin: (pin: Boolean) -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp)
            .alpha(if (enabled) 1f else 0.5f),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = RoundedCornerShape(32.dp), color = colors.primaryContainer, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ToolbarButton(PinIcon, if (allPinned) "Unpin" else "Pin", enabled) { onPin(!allPinned) }
                ToolbarButton(MoveIcon, "Move to folder", enabled, onMove)
                ToolbarButton(ShareIcon, "Share", enabled, onShare)
            }
        }
        Surface(
            onClick = onDelete,
            enabled = enabled,
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(20.dp),
            color = colors.errorContainer,
            contentColor = colors.onErrorContainer,
            shadowElevation = 4.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(TrashIcon, contentDescription = "Delete", modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun ToolbarButton(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(48.dp)) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Move sheet (#f-move)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MoveSheet(
    count: Int,
    folders: List<FolderItem>,
    initialFolderId: Long?,
    onCancel: () -> Unit,
    onMove: (folderId: Long) -> Unit,
    onNewFolder: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var picked by rememberSaveable { mutableStateOf(initialFolderId) }
    fun closeThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(
        onDismissRequest = onCancel,
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
                text = if (count == 1) "Move 1 note to" else "Move $count notes to",
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
                    .semantics { heading() },
                style = screenType(20f, FontWeight.Bold, lineHeight = 24f),
                color = colors.onSurface,
            )
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                folders.forEach { folder ->
                    // .rw.bsr: a color dot, the name, a radio at the end
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .selectable(selected = picked == folder.id, role = Role.RadioButton) { picked = folder.id }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(resolve(folder.color)),
                        )
                        Text(
                            text = folder.name,
                            modifier = Modifier.weight(1f),
                            style = screenType(16f, FontWeight.Normal, lineHeight = 22f),
                            color = colors.onSurface,
                        )
                        RadioButton(selected = picked == folder.id, onClick = null)
                    }
                }
                // .rw.nf: New folder, in primary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable(role = Role.Button, onClick = onNewFolder)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(FolderPlusIcon, contentDescription = null, modifier = Modifier.size(20.dp), tint = colors.primary)
                    Text(
                        text = "New folder",
                        style = screenType(16f, FontWeight.SemiBold, lineHeight = 22f),
                        color = colors.primary,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                FilledTonalButton(
                    onClick = { closeThen(onCancel) },
                    modifier = Modifier.height(48.dp),
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer,
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) { Text("Cancel", style = screenType(14f, FontWeight.Bold, lineHeight = 20f)) }
                Button(
                    onClick = { picked?.let { id -> closeThen { onMove(id) } } },
                    enabled = picked != null,
                    modifier = Modifier.height(48.dp),
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) { Text("Move", style = screenType(14f, FontWeight.Bold, lineHeight = 20f)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Icons (mockup symbols; shared ones come from the folders package)
// ─────────────────────────────────────────────────────────────────────────────

internal val SearchIcon by lazy { strokeIcon("Search", "M18 11a7 7 0 1 1-14 0a7 7 0 1 1 14 0zM20 20l-3.5-3.5") }
private val SortLinesIcon by lazy { strokeIcon("SortLines", "M4 7h16M7 12h10M10 17h4") }
internal val CheckIcon by lazy { strokeIcon("Check", "m5 12.5 4.5 4.5L19 7.5") }
private val GridIcon by lazy {
    strokeIcon(
        "Grid",
        "M6 4h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z" +
                "M15 4h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2h-3a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z" +
                "M6 13h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2z" +
                "M15 13h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2h-3a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2z",
    )
}
internal val MoveIcon by lazy {
    strokeIcon("Move", "M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM9 13h6M13 10.5l2.5 2.5-2.5 2.5")
}
internal val ShareIcon by lazy {
    strokeIcon("Share", "M12 15V3M8 7l4-4 4 4M6 11H5a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-6a2 2 0 0 0-2-2h-1")
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

private val Personal = Triple(1L, "Personal", FolderColor.Green)
private val Work = Triple(2L, "Work", FolderColor.Amber)
private val Learning = Triple(3L, "Learning", FolderColor.Violet)
private val Ideas = Triple(4L, "Ideas", FolderColor.Rose)

private fun note(
    id: Long,
    title: String,
    date: String,
    preview: String,
    folder: Triple<Long, String, FolderColor>,
    pinned: Boolean = false,
) = NoteItem(id, title, preview, date, folder.first, folder.second, folder.third, pinned)

private val PreviewSections = listOf(
    NoteSection("Pinned", listOf(note(1, "Swift learning plan", "Yesterday", "Views, state, bindings and navigation", Learning, pinned = true))),
    NoteSection(
        "Today",
        listOf(
            note(2, "Grocery list", "10:42 AM", "Oat milk, bananas, spinach, rice", Personal),
            note(3, "Sprint review notes", "8:15 AM", "Demo went well, follow up on API limits", Work),
        ),
    ),
    NoteSection(
        "Previous 7 days",
        listOf(
            note(4, "Book ideas", "Monday", "Short stories about small towns", Ideas),
            note(5, "Trip to Hyderabad", "Sunday", "Train at 6:10 AM, hotel near Banjara Hills", Personal),
            note(6, "SwiftUI state cheat sheet", "Friday", "@State, @Binding, @Observable", Learning),
        ),
    ),
)

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun NotesListPreview() {
    FlowNotesTheme(dynamicColor = false) {
        NotesScreen(state = NotesUiState(sections = PreviewSections, folders = PreviewFolders))
    }
}

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun NotesGridPreview() {
    FlowNotesTheme(dynamicColor = false) {
        NotesScreen(
            state = NotesUiState(sections = PreviewSections, folders = PreviewFolders, viewMode = NotesViewMode.Grid),
        )
    }
}

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun NotesEmptyPreview() {
    FlowNotesTheme(dynamicColor = false) {
        NotesScreen(state = NotesUiState())
    }
}