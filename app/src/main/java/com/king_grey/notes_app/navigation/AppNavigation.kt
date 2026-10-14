package com.king_grey.notes_app.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.king_grey.notes_app.R
import com.king_grey.notes_app.ui.screens.folders.DeletedNote
import com.king_grey.notes_app.ui.screens.folders.FolderColor
import com.king_grey.notes_app.ui.screens.folders.FolderDetailScreen
import com.king_grey.notes_app.ui.screens.folders.FolderDetailUiState
import com.king_grey.notes_app.ui.screens.folders.FolderItem
import com.king_grey.notes_app.ui.screens.folders.FolderNote
import com.king_grey.notes_app.ui.screens.folders.FoldersScreen
import com.king_grey.notes_app.ui.screens.folders.FoldersUiState
import com.king_grey.notes_app.ui.screens.folders.RecentlyDeletedScreen
import com.king_grey.notes_app.ui.screens.folders.RecentlyDeletedUiState
import com.king_grey.notes_app.ui.screens.notes.NotesScreen
import com.king_grey.notes_app.ui.screens.notes.NotesUiState
import com.king_grey.notes_app.ui.screens.notes.NotesViewMode
import com.king_grey.notes_app.ui.screens.notes.NoteSection
import com.king_grey.notes_app.ui.screens.notes.NoteItem
import com.king_grey.notes_app.ui.screens.notes.NoteBlock
import com.king_grey.notes_app.ui.screens.notes.AttachKind
import com.king_grey.notes_app.ui.screens.notes.NoteEditorScreen
import com.king_grey.notes_app.ui.screens.notes.NoteEditorUiState
import com.king_grey.notes_app.ui.screens.notes.nextId
import com.king_grey.notes_app.ui.screens.search.SearchScreen
import com.king_grey.notes_app.ui.screens.search.SearchUiState
import com.king_grey.notes_app.ui.screens.settings.AccentColor
import com.king_grey.notes_app.ui.screens.settings.AppearanceMode
import com.king_grey.notes_app.ui.screens.settings.SettingsScreen
import com.king_grey.notes_app.ui.screens.settings.SettingsSheet
import com.king_grey.notes_app.ui.screens.settings.SettingsUiState
import com.king_grey.notes_app.ui.screens.shared.Scaled
import com.king_grey.notes_app.ui.theme.FlowNotesTheme
import com.king_grey.notes_app.ui.theme.FlowNotesPalette


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

