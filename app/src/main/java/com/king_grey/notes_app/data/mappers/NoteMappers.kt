package com.king_grey.notes_app.data.mappers

import com.king_grey.notes_app.data.local.entity.NoteEntity
import com.king_grey.notes_app.data.models.Note

fun NoteEntity.toModel(): Note = Note(
    id = id,
    title = title,
    blocks = NoteBlocksJson.decode(blocksJson),
    folderId = folderId,
    pinned = pinned,
    createdAt = createdAt,
    editedAt = editedAt,
    deletedAt = deletedAt,
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    blocksJson = NoteBlocksJson.encode(blocks),
    folderId = folderId,
    pinned = pinned,
    createdAt = createdAt,
    editedAt = editedAt,
    deletedAt = deletedAt,
)
