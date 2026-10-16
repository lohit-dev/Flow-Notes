package com.king_grey.notes_app.data.models

import com.king_grey.notes_app.ui.screens.notes.NoteBlock
import com.king_grey.notes_app.ui.screens.notes.notePreview

/** A note as the app works with it. Room's [NoteEntity][com.king_grey.notes_app.data.local.entity.NoteEntity] is mapped to and from this. */
data class Note(
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

val Note.preview: String get() = notePreview(blocks)
val Note.displayTitle: String get() = title.trim().ifEmpty { "New note" }

/** True when there is nothing worth keeping: no title, no text, no attachments. */
fun Note.isBlank(): Boolean = title.isBlank() && blocks.all { block ->
    when (block) {
        is NoteBlock.Text -> block.text.isBlank()
        is NoteBlock.Check -> block.text.isBlank()
        else -> false
    }
}
