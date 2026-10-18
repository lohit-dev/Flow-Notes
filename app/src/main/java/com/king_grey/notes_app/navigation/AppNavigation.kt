package com.king_grey.notes_app.navigation

import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.king_grey.notes_app.R
import com.king_grey.notes_app.data.models.Note
import com.king_grey.notes_app.ui.viewmodel.NoteDraft
import com.king_grey.notes_app.ui.viewmodel.NotesViewModel
import com.king_grey.notes_app.utils.IdGenerator
import com.king_grey.notes_app.ui.screens.folders.FolderColor
import com.king_grey.notes_app.ui.screens.folders.FolderDetailScreen
import com.king_grey.notes_app.ui.screens.folders.FolderDetailUiState
import com.king_grey.notes_app.ui.screens.folders.FolderItem
import com.king_grey.notes_app.ui.screens.folders.DeletedNoteScreen
import com.king_grey.notes_app.ui.screens.folders.FoldersScreen
import com.king_grey.notes_app.ui.screens.folders.FoldersUiState
import com.king_grey.notes_app.ui.screens.folders.RecentlyDeletedScreen
import com.king_grey.notes_app.ui.screens.folders.RecentlyDeletedUiState
import com.king_grey.notes_app.ui.screens.notes.AttachKind
import com.king_grey.notes_app.ui.screens.notes.NoteBlock
import com.king_grey.notes_app.ui.screens.notes.NoteEditorScreen
import com.king_grey.notes_app.ui.screens.notes.NoteEditorUiState
import com.king_grey.notes_app.ui.screens.notes.NotesScreen
import com.king_grey.notes_app.ui.screens.notes.NotesUiState
import com.king_grey.notes_app.ui.screens.notes.NotesViewMode
import com.king_grey.notes_app.ui.screens.notes.nextId
import com.king_grey.notes_app.ui.screens.notes.noteToPlainText
import com.king_grey.notes_app.ui.screens.search.SearchScreen
import com.king_grey.notes_app.ui.screens.search.SearchUiState
import com.king_grey.notes_app.ui.screens.settings.AccentColor
import com.king_grey.notes_app.ui.screens.settings.AppearanceMode
import com.king_grey.notes_app.ui.screens.settings.NewNoteStart
import com.king_grey.notes_app.ui.screens.settings.PrivacyPolicyScreen
import com.king_grey.notes_app.ui.screens.settings.SettingsScreen
import com.king_grey.notes_app.ui.screens.settings.SortSheet
import com.king_grey.notes_app.ui.screens.settings.SettingsSheet
import com.king_grey.notes_app.ui.screens.settings.SettingsUiState
import com.king_grey.notes_app.ui.screens.shared.screenType
import com.king_grey.notes_app.ui.theme.FlowMotion
import com.king_grey.notes_app.ui.theme.FlowNotesPalette
import com.king_grey.notes_app.ui.theme.FlowNotesTheme

private data class TopLevelDestination(
    val route: AppDestination,
    val label: String,
    @DrawableRes val iconRes: Int,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(AppDestination.Notes, "Notes", R.drawable.ic_note),
    TopLevelDestination(AppDestination.Folders, "Folders", R.drawable.ic_folder),
    TopLevelDestination(AppDestination.Settings, "Settings", R.drawable.ic_settings),
)

/**
 * App shell. Notes, folders and settings come from [NotesViewModel] (Room + DataStore); what is
 * local here is only UI state: the back stack, the open editor, the search box, the open sheet.
 */
