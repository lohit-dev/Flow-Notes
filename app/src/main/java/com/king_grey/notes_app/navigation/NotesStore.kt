package com.king_grey.notes_app.navigation

import com.king_grey.notes_app.ui.screens.folders.DeletedNote
import com.king_grey.notes_app.ui.screens.folders.FolderItem
import com.king_grey.notes_app.ui.screens.folders.FolderNote
import com.king_grey.notes_app.ui.screens.notes.NoteBlock
import com.king_grey.notes_app.ui.screens.notes.NoteItem
import com.king_grey.notes_app.ui.screens.notes.NoteSection
import com.king_grey.notes_app.ui.screens.notes.noteToPlainText
import com.king_grey.notes_app.ui.screens.notes.notePreview
import com.king_grey.notes_app.ui.screens.settings.NoteSort
import com.king_grey.notes_app.ui.screens.settings.SortOrder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// In-memory note store. Plain Kotlin, no persistence: AppNavigation keeps a
// `List<StoredNote>` in Compose state and these helpers derive everything the
// screens show from it. Replace with a repository when real storage lands.
// ─────────────────────────────────────────────────────────────────────────────

private const val DAY_MS = 24L * 60 * 60 * 1000
const val DELETED_RETENTION_DAYS = 30

data class StoredNote(
    val id: Long,
    val title: String = "",
    val blocks: List<NoteBlock> = emptyList(),
    val folderId: Long? = null,
    val pinned: Boolean = false,
    val locked: Boolean = false,
    val createdAt: Long,
    val editedAt: Long = createdAt,
    /** Non-null while the note sits in Recently deleted. */
    val deletedAt: Long? = null,
)

val StoredNote.preview: String get() = notePreview(blocks)
val StoredNote.displayTitle: String get() = title.trim().ifEmpty { "New note" }

/** True when there is nothing worth keeping: no title, no text, no attachments. */
fun StoredNote.isBlank(): Boolean = title.isBlank() && blocks.all { block ->
    when (block) {
        is NoteBlock.Text -> block.text.isBlank()
        is NoteBlock.Check -> block.text.isBlank()
        else -> false
    }
}

fun shareTextOf(notes: List<StoredNote>): String =
    notes.joinToString("\n\n") { noteToPlainText(it.title, it.blocks) }

fun editorDateLabel(millis: Long): String =
    SimpleDateFormat("d MMMM yyyy 'at' h:mm a", Locale.getDefault()).format(Date(millis))

private fun startOfDay(now: Long): Long = Calendar.getInstance().apply {
    timeInMillis = now
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

/** "Today", "Yesterday", or "12 Oct" / "12 Oct 2025". */
fun listDateLabel(millis: Long, now: Long = System.currentTimeMillis()): String {
    val today = startOfDay(now)
    return when {
        millis >= today -> "Today"
        millis >= today - DAY_MS -> "Yesterday"
        else -> {
            val sameYear = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.YEAR) ==
                Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.YEAR)
            SimpleDateFormat(if (sameYear) "d MMM" else "d MMM yyyy", Locale.getDefault())
                .format(Date(millis))
        }
    }
}

fun StoredNote.toNoteItem(folders: List<FolderItem>, now: Long = System.currentTimeMillis()): NoteItem {
    val folder = folders.firstOrNull { it.id == folderId }
    return NoteItem(
        id = id,
        title = displayTitle,
        preview = preview,
        dateLabel = listDateLabel(editedAt, now),
        folderId = folder?.id,
        folderName = folder?.name,
        folderColor = folder?.color,
        pinned = pinned,
    )
}

fun StoredNote.toFolderNote(now: Long = System.currentTimeMillis()) = FolderNote(
    id = id,
    title = displayTitle,
    dateLabel = listDateLabel(editedAt, now),
    preview = preview,
    pinned = pinned,
)

