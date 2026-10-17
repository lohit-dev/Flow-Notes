package com.king_grey.notes_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.king_grey.notes_app.data.models.Folder
import com.king_grey.notes_app.data.models.Note
import com.king_grey.notes_app.data.models.UserSettings
import com.king_grey.notes_app.data.models.isBlank
import com.king_grey.notes_app.data.repositories.FoldersRepository
import com.king_grey.notes_app.data.repositories.NotesRepository
import com.king_grey.notes_app.data.repositories.SearchHistoryRepository
import com.king_grey.notes_app.data.repositories.SettingsRepository
import com.king_grey.notes_app.data.storage.AttachmentStore
import com.king_grey.notes_app.data.storage.attachmentUris
import com.king_grey.notes_app.navigation.DELETED_RETENTION_DAYS
import com.king_grey.notes_app.ui.screens.folders.FolderColor
import com.king_grey.notes_app.ui.screens.notes.NoteBlock
import com.king_grey.notes_app.ui.screens.settings.AccentColor
import com.king_grey.notes_app.ui.screens.settings.AppearanceMode
import com.king_grey.notes_app.ui.screens.settings.NewNoteStart
import com.king_grey.notes_app.ui.screens.settings.NoteSort
import com.king_grey.notes_app.ui.screens.settings.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/** What the editor hands back to be stored. */
data class NoteDraft(
    val id: Long,
    val title: String,
    val blocks: List<NoteBlock>,
    val pinned: Boolean,
    val folderId: Long?,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val foldersRepository: FoldersRepository,
    private val settingsRepository: SettingsRepository,
    private val searchHistory: SearchHistoryRepository,
    private val attachments: AttachmentStore,
) : ViewModel() {

    // null until the first value is read from disk, so the UI can wait instead of flashing defaults.
    val notes: StateFlow<List<Note>?> = notesRepository.notes
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val folders: StateFlow<List<Folder>?> = foldersRepository.folders
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val settings: StateFlow<UserSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Editor saves run one at a time, in the order they were requested. */
    private val saveLock = Mutex()

    /** Recent searches, newest first. Kept on disk. */
    val recentSearches: StateFlow<List<String>> = searchHistory.recent
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * The editor reports every keystroke. Writing each one to Room would re-read and re-decode all
     * notes and recompose the app per letter, so drafts wait for a short pause in typing and only
     * the latest one is written. [flushDraft] writes it at once (leaving the editor, app stopped).
     */
    private val drafts = MutableSharedFlow<NoteDraft>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    @Volatile private var pendingDraft: NoteDraft? = null

    init {
        viewModelScope.launch {
            drafts.debounce(DRAFT_DEBOUNCE_MS).collect { persist(it) }
        }
        viewModelScope.launch {
            notesRepository.purgeExpired(DELETED_RETENTION_DAYS)
            // Drop attachment copies that no note refers to any more.
            attachments.pruneExcept(notesRepository.notes.first().flatMap { it.attachmentUris() }.toSet())
        }
    }

    // ── Notes ────────────────────────────────────────────────────────────────

    /** Stores what the editor shows. A new note is only stored once it has something in it. */
    fun saveDraft(draft: NoteDraft) {
        pendingDraft = draft
        drafts.tryEmit(draft)
    }

    /** Writes the latest draft now instead of waiting for typing to pause. */
    fun flushDraft() {
        viewModelScope.launch { flushPending() }
    }

    private suspend fun flushPending() {
        pendingDraft?.let { persist(it) }
    }

    private suspend fun persist(draft: NoteDraft) {
        saveLock.withLock {
            val now = System.currentTimeMillis()
            val existing = notesRepository.get(draft.id)
            val changed = existing == null || existing.title != draft.title || existing.blocks != draft.blocks
            val note = (existing ?: Note(id = draft.id, createdAt = now)).copy(
                title = draft.title,
                blocks = draft.blocks,
                pinned = draft.pinned,
                folderId = draft.folderId,
                editedAt = if (changed) now else existing!!.editedAt,
            )
            if (existing != null || !note.isBlank()) notesRepository.save(note)
            if (pendingDraft === draft) pendingDraft = null
        }
    }

    /** A note left empty is not worth keeping. */
    fun discardIfBlank(id: Long) {
        viewModelScope.launch {
            flushPending()
            saveLock.withLock {
                val note = notesRepository.get(id)
                if (note != null && note.isBlank()) notesRepository.deleteForever(listOf(id))
            }
        }
    }

    fun setPinned(ids: Collection<Long>, pinned: Boolean) = viewModelScope.launch { notesRepository.setPinned(ids, pinned) }
    fun moveNotes(ids: Collection<Long>, folderId: Long?) = viewModelScope.launch { notesRepository.moveToFolder(ids, folderId) }
    fun trash(ids: Collection<Long>) = viewModelScope.launch { notesRepository.trash(ids) }
    fun restore(ids: Collection<Long>) = viewModelScope.launch { notesRepository.restore(ids) }
    fun deleteForever(ids: Collection<Long>) = viewModelScope.launch {
        val files = ids.mapNotNull { notesRepository.get(it) }.flatMap { it.attachmentUris() }
        notesRepository.deleteForever(ids)
        attachments.delete(files)
    }

    // ── Attachments ──────────────────────────────────────────────────────────

    /** Copies a picked file into app storage. [onDone] gets the stored URI, or null if it could not be read. */
    fun importAttachment(source: Uri, displayName: String?, mimeType: String?, onDone: (String?) -> Unit) {
        viewModelScope.launch { onDone(attachments.import(source, displayName, mimeType)) }
    }

    fun saveCameraImage(bitmap: Bitmap, onDone: (String?) -> Unit) {
        viewModelScope.launch { onDone(attachments.saveBitmap(bitmap)) }
    }

    // ── Search history ───────────────────────────────────────────────────────

    fun addRecentSearch(term: String) = viewModelScope.launch { searchHistory.add(term) }
    fun clearRecentSearches() = viewModelScope.launch { searchHistory.clear() }

    // ── Folders ──────────────────────────────────────────────────────────────

    /** [onCreated] gets the new id once it is stored. */
    fun createFolder(name: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch { onCreated(foldersRepository.create(name)) }
    }

    fun createFolderAndMove(ids: Collection<Long>, name: String) {
        viewModelScope.launch { notesRepository.moveToFolder(ids, foldersRepository.create(name)) }
    }

    fun renameFolder(id: Long, name: String) = viewModelScope.launch { foldersRepository.rename(id, name) }
    fun setFolderColor(id: Long, color: FolderColor) = viewModelScope.launch { foldersRepository.setColor(id, color) }
    fun reorderFolders(orderedIds: List<Long>) = viewModelScope.launch { foldersRepository.reorder(orderedIds) }
    fun deleteFolder(id: Long, deleteNotes: Boolean) = viewModelScope.launch { foldersRepository.delete(id, deleteNotes) }

    // ── Settings ─────────────────────────────────────────────────────────────

    fun setAppearance(v: AppearanceMode) = viewModelScope.launch { settingsRepository.setAppearance(v) }
    fun setAccent(v: AccentColor) = viewModelScope.launch { settingsRepository.setAccent(v) }
    fun setDynamicColor(v: Boolean) = viewModelScope.launch { settingsRepository.setDynamicColor(v) }
    fun setTextSize(v: Int) = viewModelScope.launch { settingsRepository.setTextSize(v) }
    fun setSort(sort: NoteSort, order: SortOrder) = viewModelScope.launch { settingsRepository.setSort(sort, order) }
    fun setNewNoteStart(v: NewNoteStart) = viewModelScope.launch { settingsRepository.setNewNoteStart(v) }
    fun setShowPreviews(v: Boolean) = viewModelScope.launch { settingsRepository.setShowPreviews(v) }
}

private const val DRAFT_DEBOUNCE_MS = 400L