@Composable
fun AppNavigation(
    viewModel: NotesViewModel = hiltViewModel(),
    // Drawn on top of everything inside the app theme, so it can pick up the accent and dark mode.
    overlay: @Composable () -> Unit = {},
) {
    val backStack = remember { mutableStateListOf<AppDestination>(AppDestination.Notes) }
    val currentDestination = backStack.lastOrNull() ?: AppDestination.Notes
    val showBottomBar = topLevelDestinations.any { it.route == currentDestination }
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current

    val storedNotes by viewModel.notes.collectAsStateWithLifecycle()
    val storedFolders by viewModel.folders.collectAsStateWithLifecycle()
    val storedSettings by viewModel.settings.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    var openSheet by remember { mutableStateOf(SettingsSheet.None) }
    var dragOrder by remember { mutableStateOf<List<Long>?>(null) }

    // Wait for the first read from disk so the theme and lists do not flash defaults.
    val loadedSettings = storedSettings
    if (loadedSettings == null || storedNotes == null || storedFolders == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    // Read through a function, not a captured value: NavDisplay keeps each entry's lambda, so
    // anything read here at composition time would be frozen when the entry was created. The
    // values behind the functions are derivedStateOf, so they are worked out once per change in
    // the data and not on every recomposition.
    val settingsUi by remember {
        derivedStateOf {
            val s = storedSettings ?: loadedSettings
            SettingsUiState(
                appearance = s.appearance,
                accent = s.accent,
                dynamicColor = s.dynamicColor,
                textSize = s.textSize,
                sort = s.sort,
                sortOrder = s.sortOrder,
                newNoteStart = s.newNoteStart,
                showPreviews = s.showPreviews,
                openSheet = openSheet,
            )
        }
    }
    fun settings(): SettingsUiState = settingsUi
    var selectedFolder by remember { mutableStateOf<Long?>(null) }
    var fabMenuOpen by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(NotesViewMode.List) }
    var editorState by remember { mutableStateOf(NoteEditorUiState(noteId = 0L, isNew = true)) }
    var searchQuery by remember { mutableStateOf("") }
    var searchFolderId by remember { mutableStateOf<Long?>(null) }
    var showFolderSort by remember { mutableStateOf(false) }

    // ── Derived from the store ───────────────────────────────────────────────
    // Functions, not vals: NavDisplay keeps each entry's lambda, so a value computed here would
    // be frozen at the moment the entry was created. Calling these inside the entry content
    // reads the state there, and the screen recomposes when it changes.
    val liveNotesList by remember {
        derivedStateOf { storedNotes.orEmpty().filter { it.deletedAt == null } }
    }
    val trashedNotesList by remember {
        derivedStateOf { storedNotes.orEmpty().filter { it.deletedAt != null } }
    }
    val folderItems by remember {
        derivedStateOf {
            val live = liveNotesList
            val counts = live.groupingBy { it.folderId }.eachCount()
            val base = storedFolders.orEmpty()
            val order = dragOrder
            val ordered = if (order != null && order.toSet() == base.map { it.id }.toSet()) {
                order.mapNotNull { id -> base.firstOrNull { it.id == id } }
            } else base
            ordered.map { f -> FolderItem(f.id, f.name, f.color, counts[f.id] ?: 0) }
        }
    }
    val foldersUiState by remember {
        derivedStateOf {
            FoldersUiState(
                folders = folderItems,
                allNotesCount = liveNotesList.size,
                deletedCount = trashedNotesList.size,
            )
        }
    }
    val notesUiState by remember {
        derivedStateOf {
            val s = settingsUi
            NotesUiState(
                sections = groupForList(
                    notes = liveNotesList.filter { selectedFolder == null || it.folderId == selectedFolder },
                    folders = folderItems,
                    sort = s.sort,
                    order = s.sortOrder,
                ),
                folders = folderItems,
                selectedFolderId = selectedFolder,
                viewMode = viewMode,
                sort = s.sort,
                sortOrder = s.sortOrder,
            )
        }
    }
    fun allNotes(): List<Note> = storedNotes.orEmpty()
    fun liveNotes() = liveNotesList
    fun trashedNotes() = trashedNotesList
    fun folders(): List<FolderItem> = folderItems
    fun foldersUi() = foldersUiState
    fun notesUi() = notesUiState

    // ── Store operations ─────────────────────────────────────────────────────
    fun deleteFolder(id: Long, deleteNotes: Boolean) {
        viewModel.deleteFolder(id, deleteNotes)
        if (selectedFolder == id) selectedFolder = null
        if (searchFolderId == id) searchFolderId = null
    }

    /** Hands what the editor shows to the ViewModel to store. */
    fun commitEditor(s: NoteEditorUiState) {
        editorState = s
        viewModel.saveDraft(
            NoteDraft(
                id = s.noteId,
                title = s.title,
                blocks = s.blocks,
                pinned = s.pinned,
                folderId = s.folderId,
            ),
        )
    }

    fun openNote(id: Long) {
        val n = allNotes().firstOrNull { it.id == id } ?: return
        editorState = NoteEditorUiState(
            noteId = n.id,
            title = n.title,
            blocks = n.blocks.ifEmpty { listOf(NoteBlock.Text(1)) },
            dateLabel = editorDateLabel(n.editedAt),
            pinned = n.pinned,
            folderId = n.folderId,
        )
        backStack.add(AppDestination.NoteEditor(n.id))
    }

    fun startNewNote(forceChecklist: Boolean = false, folderId: Long? = null) {
        val id = IdGenerator.newId()
        val start = settings().newNoteStart
        val checklist = forceChecklist || start == NewNoteStart.Checklist
        editorState = NoteEditorUiState(
            noteId = id,
            blocks = if (checklist) listOf(NoteBlock.Check(1)) else listOf(NoteBlock.Text(1)),
            dateLabel = editorDateLabel(System.currentTimeMillis()),
            folderId = folderId,
            isNew = true,
            startOnBody = !checklist && start == NewNoteStart.Body,
        )
        backStack.add(AppDestination.NoteEditor(id, isNew = true))
    }

    // Typing is saved after a short pause; make sure nothing is left waiting if the app goes away.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.flushDraft() }

    fun popBack() {
        if (backStack.size <= 1) return
        if (backStack.last() is AppDestination.NoteEditor) {
            // A note left empty is not worth keeping.
            viewModel.discardIfBlank(editorState.noteId)
        }
        backStack.removeAt(backStack.lastIndex)
    }

    fun share(text: String) {
        if (text.isBlank()) return
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching { context.startActivity(Intent.createChooser(send, null)) }
    }

    // ── Add to note ──────────────────────────────────────────────────────────
    // Minimal stand-ins: pick or capture something and drop it into the open note.
    fun appendBlock(make: (id: Long) -> NoteBlock) {
        val s = editorState
        commitEditor(s.copy(blocks = s.blocks + make(s.blocks.nextId())))
    }

    fun displayName(uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    }.getOrNull()

    // Every attachment is copied into the app's own storage first (see AttachmentStore), so notes
    // keep working after the picker grant or the source file goes away.
    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                viewModel.importAttachment(uri, displayName(uri), context.contentResolver.getType(uri)) { stored ->
                    if (stored != null) appendBlock { NoteBlock.Image(it, stored) }
                }
            }
        }
    val filePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                val mime = context.contentResolver.getType(uri)
                val name = displayName(uri)
                viewModel.importAttachment(uri, name, mime) { stored ->
                    if (stored != null) {
                        if (mime != null && mime.startsWith("image/")) {
                            appendBlock { NoteBlock.Image(it, stored) }
                        } else {
                            appendBlock { NoteBlock.Link(it, name ?: "File", mime ?: "File", stored) }
                        }
                    }
                }
            }
        }
    val cameraCapture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.saveCameraImage(bitmap) { stored ->
                if (stored != null) appendBlock { NoteBlock.Image(it, stored) }
            }
        }
    }
    val voiceRecorder = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            viewModel.importAttachment(uri, "Voice memo.m4a", context.contentResolver.getType(uri)) { stored ->
                if (stored != null) appendBlock { NoteBlock.Link(it, "Voice memo", "Audio", stored) }
            }
        }
    }

    // ── Theme ────────────────────────────────────────────────────────────────
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (settings().appearance) {
        AppearanceMode.System -> systemDarkTheme
        AppearanceMode.Light -> false
        AppearanceMode.Dark -> true
    }
    val palette = when (settings().accent) {
        AccentColor.Violet -> FlowNotesPalette.Violet
        AccentColor.Sapphire -> FlowNotesPalette.Sapphire
        AccentColor.Meadow -> FlowNotesPalette.Meadow
        AccentColor.Sunset -> FlowNotesPalette.Sunset
        AccentColor.Orchid -> FlowNotesPalette.Orchid
        AccentColor.Monochrome -> FlowNotesPalette.Monochrome
    }
    val baseDensity = LocalDensity.current
    val textScale = settings().textSize / 17f

    CompositionLocalProvider(
        LocalDensity provides Density(baseDensity.density, fontScale = textScale),
    ) {
        FlowNotesTheme(
            darkTheme = darkTheme,
            dynamicColor = settings().dynamicColor,
            palette = palette,
        ) {
            val colors = MaterialTheme.colorScheme
            Box(Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = colors.background,
                bottomBar = {
                    if (showBottomBar) {
                        Box {
                            NavigationBar(
                                containerColor = colors.background,
                                tonalElevation = 0.dp,
                            ) {
                                topLevelDestinations.forEach { destination ->
                                    NavigationBarItem(
                                        selected = currentDestination == destination.route,
                                        onClick = {
                                            if (currentDestination != destination.route) {
                                                backStack.clear()
                                                backStack.add(destination.route)
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                painter = painterResource(destination.iconRes),
                                                contentDescription = destination.label,
                                            )
                                        },
                                        label = {
                                            Text(
                                                destination.label,
                                                style = screenType(
                                                    12f,
                                                    FontWeight.Medium,
                                                    16f,
                                                    0.5f
                                                ),
                                            )
                                        },
                                        alwaysShowLabel = true,
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = colors.onPrimaryContainer,
                                            selectedTextColor = colors.onSurface,
                                            indicatorColor = colors.primaryContainer,
                                            unselectedIconColor = colors.onSurfaceVariant,
                                            unselectedTextColor = colors.onSurfaceVariant,
                                        ),
                                    )
                                }
                            }
                            if (fabMenuOpen && currentDestination == AppDestination.Notes) {
                                // Same 40% dim as the notes screen, so it covers the bar too.
                                Box(
                                    Modifier
                                        .matchParentSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                NavDisplay(
                    backStack = backStack,
                    onBack = { popBack() },
                    // Tabs cross-fade with a small scale; going deeper slides in from the right
                    // and back slides out again, all on springs (see FlowMotion).
                    transitionSpec = {
                        if (backStack.lastOrNull()?.let { top -> topLevelDestinations.any { it.route == top } } == true) {
                            (fadeIn(FlowMotion.effects()) + scaleIn(FlowMotion.spatial(), initialScale = 0.96f)) togetherWith
                                fadeOut(FlowMotion.effects())
                        } else {
                            (slideInHorizontally(FlowMotion.spatial()) { it / 4 } + fadeIn(FlowMotion.effects())) togetherWith
                                (slideOutHorizontally(FlowMotion.spatial()) { -it / 6 } + fadeOut(FlowMotion.effects()))
                        }
                    },
                    popTransitionSpec = {
                        (slideInHorizontally(FlowMotion.spatial()) { -it / 6 } + fadeIn(FlowMotion.effects())) togetherWith
                            (slideOutHorizontally(FlowMotion.spatial()) { it / 4 } + fadeOut(FlowMotion.effects()))
                    },
                    entryProvider = { key ->
                        when (key) {
                            AppDestination.Notes -> NavEntry(key) {
                                NotesScreen(
                                    state = notesUi(),
                                    onOpenNote = { note -> openNote(note.id) },
                                    onNewNote = { startNewNote() },
                                    onNewChecklist = { startNewNote(forceChecklist = true) },
                                    onNewFolder = { name -> viewModel.createFolder(name) },
                                    onOpenSearch = {
                                        searchQuery = ""
                                        searchFolderId = null
                                        backStack.add(AppDestination.Search)
                                    },
                                    onFolderFilter = { selectedFolder = it },
                                    onViewModeChange = { viewMode = it },
                                    onSortSave = { sort, order -> viewModel.setSort(sort, order) },
                                    onOpenDeleted = { backStack.add(AppDestination.RecentlyDeleted) },
                                    onTogglePin = { ids, pin -> viewModel.setPinned(ids, pin) },
                                    onMoveNotes = { ids, folderId -> viewModel.moveNotes(ids, folderId) },
                                    onCreateFolderAndMove = { ids, name ->
                                        viewModel.createFolderAndMove(ids, name)
                                    },
                                    onShareNotes = { ids ->
                                        share(shareTextOf(allNotes().filter { it.id in ids }))
                                    },
                                    onDeleteNotes = { ids -> viewModel.trash(ids) },
                                    onUndoDelete = { ids -> viewModel.restore(ids) },
                                    onSelectionModeChange = { _ -> },
                                    onFabOpenChange = { fabMenuOpen = it },
                                )

                            }

                            AppDestination.Search -> NavEntry(key) {
                                val query = searchQuery.trim()
                                val results = if (query.isEmpty()) {
                                    emptyList()
                                } else {
                                    liveNotes()
                                        .filter { note ->
                                            (searchFolderId == null || note.folderId == searchFolderId) &&
                                                    noteToPlainText(note.title, note.blocks)
                                                        .contains(query, ignoreCase = true)
                                        }
                                        .sortedByDescending { it.editedAt }
                                        .map { it.toNoteItem(folders()) }
                                }
                                SearchScreen(
                                    state = SearchUiState(
                                        folders = folders(),
                                        selectedFolderId = searchFolderId,
                                        recent = recentSearches,
                                        results = results,
                                    ),
                                    initialQuery = searchQuery,
                                    onBack = { popBack() },
                                    onQueryChange = { searchQuery = it },
                                    onFolderFilter = { searchFolderId = it },
                                    onSubmit = { submitted -> viewModel.addRecentSearch(submitted) },
                                    onOpenNote = { note -> openNote(note.id) },
                                    onClearRecent = { viewModel.clearRecentSearches() },
                                )

                            }

                            is AppDestination.NoteEditor -> NavEntry(key) {
                                NoteEditorScreen(
                                    state = editorState.copy(folders = folders()),
                                    onBack = { popBack() },
                                    onTitleChange = { commitEditor(editorState.copy(title = it)) },
                                    onBlocksChange = { commitEditor(editorState.copy(blocks = it)) },
                                    onTogglePin = {
                                        commitEditor(editorState.copy(pinned = !editorState.pinned))
                                    },
                                    onMoveToFolder = { folderId ->
                                        commitEditor(editorState.copy(folderId = folderId))
                                    },
                                    onCreateFolderAndMove = { name ->
                                        viewModel.createFolder(name) { id ->
                                            commitEditor(editorState.copy(folderId = id))
                                        }
                                    },
                                    onDelete = {
                                        viewModel.trash(listOf(editorState.noteId))
                                        popBack()
                                    },
                                    onAttach = { kind ->
                                        when (kind) {
                                            AttachKind.Photos -> photoPicker.launch("image/*")
                                            AttachKind.Files -> filePicker.launch(arrayOf("*/*"))
                                            AttachKind.Camera,
                                            AttachKind.ScanDocument -> cameraCapture.launch(null)

                                            AttachKind.VoiceMemo -> runCatching {
                                                voiceRecorder.launch(
                                                    Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION),
                                                )
                                            }
                                        }
                                    },
                                )

                            }

                            AppDestination.Folders -> NavEntry(key) {
                                FoldersScreen(
                                    state = foldersUi(),
                                    onOpenAllNotes = {
                                        selectedFolder = null
                                        backStack.clear()
                                        backStack.add(AppDestination.Notes)
                                    },
                                    onOpenFolder = { folder ->
                                        backStack.add(AppDestination.FolderDetails(folder.id))
                                    },
                                    onOpenDeleted = { backStack.add(AppDestination.RecentlyDeleted) },
                                    onCreateFolder = { name -> viewModel.createFolder(name) },
                                    onRenameFolder = { id, name -> viewModel.renameFolder(id, name) },
                                    onDeleteFolder = { id, deleteNotes ->
                                        deleteFolder(
                                            id,
                                            deleteNotes
                                        )
                                    },
                                    onMoveFolder = { fromId, toId ->
                                        val list = folders().map { it.id }.toMutableList()
                                        val fromIdx = list.indexOf(fromId)
                                        val toIdx = list.indexOf(toId)
                                        if (fromIdx != -1 && toIdx != -1) {
                                            list.add(toIdx, list.removeAt(fromIdx))
                                            dragOrder = list
                                        }
                                    },
                                    onReorderFinished = {
                                        dragOrder?.let { viewModel.reorderFolders(it) }
                                    },
                                )

                            }

                            is AppDestination.FolderDetails -> {
                                val folderId = key.folderId
                                NavEntry(key) {
                                    val all = folders()
                                    val folder = all.firstOrNull { it.id == folderId }
                                        ?: FolderItem(folderId, "Folder", FolderColor.Violet, 0)
                                    val folderNotes = sortNotes(
                                        liveNotes().filter { it.folderId == folderId },
                                        settings().sort,
                                        settings().sortOrder,
                                    ).map { it.toFolderNote() }
                                    if (showFolderSort) {
                                        SortSheet(
                                            sort = settings().sort,
                                            order = settings().sortOrder,
                                            onSave = { sort, order -> viewModel.setSort(sort, order) },
                                            onDismiss = { showFolderSort = false },
                                        )
                                    }
                                    FolderDetailScreen(
                                        state = FolderDetailUiState(
                                            folder = folder,
                                            notes = folderNotes,
                                            otherFolderNames = all
                                                .filter { it.id != folderId }
                                                .map { it.name },
                                        ),
                                        onBack = { popBack() },
                                        onOpenNote = { note -> openNote(note.id) },
                                        onNewNote = { startNewNote(folderId = folderId) },
                                        onRename = { newName -> viewModel.renameFolder(folderId, newName) },
                                        onColorChange = { newColor ->
                                            viewModel.setFolderColor(folderId, newColor)
                                        },
                                        onSortNotes = { showFolderSort = true },
                                        onDeleteFolder = { deleteNotes ->
                                            deleteFolder(folderId, deleteNotes)
                                            popBack()
                                        },
                                    )

                                }
                            }

                            AppDestination.RecentlyDeleted -> NavEntry(key) {
                                RecentlyDeletedScreen(
                                    state = RecentlyDeletedUiState(
                                        notes = trashedNotes().map { it.toDeletedNote() },
                                    ),
                                    onBack = { popBack() },
                                    onOpenNote = { note -> backStack.add(AppDestination.DeletedNotePreview(note.id)) },
                                    onRestore = { ids -> viewModel.restore(ids) },
                                    onDeleteForever = { ids -> viewModel.deleteForever(ids) },
                                )

                            }

                            is AppDestination.DeletedNotePreview -> NavEntry(key) {
                                val note = trashedNotes().firstOrNull { it.id == key.noteId }
                                if (note == null) {
                                    // Restored or deleted from here: nothing left to show.
                                    LaunchedEffect(Unit) { popBack() }
                                } else {
                                    DeletedNoteScreen(
                                        note = note.toDeletedNote(),
                                        onBack = { popBack() },
                                        onRestore = {
                                            viewModel.restore(listOf(note.id))
                                            popBack()
                                        },
                                        onDeleteForever = {
                                            viewModel.deleteForever(listOf(note.id))
                                            popBack()
                                        },
                                    )
                                }
                            }

                            AppDestination.Settings -> NavEntry(key) {
                                SettingsScreen(
                                    state = settings(),
                                    onAppearanceChange = { viewModel.setAppearance(it) },
                                    onAccentChange = { viewModel.setAccent(it) },
                                    onDynamicColorChange = { viewModel.setDynamicColor(it) },
                                    onTextSizeChange = { viewModel.setTextSize(it) },
                                    onOpenSheet = { openSheet = it },
                                    onCloseSheet = { openSheet = SettingsSheet.None },
                                    onSortSave = { sort, order ->
                                        viewModel.setSort(sort, order)
                                        openSheet = SettingsSheet.None
                                    },
                                    onStartWithSave = { start ->
                                        viewModel.setNewNoteStart(start)
                                        openSheet = SettingsSheet.None
                                    },
                                    onShowPreviewsChange = { viewModel.setShowPreviews(it) },
                                    onOpenPrivacy = { backStack.add(AppDestination.PrivacyPolicy) },
                                    onRateApp = {
                                        val marketIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=${context.packageName}"),
                                        )
                                        try {
                                            context.startActivity(marketIntent)
                                        } catch (_: ActivityNotFoundException) {
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    "https://play.google.com/store/apps/details?id=${context.packageName}".toUri(),
                                                ),
                                            )
                                        }
                                    },
                                )

                            }

                            AppDestination.PrivacyPolicy -> NavEntry(key) {
                                PrivacyPolicyScreen(onBack = { popBack() })
                            }
                        }
                    },
                    modifier = Modifier.padding(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        top = 0.dp,
                        bottom = innerPadding.calculateBottomPadding(),
                    ),
                )
            }
            overlay()
            }
        }
    }
}
