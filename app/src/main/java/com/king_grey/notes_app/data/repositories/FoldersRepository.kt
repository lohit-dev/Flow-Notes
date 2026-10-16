package com.king_grey.notes_app.data.repositories

import com.king_grey.notes_app.data.models.Folder
import com.king_grey.notes_app.ui.screens.folders.FolderColor
import kotlinx.coroutines.flow.Flow

interface FoldersRepository {
    val folders: Flow<List<Folder>>

    /** Returns the new folder's id. */
    suspend fun create(name: String, color: FolderColor = FolderColor.Violet): Long
    suspend fun rename(id: Long, name: String)
    suspend fun setColor(id: Long, color: FolderColor)

    /** [orderedIds] is the new top to bottom order. */
    suspend fun reorder(orderedIds: List<Long>)

    /** The folder's notes go to Recently deleted when [deleteNotes] is true, otherwise they stay as unfiled notes. */
    suspend fun delete(id: Long, deleteNotes: Boolean)
}
