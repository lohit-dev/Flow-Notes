package com.king_grey.notes_app.data.repositories

import com.king_grey.notes_app.data.models.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    /** Every note, including those in Recently deleted. */
    val notes: Flow<List<Note>>

    suspend fun get(id: Long): Note?
    suspend fun save(note: Note)
    suspend fun setPinned(ids: Collection<Long>, pinned: Boolean)
    suspend fun moveToFolder(ids: Collection<Long>, folderId: Long?)
    suspend fun trash(ids: Collection<Long>)
    suspend fun restore(ids: Collection<Long>)
    suspend fun deleteForever(ids: Collection<Long>)
    suspend fun purgeExpired(retentionDays: Int)
}
