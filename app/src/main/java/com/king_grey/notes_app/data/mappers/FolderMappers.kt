package com.king_grey.notes_app.data.mappers

import com.king_grey.notes_app.data.local.entity.FolderEntity
import com.king_grey.notes_app.data.models.Folder

fun FolderEntity.toModel(): Folder = Folder(id = id, name = name, color = color, position = position)

fun Folder.toEntity(): FolderEntity = FolderEntity(id = id, name = name, color = color, position = position)
