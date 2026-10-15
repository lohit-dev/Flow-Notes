package com.king_grey.notes_app.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
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
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.king_grey.notes_app.R
import com.king_grey.notes_app.ui.screens.folders.FolderColor
import com.king_grey.notes_app.ui.screens.folders.FolderDetailScreen
import com.king_grey.notes_app.ui.screens.folders.FolderDetailUiState
import com.king_grey.notes_app.ui.screens.folders.FolderItem
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
import com.king_grey.notes_app.ui.screens.settings.SettingsSheet
import com.king_grey.notes_app.ui.screens.settings.SettingsUiState
import com.king_grey.notes_app.ui.screens.shared.screenType
import com.king_grey.notes_app.ui.theme.FlowNotesPalette
import com.king_grey.notes_app.ui.theme.FlowNotesTheme
import java.io.File

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
 * App shell. All data lives in memory here: [allNotes] is the single source of truth and the
 * list, folder counts, search results and Recently deleted are derived from it every
 * composition. Swap this for a repository when real storage lands.
 */
@Composable
fun AppNavigation() {
    val backStack = remember { mutableStateListOf<AppDestination>(AppDestination.Notes) }
    val currentDestination = backStack.lastOrNull() ?: AppDestination.Notes
    val showBottomBar = topLevelDestinations.any { it.route == currentDestination }
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current

    var settingsState by remember { mutableStateOf(SettingsUiState()) }
    var allNotes by remember { mutableStateOf(seedNotes()) }
    var foldersBase by remember {
        mutableStateOf(
            listOf(
                FolderItem(1L, "Work", FolderColor.Violet, 0),
                FolderItem(2L, "Personal", FolderColor.Green, 0),
            )
        )
    }
    var selectedFolder by remember { mutableStateOf<Long?>(null) }
    var fabMenuOpen by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(NotesViewMode.List) }
    var editorState by remember { mutableStateOf(NoteEditorUiState(noteId = 0L, isNew = true)) }
    var searchQuery by remember { mutableStateOf("") }
    var searchFolderId by remember { mutableStateOf<Long?>(null) }
    var recentSearches by remember { mutableStateOf(emptyList<String>()) }

    // ── Derived from the store ───────────────────────────────────────────────
    // Functions, not vals: NavDisplay keeps each entry's lambda, so a value computed here would
    // be frozen at the moment the entry was created. Calling these inside the entry content
    // reads the state there, and the screen recomposes when it changes.
    fun liveNotes() = allNotes.filter { it.deletedAt == null }
    fun trashedNotes() = allNotes.filter { it.deletedAt != null }
    fun folders(): List<FolderItem> {
        val live = liveNotes()
        return foldersBase.map { f -> f.copy(noteCount = live.count { it.folderId == f.id }) }
    }

    fun foldersUi() = FoldersUiState(
        folders = folders(),
        allNotesCount = liveNotes().size,
        deletedCount = trashedNotes().size,
    )

    fun notesUi(): NotesUiState {
        val all = folders()
        return NotesUiState(
            sections = groupForList(
                notes = liveNotes().filter { selectedFolder == null || it.folderId == selectedFolder },
                folders = all,
                sort = settingsState.sort,
                order = settingsState.sortOrder,
            ),
            folders = all,
            selectedFolderId = selectedFolder,
            viewMode = viewMode,
            sort = settingsState.sort,
            sortOrder = settingsState.sortOrder,
        )
    }

    // ── Store operations ─────────────────────────────────────────────────────
    fun updateNotes(ids: Collection<Long>, change: (StoredNote) -> StoredNote) {
        allNotes = allNotes.map { if (it.id in ids) change(it) else it }
    }

    fun trash(ids: Collection<Long>) {
        val stamp = System.currentTimeMillis()
        updateNotes(ids) { it.copy(deletedAt = stamp) }
    }

    fun restore(ids: Collection<Long>) = updateNotes(ids) { it.copy(deletedAt = null) }

    fun newFolderId() = (foldersBase.maxOfOrNull { it.id } ?: 0L) + 1L

    fun addFolder(name: String): Long {
        val id = newFolderId()
        foldersBase = foldersBase + FolderItem(id, name, FolderColor.Violet, 0)
        return id
    }

    fun deleteFolder(id: Long, deleteNotes: Boolean) {
        val stamp = System.currentTimeMillis()
        allNotes = allNotes.map {
            if (it.folderId == id) {
                it.copy(
                    folderId = null,
                    deletedAt = if (deleteNotes && it.deletedAt == null) stamp else it.deletedAt,
                )
            } else it
        }
        foldersBase = foldersBase.filter { it.id != id }
        if (selectedFolder == id) selectedFolder = null
        if (searchFolderId == id) searchFolderId = null
    }

    /** Saves what the editor shows. A new note is only stored once it has something in it. */
    fun commitEditor(s: NoteEditorUiState) {
        editorState = s
        val existing = allNotes.firstOrNull { it.id == s.noteId }
        val stamp = System.currentTimeMillis()
        val changed = existing == null || existing.title != s.title || existing.blocks != s.blocks
        val note = (existing ?: StoredNote(id = s.noteId, createdAt = stamp)).copy(
            title = s.title,
            blocks = s.blocks,
            pinned = s.pinned,
            locked = s.locked,
            folderId = s.folderId,
            editedAt = if (changed) stamp else existing.editedAt,
        )
        if (existing == null) {
            if (!note.isBlank()) allNotes = allNotes + note
        } else {
            allNotes = allNotes.map { if (it.id == note.id) note else it }
        }
    }

    fun openNote(id: Long) {
        val n = allNotes.firstOrNull { it.id == id } ?: return
        editorState = NoteEditorUiState(
            noteId = n.id,
            title = n.title,
            blocks = n.blocks.ifEmpty { listOf(NoteBlock.Text(1)) },
            dateLabel = editorDateLabel(n.editedAt),
            pinned = n.pinned,
            locked = n.locked,
            folderId = n.folderId,
        )
        backStack.add(AppDestination.NoteEditor(n.id))
    }

    fun startNewNote(forceChecklist: Boolean = false, folderId: Long? = null) {
        val id = (allNotes.maxOfOrNull { it.id } ?: 0L) + 1L
        val start = settingsState.newNoteStart
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

    fun popBack() {
        if (backStack.size <= 1) return
        if (backStack.last() is AppDestination.NoteEditor) {
            // A note left empty is not worth keeping.
            val id = editorState.noteId
            allNotes.firstOrNull { it.id == id }?.let { n ->
                if (n.isBlank()) allNotes = allNotes.filterNot { it.id == id }
            }
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

    fun saveBitmap(bitmap: Bitmap): String {
        val dir = File(context.filesDir, "note_images").apply { mkdirs() }
        val file = File(dir, "img_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        return Uri.fromFile(file).toString()
    }

    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) appendBlock { NoteBlock.Image(it, uri.toString()) }
        }
    val filePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                val mime = context.contentResolver.getType(uri)
                if (mime != null && mime.startsWith("image/")) {
                    appendBlock { NoteBlock.Image(it, uri.toString()) }
                } else {
                    appendBlock {
                        NoteBlock.Link(
                            it,
                            displayName(uri) ?: "File",
                            mime ?: "File",
                            uri.toString()
                        )
                    }
                }
            }
        }
    val cameraCapture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            val uri = saveBitmap(bitmap)
            appendBlock { NoteBlock.Image(it, uri) }
        }
    }
    val voiceRecorder = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uri = result.data?.data
        if (uri != null) appendBlock { NoteBlock.Link(it, "Voice memo", "Audio", uri.toString()) }
    }

    // ── Theme ────────────────────────────────────────────────────────────────
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (settingsState.appearance) {
        AppearanceMode.System -> systemDarkTheme
        AppearanceMode.Light -> false
        AppearanceMode.Dark -> true
    }
    val palette = when (settingsState.accent) {
        AccentColor.Violet -> FlowNotesPalette.Violet
        AccentColor.Sapphire -> FlowNotesPalette.Sapphire
        AccentColor.Meadow -> FlowNotesPalette.Meadow
        AccentColor.Sunset -> FlowNotesPalette.Sunset
        AccentColor.Orchid -> FlowNotesPalette.Orchid
    }
    val baseDensity = LocalDensity.current
    val textScale = settingsState.textSize / 17f

    CompositionLocalProvider(
        LocalDensity provides Density(baseDensity.density, fontScale = textScale),
    ) {
        FlowNotesTheme(
            darkTheme = darkTheme,
            dynamicColor = settingsState.dynamicColor,
            palette = palette,
        ) {
            val colors = MaterialTheme.colorScheme
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
                    entryProvider = { key ->
                        when (key) {
                            AppDestination.Notes -> NavEntry(key) {
                                NotesScreen(
                                    state = notesUi(),
                                    onOpenNote = { note -> openNote(note.id) },
                                    onNewNote = { startNewNote() },
                                    onNewChecklist = { startNewNote(forceChecklist = true) },
                                    onNewFolder = { name -> addFolder(name) },
                                    onOpenSearch = {
                                        searchQuery = ""
                                        searchFolderId = null
                                        backStack.add(AppDestination.Search)
                                    },
                                    onFolderFilter = { selectedFolder = it },
                                    onViewModeChange = { viewMode = it },
                                    onSortSave = { sort, order ->
                                        settingsState =
                                            settingsState.copy(sort = sort, sortOrder = order)
                                    },
                                    onOpenDeleted = { backStack.add(AppDestination.RecentlyDeleted) },
                                    onTogglePin = { ids, pin ->
                                        updateNotes(ids) { it.copy(pinned = pin) }
                                    },
                                    onMoveNotes = { ids, folderId ->
                                        updateNotes(ids) { it.copy(folderId = folderId) }
                                    },
                                    onCreateFolderAndMove = { ids, name ->
                                        val id = addFolder(name)
                                        updateNotes(ids) { it.copy(folderId = id) }
                                    },
                                    onShareNotes = { ids ->
                                        share(shareTextOf(allNotes.filter { it.id in ids }))
                                    },
                                    onDeleteNotes = { ids -> trash(ids) },
                                    onUndoDelete = { ids -> restore(ids) },
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
                                    onSubmit = { submitted ->
                                        val term = submitted.trim()
                                        if (term.isNotEmpty()) {
                                            recentSearches = (listOf(term) +
                                                    recentSearches.filterNot {
                                                        it.equals(
                                                            term,
                                                            true
                                                        )
                                                    }).take(8)
                                        }
                                    },
                                    onOpenNote = { note -> openNote(note.id) },
                                    onClearRecent = { recentSearches = emptyList() },
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
                                        val id = addFolder(name)
                                        commitEditor(editorState.copy(folderId = id))
                                    },
                                    onToggleLock = {
                                        commitEditor(editorState.copy(locked = !editorState.locked))
                                    },
                                    onDelete = {
                                        trash(listOf(editorState.noteId))
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
                                    onCreateFolder = { name -> addFolder(name) },
                                    onRenameFolder = { id, name ->
                                        foldersBase = foldersBase.map {
                                            if (it.id == id) it.copy(name = name) else it
                                        }
                                    },
                                    onDeleteFolder = { id, deleteNotes ->
                                        deleteFolder(
                                            id,
                                            deleteNotes
                                        )
                                    },
                                    onMoveFolder = { fromId, toId ->
                                        val list = foldersBase.toMutableList()
                                        val fromIdx = list.indexOfFirst { it.id == fromId }
                                        val toIdx = list.indexOfFirst { it.id == toId }
                                        if (fromIdx != -1 && toIdx != -1) {
                                            list.add(toIdx, list.removeAt(fromIdx))
                                            foldersBase = list
                                        }
                                    },
                                    onReorderFinished = {},
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
                                        settingsState.sort,
                                        settingsState.sortOrder,
                                    ).map { it.toFolderNote() }
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
                                        onRename = { newName ->
                                            foldersBase = foldersBase.map {
                                                if (it.id == folderId) it.copy(name = newName) else it
                                            }
                                        },
                                        onColorChange = { newColor ->
                                            foldersBase = foldersBase.map {
                                                if (it.id == folderId) it.copy(color = newColor) else it
                                            }
                                        },
                                        onSortNotes = {},
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
                                    onOpenNote = { _ -> },
                                    onRestore = { ids -> restore(ids) },
                                    onDeleteForever = { ids ->
                                        allNotes = allNotes.filterNot { it.id in ids }
                                    },
                                )

                            }

                            AppDestination.Settings -> NavEntry(key) {
                                SettingsScreen(
                                    state = settingsState,
                                    onAppearanceChange = {
                                        settingsState = settingsState.copy(appearance = it)
                                    },
                                    onAccentChange = {
                                        settingsState = settingsState.copy(accent = it)
                                    },
                                    onDynamicColorChange = {
                                        settingsState = settingsState.copy(dynamicColor = it)
                                    },
                                    onTextSizeChange = {
                                        settingsState = settingsState.copy(textSize = it)
                                    },
                                    onOpenSheet = {
                                        settingsState = settingsState.copy(openSheet = it)
                                    },
                                    onCloseSheet = {
                                        settingsState =
                                            settingsState.copy(openSheet = SettingsSheet.None)
                                    },
                                    onSortSave = { sort, order ->
                                        settingsState = settingsState.copy(
                                            sort = sort,
                                            sortOrder = order,
                                            openSheet = SettingsSheet.None,
                                        )
                                    },
                                    onStartWithSave = { start ->
                                        settingsState = settingsState.copy(
                                            newNoteStart = start,
                                            openSheet = SettingsSheet.None,
                                        )
                                    },
                                    onShowPreviewsChange = {
                                        settingsState = settingsState.copy(showPreviews = it)
                                    },
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
        }
    }
}
