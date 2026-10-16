package com.king_grey.notes_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * [blocksJson] holds the document (text, checklist rows, images, links, tables, drawings) as JSON,
 * see NoteBlocksJson. Deleting a folder sets [folderId] to null instead of removing the note.
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("folder_id"), Index("deleted_at")],
)
data class NoteEntity(
    @PrimaryKey val id: Long,
    val title: String,
    @ColumnInfo(name = "blocks_json") val blocksJson: String,
    @ColumnInfo(name = "folder_id") val folderId: Long?,
    val pinned: Boolean,
    val locked: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "edited_at") val editedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
