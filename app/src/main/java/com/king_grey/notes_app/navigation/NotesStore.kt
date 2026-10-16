package com.king_grey.notes_app.navigation

import com.king_grey.notes_app.data.models.Note
import com.king_grey.notes_app.data.models.displayTitle
import com.king_grey.notes_app.data.models.preview
import com.king_grey.notes_app.ui.screens.folders.DeletedNote
import com.king_grey.notes_app.ui.screens.folders.FolderItem
import com.king_grey.notes_app.ui.screens.folders.FolderNote
import com.king_grey.notes_app.ui.screens.notes.NoteItem
import com.king_grey.notes_app.ui.screens.notes.NoteSection
import com.king_grey.notes_app.ui.screens.notes.noteToPlainText
import com.king_grey.notes_app.ui.screens.settings.NoteSort
import com.king_grey.notes_app.ui.screens.settings.SortOrder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Presentation helpers: turn the stored [Note]s into what each screen shows (sections, labels).
// ─────────────────────────────────────────────────────────────────────────────

private const val DAY_MS = 24L * 60 * 60 * 1000
const val DELETED_RETENTION_DAYS = 30

fun shareTextOf(notes: List<Note>): String =
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

/** Mockup: a time for today ("10:42 AM"), "Yesterday", a weekday within a week, then "12 Oct". */
fun listDateLabel(millis: Long, now: Long = System.currentTimeMillis()): String {
    val today = startOfDay(now)
    return when {
        millis >= today -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
        millis >= today - DAY_MS -> "Yesterday"
        millis >= today - 6 * DAY_MS -> SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(millis))
        else -> {
            val sameYear = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.YEAR) ==
                Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.YEAR)
            SimpleDateFormat(if (sameYear) "d MMM" else "d MMM yyyy", Locale.getDefault())
                .format(Date(millis))
        }
    }
}

fun Note.toNoteItem(folders: List<FolderItem>, now: Long = System.currentTimeMillis()): NoteItem {
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

fun Note.toFolderNote(now: Long = System.currentTimeMillis()) = FolderNote(
    id = id,
    title = displayTitle,
    dateLabel = listDateLabel(editedAt, now),
    preview = preview,
    pinned = pinned,
)

fun Note.toDeletedNote(now: Long = System.currentTimeMillis()): DeletedNote {
    val gone = ((now - (deletedAt ?: now)) / DAY_MS).toInt()
    return DeletedNote(
        id = id,
        title = displayTitle,
        daysLeft = (DELETED_RETENTION_DAYS - gone).coerceIn(0, DELETED_RETENTION_DAYS),
        dateLabel = editorDateLabel(editedAt),
        body = preview,
        blocks = blocks,
    )
}

/** Live notes sorted by the user's sort setting. Pinned handling is left to [groupForList]. */
fun sortNotes(notes: List<Note>, sort: NoteSort, order: SortOrder): List<Note> {
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
    notes: List<Note>,
    folders: List<FolderItem>,
    sort: NoteSort,
    order: SortOrder,
    now: Long = System.currentTimeMillis(),
): List<NoteSection> {
    val sorted = sortNotes(notes, sort, order)
    val today = startOfDay(now)
    val week = today - 7 * DAY_MS
    fun section(title: String, list: List<Note>) =
        NoteSection(title, list.map { it.toNoteItem(folders, now) })

    val pinned = sorted.filter { it.pinned }
    val rest = sorted.filterNot { it.pinned }
    val stamp: (Note) -> Long = { if (sort == NoteSort.DateCreated) it.createdAt else it.editedAt }

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