fun StoredNote.toDeletedNote(now: Long = System.currentTimeMillis()): DeletedNote {
    val gone = ((now - (deletedAt ?: now)) / DAY_MS).toInt()
    return DeletedNote(
        id = id,
        title = displayTitle,
        daysLeft = (DELETED_RETENTION_DAYS - gone).coerceIn(0, DELETED_RETENTION_DAYS),
        dateLabel = listDateLabel(editedAt, now),
        body = preview,
    )
}

/** Live notes sorted by the user's sort setting. Pinned handling is left to [groupForList]. */
fun sortNotes(notes: List<StoredNote>, sort: NoteSort, order: SortOrder): List<StoredNote> {
    val base = when (sort) {
        NoteSort.DateEdited -> notes.sortedBy { it.editedAt }
        NoteSort.DateCreated -> notes.sortedBy { it.createdAt }
        NoteSort.Title -> notes.sortedBy { it.displayTitle.lowercase() }
    }
    // Dates: newest first means descending. Title: "newest first" reads as A to Z.
    val newestFirst = order == SortOrder.NewestFirst
    return if (sort == NoteSort.Title) {
        if (newestFirst) base else base.reversed()
    } else {
        if (newestFirst) base.reversed() else base
    }
}

/** Pinned, Today, Previous 7 days, Earlier. Empty sections are left out. */
fun groupForList(
    notes: List<StoredNote>,
    folders: List<FolderItem>,
    sort: NoteSort,
    order: SortOrder,
    now: Long = System.currentTimeMillis(),
): List<NoteSection> {
    val sorted = sortNotes(notes, sort, order)
    val today = startOfDay(now)
    val week = today - 7 * DAY_MS
    fun section(title: String, list: List<StoredNote>) =
        NoteSection(title, list.map { it.toNoteItem(folders, now) })

    val pinned = sorted.filter { it.pinned }
    val rest = sorted.filterNot { it.pinned }
    val stamp: (StoredNote) -> Long = { if (sort == NoteSort.DateCreated) it.createdAt else it.editedAt }

    // Title sort has no meaningful date groups, so everything unpinned goes in one section.
    val sections = if (sort == NoteSort.Title) {
        listOf(section("Notes", rest))
    } else {
        listOf(
            section("Today", rest.filter { stamp(it) >= today }),
            section("Previous 7 days", rest.filter { stamp(it) in week until today }),
            section("Earlier", rest.filter { stamp(it) < week }),
        )
    }
    return (listOf(section("Pinned", pinned)) + sections).filter { it.notes.isNotEmpty() }
}

/** Starting data so every screen has something to show. */
fun seedNotes(now: Long = System.currentTimeMillis()): List<StoredNote> = listOf(
    StoredNote(
        id = 1,
        title = "Welcome to Flow Notes",
        blocks = listOf(NoteBlock.Text(1, "Tap to view or edit this note.")),
        pinned = true,
        createdAt = now - 60_000,
    ),
    StoredNote(
        id = 2,
        title = "Sprint planning",
        blocks = listOf(
            NoteBlock.Check(1, "Review the backlog", checked = true),
            NoteBlock.Check(2, "Pick the top five"),
            NoteBlock.Check(3, "Book the retro"),
        ),
        folderId = 1L,
        createdAt = now - DAY_MS,
    ),
    StoredNote(
        id = 3,
        title = "Weekend shopping",
        blocks = listOf(NoteBlock.Text(1, "Oat milk, lemons, coffee beans, basil.")),
        folderId = 2L,
        createdAt = now - 3 * DAY_MS,
    ),
    StoredNote(
        id = 4,
        title = "Reading list",
        blocks = listOf(NoteBlock.Text(1, "Three books to start before the end of the month.")),
        createdAt = now - 12 * DAY_MS,
    ),
    StoredNote(
        id = 5,
        title = "Old meeting notes",
        blocks = listOf(NoteBlock.Text(1, "Some body text")),
        createdAt = now - 20 * DAY_MS,
        deletedAt = now - 5 * DAY_MS,
    ),
)