@Composable
fun AppNavigation() {
    val backStack = remember {
        mutableStateListOf<AppDestination>(AppDestination.Notes)
    }
    val currentDestination = backStack.lastOrNull() ?: AppDestination.Notes
    val showBottomBar = topLevelDestinations.any { it.route == currentDestination }
    val layoutDirection = LocalLayoutDirection.current

    var settingsState by remember { mutableStateOf(SettingsUiState()) }
    var foldersState by remember {
        mutableStateOf(
            FoldersUiState(
                folders = listOf(
                    FolderItem(1L, "Work", FolderColor.Violet, 12),
                    FolderItem(2L, "Personal", FolderColor.Green, 5),
                ),
                allNotesCount = 17,
                deletedCount = 1,
            )
        )
    }

    var folderNotesMap by remember {
        mutableStateOf(
            mapOf(
                1L to listOf(FolderNote(1, "Work note 1", "Yesterday", "Meeting notes...")),
                2L to listOf(FolderNote(2, "Personal note 1", "Today", "Shopping list..."))
            )
        )
    }

    var deletedNotesState by remember {
        mutableStateOf(
            listOf(DeletedNote(1, "Old deleted note", 25, "10 Sep 2026", "Some body text"))
        )
    }

    var editorState by remember { mutableStateOf(NoteEditorUiState(noteId = 0L, isNew = true)) }
    var searchQuery by remember { mutableStateOf("") }
    var searchFolderId by remember { mutableStateOf<Long?>(null) }
    var recentSearches by remember { mutableStateOf(emptyList<String>()) }

    val addImageFromPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            editorState = editorState.copy(
                blocks = editorState.blocks + NoteBlock.Image(editorState.blocks.nextId(), uri.toString()),
            )
        }
    }
    val addImageFromDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            editorState = editorState.copy(
                blocks = editorState.blocks + NoteBlock.Image(editorState.blocks.nextId(), uri.toString()),
            )
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { }

    var notesState by remember {
        mutableStateOf(
            NotesUiState(
                sections = listOf(
                    NoteSection(
                        "Today",
                        listOf(
                            NoteItem(1, "Welcome to Flow Notes", "Tap to view or edit this note.", "Today", 1L, "Work", FolderColor.Violet, pinned = true)
                        )
                    )
                ),
                folders = foldersState.folders,
                viewMode = NotesViewMode.List,
                sort = settingsState.sort,
                sortOrder = settingsState.sortOrder,
            )
        )
    }

    LaunchedEffect(foldersState.folders) {
        notesState = notesState.copy(folders = foldersState.folders)
    }

    val context = LocalContext.current
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
            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar {
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
                                    label = { Text(destination.label) },
                                    alwaysShowLabel = true,
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                NavDisplay(
                    backStack = backStack,
                    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                    entryProvider = { key ->
                        when (key) {
                            AppDestination.Notes -> NavEntry(key) {
                                Scaled {
                                    NotesScreen(
                                        state = notesState,
                                        onOpenNote = { note ->
                                            editorState = NoteEditorUiState(
                                                noteId = note.id,
                                                title = note.title,
                                                blocks = listOf(NoteBlock.Text(note.id, note.preview)),
                                                dateLabel = note.dateLabel,
                                                pinned = note.pinned,
                                            )
                                            backStack.add(AppDestination.NoteEditor(note.id))
                                        },
                                        onNewNote = {
                                            val id = System.currentTimeMillis()
                                            editorState = NoteEditorUiState(noteId = id, isNew = true)
                                            backStack.add(AppDestination.NoteEditor(id, isNew = true))
                                        },
                                        onNewChecklist = {
                                            val id = System.currentTimeMillis()
                                            editorState = NoteEditorUiState(
                                                noteId = id,
                                                blocks = listOf(NoteBlock.Check(id)),
                                                isNew = true,
                                            )
                                            backStack.add(AppDestination.NoteEditor(id, isNew = true))
                                        },
                                        onNewFolder = { name ->
                                            val newId = (foldersState.folders.maxOfOrNull { it.id } ?: 0L) + 1L
                                            val newFolder = FolderItem(id = newId, name = name, color = FolderColor.Violet, noteCount = 0)
                                            foldersState = foldersState.copy(folders = foldersState.folders + newFolder)
                                        },
                                        onOpenSearch = {
                                            searchQuery = ""
                                            searchFolderId = null
                                            backStack.add(AppDestination.Search)
                                        },
                                        onFolderFilter = { folderId ->
                                            notesState = notesState.copy(selectedFolderId = folderId)
                                        },
                                        onViewModeChange = { mode ->
                                            notesState = notesState.copy(viewMode = mode)
                                        },
                                        onSortSave = { sort, order ->
                                            notesState = notesState.copy(sort = sort, sortOrder = order)
                                            settingsState = settingsState.copy(sort = sort, sortOrder = order)
                                        },
                                        onOpenDeleted = {
                                            backStack.add(AppDestination.RecentlyDeleted)
                                        },
                                        onTogglePin = { ids, pin ->
                                            val updatedSections = notesState.sections.map { section ->
                                                section.copy(
                                                    notes = section.notes.map { note ->
                                                        if (note.id in ids) note.copy(pinned = pin) else note
                                                    }
                                                )
                                            }
                                            notesState = notesState.copy(sections = updatedSections)
                                        },
                                        onMoveNotes = { ids, folderId ->
                                            val targetFolder = foldersState.folders.firstOrNull { it.id == folderId }
                                            val updatedSections = notesState.sections.map { section ->
                                                section.copy(
                                                    notes = section.notes.map { note ->
                                                        if (note.id in ids) note.copy(folderId = folderId, folderName = targetFolder?.name, folderColor = targetFolder?.color) else note
                                                    }
                                                )
                                            }
                                            notesState = notesState.copy(sections = updatedSections)
                                        },
                                        onCreateFolderAndMove = { ids, name ->
                                            val newId = (foldersState.folders.maxOfOrNull { it.id } ?: 0L) + 1L
                                            val newFolder = FolderItem(id = newId, name = name, color = FolderColor.Violet, noteCount = ids.size)
                                            foldersState = foldersState.copy(folders = foldersState.folders + newFolder)
                                            val updatedSections = notesState.sections.map { section ->
                                                section.copy(
                                                    notes = section.notes.map { note ->
                                                        if (note.id in ids) note.copy(folderId = newId, folderName = name, folderColor = FolderColor.Violet) else note
                                                    }
                                                )
                                            }
                                            notesState = notesState.copy(sections = updatedSections)
                                        },
                                        onShareNotes = {},
                                        onDeleteNotes = { ids ->
                                            val flatNotes = notesState.sections.flatMap { it.notes }
                                            val deletedNotes = flatNotes.filter { it.id in ids }.map {
                                                DeletedNote(it.id, it.title, 30, it.dateLabel, it.preview)
                                            }
                                            deletedNotesState = deletedNotesState + deletedNotes
                                            val updatedSections = notesState.sections.map { section ->
                                                section.copy(notes = section.notes.filter { it.id !in ids })
                                            }.filter { it.notes.isNotEmpty() }
                                            notesState = notesState.copy(sections = updatedSections)
                                            foldersState = foldersState.copy(
                                                allNotesCount = (foldersState.allNotesCount - ids.size).coerceAtLeast(0),
                                                deletedCount = foldersState.deletedCount + ids.size
                                            )
                                        },
                                        onUndoDelete = { ids -> },
                                        onSelectionModeChange = { _ -> },
                                    )
                                }
                            }
                            AppDestination.Search -> NavEntry(key) {
                                val allNotes = notesState.sections.flatMap { it.notes }
                                val query = searchQuery.trim()
                                val results = if (query.isEmpty()) {
                                    emptyList()
                                } else {
                                    allNotes.filter { note ->
                                        (searchFolderId == null || note.folderId == searchFolderId) &&
                                            (note.title.contains(query, ignoreCase = true) ||
                                                note.preview.contains(query, ignoreCase = true))
                                    }
                                }
                                Scaled {
                                    SearchScreen(
                                        state = SearchUiState(
                                            folders = foldersState.folders,
                                            selectedFolderId = searchFolderId,
                                            recent = recentSearches,
                                            results = results,
                                        ),
                                        initialQuery = searchQuery,
                                        onBack = {
                                            if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                                        },
                                        onQueryChange = { searchQuery = it },
                                        onFolderFilter = { searchFolderId = it },
                                        onSubmit = { submitted ->
                                            val term = submitted.trim()
                                            if (term.isNotEmpty()) {
                                                recentSearches = (listOf(term) + recentSearches.filterNot { it.equals(term, true) }).take(8)
                                            }
                                        },
                                        onOpenNote = { note ->
                                            editorState = NoteEditorUiState(
                                                noteId = note.id,
                                                title = note.title,
                                                blocks = listOf(NoteBlock.Text(note.id, note.preview)),
                                                dateLabel = note.dateLabel,
                                                pinned = note.pinned,
                                            )
                                            backStack.add(AppDestination.NoteEditor(note.id))
                                        },
                                        onClearRecent = { recentSearches = emptyList() },
                                    )
                                }
                            }
                            is AppDestination.NoteEditor -> NavEntry(key) {
                                Scaled {
                                    NoteEditorScreen(
                                        state = editorState,
                                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                                        onTitleChange = { editorState = editorState.copy(title = it) },
                                        onBlocksChange = { editorState = editorState.copy(blocks = it) },
                                        onTogglePin = { editorState = editorState.copy(pinned = !editorState.pinned) },
                                        onToggleLock = { editorState = editorState.copy(locked = !editorState.locked) },
                                        onAttach = { kind ->
                                            when (kind) {
                                                AttachKind.Photos -> addImageFromPicker.launch("image/*")
                                                AttachKind.Files -> addImageFromDocument.launch(arrayOf("image/*"))
                                                AttachKind.Camera -> cameraLauncher.launch(
                                                    Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE),
                                                )
                                                AttachKind.ScanDocument -> addImageFromDocument.launch(arrayOf("application/pdf", "image/*"))
                                                AttachKind.VoiceMemo -> {
                                                    runCatching {
                                                        context.startActivity(
                                                            Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                            AppDestination.Folders -> NavEntry(key) {
                                Scaled {
                                    FoldersScreen(
                                        state = foldersState,
                                        onOpenAllNotes = {},
                                        onOpenFolder = { folder ->
                                            backStack.add(AppDestination.FolderDetails(folder.id))
                                        },
                                        onOpenDeleted = {
                                            backStack.add(AppDestination.RecentlyDeleted)
                                        },
                                        onCreateFolder = { name ->
                                            val newId = (foldersState.folders.maxOfOrNull { it.id } ?: 0L) + 1L
                                            val newFolder = FolderItem(id = newId, name = name, color = FolderColor.Violet, noteCount = 0)
                                            foldersState = foldersState.copy(folders = foldersState.folders + newFolder)
                                        },
                                        onRenameFolder = { id, name ->
                                            foldersState = foldersState.copy(
                                                folders = foldersState.folders.map { if (it.id == id) it.copy(name = name) else it }
                                            )
                                        },
                                        onDeleteFolder = { id, deleteNotes ->
                                            val folderToDelete = foldersState.folders.firstOrNull { it.id == id }
                                            val countToRemove = if (deleteNotes && folderToDelete != null) folderToDelete.noteCount else 0
                                            foldersState = foldersState.copy(
                                                folders = foldersState.folders.filter { it.id != id },
                                                allNotesCount = foldersState.allNotesCount - countToRemove,
                                                deletedCount = foldersState.deletedCount + if (deleteNotes) countToRemove else 0
                                            )
                                        },
                                        onMoveFolder = { fromId, toId ->
                                            val list = foldersState.folders.toMutableList()
                                            val fromIdx = list.indexOfFirst { it.id == fromId }
                                            val toIdx = list.indexOfFirst { it.id == toId }
                                            if (fromIdx != -1 && toIdx != -1) {
                                                val item = list.removeAt(fromIdx)
                                                list.add(toIdx, item)
                                                foldersState = foldersState.copy(folders = list)
                                            }
                                        },
                                        onReorderFinished = {},
                                    )
                                }
                            }
                            is AppDestination.FolderDetails -> {
                                val folderId = key.folderId
                                val folder = foldersState.folders.firstOrNull { it.id == folderId }
                                    ?: FolderItem(folderId, "Folder", FolderColor.Violet, 0)
                                val notes = folderNotesMap[folderId] ?: emptyList()
                                NavEntry(key) {
                                    Scaled {
                                        FolderDetailScreen(
                                            state = FolderDetailUiState(
                                                folder = folder,
                                                notes = notes,
                                                otherFolderNames = foldersState.folders.filter { it.id != folderId }.map { it.name }
                                            ),
                                            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                                            onOpenNote = { _ -> },
                                            onNewNote = {
                                                val newNote = FolderNote(
                                                    id = (notes.maxOfOrNull { it.id } ?: 0L) + 1L,
                                                    title = "New note in ${folder.name}",
                                                    dateLabel = "Today",
                                                    preview = "Draft note..."
                                                )
                                                folderNotesMap = folderNotesMap + (folderId to (notes + newNote))
                                                foldersState = foldersState.copy(
                                                    folders = foldersState.folders.map { if (it.id == folderId) it.copy(noteCount = it.noteCount + 1) else it },
                                                    allNotesCount = foldersState.allNotesCount + 1
                                                )
                                            },
                                            onRename = { newName ->
                                                foldersState = foldersState.copy(
                                                    folders = foldersState.folders.map { if (it.id == folderId) it.copy(name = newName) else it }
                                                )
                                            },
                                            onColorChange = { newColor ->
                                                foldersState = foldersState.copy(
                                                    folders = foldersState.folders.map { if (it.id == folderId) it.copy(color = newColor) else it }
                                                )
                                            },
                                            onSortNotes = {},
                                            onDeleteFolder = { deleteNotes ->
                                                val countToRemove = if (deleteNotes) folder.noteCount else 0
                                                foldersState = foldersState.copy(
                                                    folders = foldersState.folders.filter { it.id != folderId },
                                                    allNotesCount = foldersState.allNotesCount - countToRemove,
                                                    deletedCount = foldersState.deletedCount + if (deleteNotes) countToRemove else 0
                                                )
                                                if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                                            }
                                        )
                                    }
                                }
                            }
                            AppDestination.RecentlyDeleted -> {
                                NavEntry(key) {
                                    Scaled {
                                        RecentlyDeletedScreen(
                                            state = RecentlyDeletedUiState(notes = deletedNotesState),
                                            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                                            onOpenNote = { _ -> },
                                            onRestore = { ids ->
                                                deletedNotesState = deletedNotesState.filter { it.id !in ids }
                                                foldersState = foldersState.copy(
                                                    deletedCount = foldersState.deletedCount - ids.size,
                                                    allNotesCount = foldersState.allNotesCount + ids.size
                                                )
                                            },
                                            onDeleteForever = { ids ->
                                                deletedNotesState = deletedNotesState.filter { it.id !in ids }
                                                foldersState = foldersState.copy(
                                                    deletedCount = foldersState.deletedCount - ids.size
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                            AppDestination.Settings -> NavEntry(key) {
                                Scaled {
                                    SettingsScreen(
                                        state = settingsState,
                                        onAppearanceChange = { settingsState = settingsState.copy(appearance = it) },
                                        onAccentChange = { settingsState = settingsState.copy(accent = it) },
                                        onDynamicColorChange = { settingsState = settingsState.copy(dynamicColor = it) },
                                        onTextSizeChange = { settingsState = settingsState.copy(textSize = it) },
                                        onOpenSheet = { settingsState = settingsState.copy(openSheet = it) },
                                        onCloseSheet = { settingsState = settingsState.copy(openSheet = SettingsSheet.None) },
                                        onSortSave = { sort, order ->
                                            settingsState = settingsState.copy(
                                                sort = sort,
                                                sortOrder = order,
                                                openSheet = SettingsSheet.None
                                            )
                                        },
                                        onStartWithSave = { start ->
                                            settingsState = settingsState.copy(
                                                newNoteStart = start,
                                                openSheet = SettingsSheet.None
                                            )
                                        },
                                        onShowPreviewsChange = { settingsState = settingsState.copy(showPreviews = it) },
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
                                                        Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"),
                                                    ),
                                                )
                                            }
                                        },
                                    )
                                }
                            }

                            AppDestination.PrivacyPolicy -> destinationEntry(key, "Privacy Policy")
                        }
                    },
                    modifier = Modifier.padding(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        top = 0.dp,
                        bottom = innerPadding.calculateBottomPadding(),
                    )
                )
            }
        }
    }
}

private fun destinationEntry(
    destination: AppDestination,
    title: String,
) = NavEntry(destination) {
    Scaled {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
